package com.example.concertticketshop;

import android.widget.TextView;

import java.lang.ref.WeakReference;
import java.util.Random;

public class AsyncTask  extends android.os.AsyncTask<Void, Void, String> {

    private WeakReference<TextView> mTextView;

    public AsyncTask(TextView textView) {
        mTextView = new WeakReference<>(textView);

    }

    @Override
    protected String doInBackground(Void... voids) {
        Random rand = new Random();
        int number = rand.nextInt(11);
        int ms = number * 300;


        try{
            Thread.sleep(ms);

        }catch (InterruptedException e){
            e.printStackTrace();

        }

        return "Login as guest";
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        mTextView.get().setText(s);
    }
}
