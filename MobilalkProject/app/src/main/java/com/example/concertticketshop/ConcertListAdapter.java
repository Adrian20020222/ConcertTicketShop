package com.example.concertticketshop;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

public class ConcertListAdapter extends RecyclerView.Adapter<ConcertListAdapter.ViewHolder> implements Filterable {

    private ArrayList mBandData;
    private ArrayList<ConcertList> mBandDataAll;
    private Context mContext;
    private int lastPosition = -1;


    ConcertListAdapter(Context context, ArrayList<ConcertList> bandData){
        this.mBandData = bandData;
        this.mBandDataAll = bandData;
        this.mContext = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(mContext).inflate(R.layout.concert_list, parent, false));
    }

    @Override
    public void onBindViewHolder(ConcertListAdapter.ViewHolder holder, int position) {
         ConcertList currentBand = (ConcertList) mBandData.get(position);
         
         holder.bindTo(currentBand);

         if (holder.getAdapterPosition() > lastPosition){
             Animation animation = AnimationUtils.loadAnimation(mContext, R.anim.slide_in_row);
             holder.itemView.startAnimation(animation);
             lastPosition = holder.getAdapterPosition();
         }
    }

    @Override
    public int getItemCount() {
        return mBandData.size();
    }

    @Override
    public Filter getFilter() {
        return ConcertFilter;
    }
    private Filter ConcertFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence charSequence) {
            ArrayList<ConcertList> filteredBands = new ArrayList<>();
            FilterResults results = new FilterResults();

            if(charSequence == null || charSequence.length() == 0){
                results.count = mBandDataAll.size();
                results.values = mBandDataAll;

            }else{
                String filterPattern = charSequence.toString().toLowerCase().trim();
                for (ConcertList band : mBandDataAll){

                    if (band.getName().toLowerCase().contains(filterPattern)){
                        filteredBands.add(band);
                    }

                }
                results.count = filteredBands.size();
                results.values = filteredBands;

            }
            return results;
        }

        @Override
        protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
            mBandData = (ArrayList) filterResults.values;

            notifyDataSetChanged();
        }
    };


    class ViewHolder extends RecyclerView.ViewHolder{

        private TextView mName;
        private TextView mInfo;
        private TextView mPrice;
        private ImageView mBandImage;

        public ViewHolder(View itemView) {
            super(itemView);

            mName = itemView.findViewById(R.id.bandName);
            mInfo = itemView.findViewById(R.id.bandInfo);
            mPrice = itemView.findViewById(R.id.ticketPrice);
            mBandImage = itemView.findViewById(R.id.bandImage);

        }

        public void bindTo(ConcertList currentBand) {
            mName.setText(currentBand.getName());
            mInfo.setText(currentBand.getInfo());
            mPrice.setText(currentBand.getPrice());
            Glide.with(mContext).load(currentBand.getImageResource()).into(mBandImage);


            itemView.findViewById(R.id.reserve_ticket).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.d("Activity", "Ticket reserved");
                    ((ConcertListActivity)mContext).updateAlertIcon(currentBand);
                    itemView.findViewById(R.id.delete).setOnClickListener(view1 -> ((ConcertListActivity)mContext).deleteReservation(currentBand));
                }
            });
        }
    }


}


