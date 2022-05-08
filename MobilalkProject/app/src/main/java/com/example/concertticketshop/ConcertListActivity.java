package com.example.concertticketshop;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.view.MenuItemCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.job.JobScheduler;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.TypedArray;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.w3c.dom.Document;

import java.util.ArrayList;

public class ConcertListActivity extends AppCompatActivity {
    final private int REQUEST_CODE_ASK_PERMISSIONS = 123;
    private FirebaseUser user;
    private static final String LOG_TAG = ConcertListActivity.class.getName();
    private FirebaseAuth mAuth;
    private RecyclerView mRecycleView;
    private ArrayList<ConcertList> mConcertList;
    private ConcertListAdapter mAdapter;
    private int gridNumber = 1;
    private boolean viewRow = true;
    private FrameLayout redCircle;
    private TextView contentTextView;
    private int alert = 1;
    private int queryLimit = 10;

    private FirebaseFirestore mFirestore;
    private CollectionReference mConcerts;

    private NotificationHandler mNotificationHandler;
    private AlarmManager mAlarmManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_concert_list);

        user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null){
            Log.d(LOG_TAG, "Autenticated user");


        }else{

            Log.d(LOG_TAG, "Unautenticated user");
            finish();
        }
        mRecycleView = findViewById(R.id.recyclerView);
        mRecycleView.setLayoutManager(new GridLayoutManager(this, gridNumber));
        mConcertList = new ArrayList<>();

        mAdapter = new ConcertListAdapter(this, mConcertList);
        mRecycleView.setAdapter(mAdapter);

        mFirestore = FirebaseFirestore.getInstance();
        mConcerts = mFirestore.collection("Concerts");


        queryData();

        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_POWER_CONNECTED);
        filter.addAction(Intent.ACTION_POWER_DISCONNECTED);
        this.registerReceiver(powerReciever, filter);

        mNotificationHandler = new NotificationHandler(this);
        mAlarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        setAlarmManager();

    }

    BroadcastReceiver powerReciever = new BroadcastReceiver(){
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if(action == null){
                return;

            }
            switch (action){
                case Intent.ACTION_POWER_CONNECTED:
                    queryLimit = 10;
                    break;
                case Intent.ACTION_POWER_DISCONNECTED:
                    queryLimit = 5;
                    break;

            }
            queryData();
        }
    };

    private void queryData(){
        mConcertList.clear();

        //mConcerts.whereEqualTo()
        mConcerts.orderBy("reserved_count", Query.Direction.DESCENDING).limit(queryLimit).get().addOnSuccessListener(queryDocumentSnapshots -> {
            for (QueryDocumentSnapshot document : queryDocumentSnapshots){
                ConcertList band = document.toObject(ConcertList.class);
                band.setID(document.getId());
                mConcertList.add(band);
            }
            if (mConcertList.size() == 0){
                initializeData();
                queryData();
            }
            mAdapter.notifyDataSetChanged();

        });

     }

    private void initializeData() {
        String[] bandList = getResources().getStringArray(R.array.band_names);
        String[] info = getResources().getStringArray(R.array.band_info);
        String[] price = getResources().getStringArray(R.array.ticket_price);
        TypedArray imageResource = getResources().obtainTypedArray(R.array.band_images);
       // mConcertList.clear();

        for (int i = 0; i < bandList.length; i++){

            mConcerts.add(new ConcertList(bandList[i], info[i], price[i], imageResource.getResourceId(i, 0), 0));

           // mConcertList.add(new ConcertList(bandList[i], info[i], price[i], imageResource.getResourceId(i, 0)));

        }

        imageResource.recycle();
      //  mAdapter.notifyDataSetChanged();

    }

    public void deleteReservation(ConcertList reserve){
        DocumentReference ref = mConcerts.document(reserve._getID());
        ref.delete().addOnSuccessListener(success -> {
            Log.d(LOG_TAG, "Reservation deleted" + reserve._getID());
        }).addOnFailureListener(failure -> {
            Toast.makeText(this, "Reservation " + reserve._getID() + " cannot be deleted.", Toast.LENGTH_LONG).show();
        });

        queryData();
        mNotificationHandler.cancel();
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu, menu);
        MenuItem menuItem = menu.findItem(R.id.search_bar);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(menuItem);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                Log.d(LOG_TAG, s);
                mAdapter.getFilter().filter(s);
                return false;
            }
        });

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.log_out:
                Log.d(LOG_TAG, "Log out clicked");
                FirebaseAuth.getInstance().signOut();
                finish();
                return true;
            case R.id.setting_button:
                Log.d(LOG_TAG, "Settings clicked");
                return true;
            case R.id.calendar:
                Log.d(LOG_TAG, "Calendar clicked");
                calendarOpen();
            case R.id.reserved:
                Log.d(LOG_TAG, "Reserved tickets clicked");
                return true;
            case R.id.view_selector:
                Log.d(LOG_TAG, "Switch view clicked");
                if(viewRow){
                    changeSpanCount(item, R.drawable.view_grid_icon, 1);

                }else{
                    changeSpanCount(item, R.drawable.view_row_icon, 2);

                }

                return true;
            default:
                return super.onOptionsItemSelected(item);

        }
    }
    void checkUserPermission() {
        if (Build.VERSION.SDK_INT >= 23) {
            if(ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_CALENDAR) != PackageManager.PERMISSION_GRANTED){
                requestPermissions(new String[]{Manifest.permission.WRITE_CALENDAR}, REQUEST_CODE_ASK_PERMISSIONS);
                return;
            }
        }

        addEvent();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_ASK_PERMISSIONS:
                if(grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    addEvent();
                } else {
                    Toast.makeText(this, "Permission denied!", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void calendarOpen() {
        checkUserPermission();
    }

    private void addEvent(){
        Intent intent = new Intent(Intent.ACTION_EDIT);
        intent.setType("vnd.android.cursor.item/event");
        intent.putExtra("title", "New concert");
        intent.putExtra("description", "It's concert time");
        intent.putExtra("beginTime", 0);
        intent.putExtra("endTime", 0);
        startActivity(intent);
    }

    private void changeSpanCount(MenuItem item, int drawableId, int spanCount) {
        viewRow = !viewRow;
        item.setIcon(drawableId);
        GridLayoutManager layoutManager = (GridLayoutManager) mRecycleView.getLayoutManager();
        layoutManager.setSpanCount(spanCount);

    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu){
        final MenuItem alertMenuItem = menu.findItem(R.id.reserved);
        FrameLayout rootView = (FrameLayout) alertMenuItem.getActionView();
        redCircle = (FrameLayout) rootView.findViewById(R.id.alert_red_circle);
        contentTextView = (TextView) rootView.findViewById(R.id.ticket_alert);

        rootView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onOptionsItemSelected(alertMenuItem);
            }
        });

        return super.onPrepareOptionsMenu(menu);

    }

    public void updateAlertIcon(ConcertList reserve){
        alert = (alert + 1);
        if(0 < alert){
            contentTextView.setText("!");

        }else{
            contentTextView.setText("");


        }
        redCircle.setVisibility((alert > 0) ? View.VISIBLE : View.GONE);

        mConcerts.document(reserve._getID()).update("reserved_count", reserve.getReserved_count()+1).addOnFailureListener(failure -> {
            Toast.makeText(this, "Resevation " + reserve._getID() + " cannot be changed.", Toast.LENGTH_LONG).show();

        });
        mNotificationHandler.send(reserve.getName());
        queryData();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(powerReciever);
    }

    private void setAlarmManager(){
        long repeatInterval = 5 * 60 * 1000;
        long triggerTime = SystemClock.elapsedRealtime() + repeatInterval;
        Intent intent = new Intent(this, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        mAlarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerTime,repeatInterval, pendingIntent);

    }
}