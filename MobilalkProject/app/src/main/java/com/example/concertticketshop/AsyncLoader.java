package com.example.concertticketshop;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.loader.content.AsyncTaskLoader;

import java.util.Random;

public class AsyncLoader extends AsyncTaskLoader<String> {


    public AsyncLoader(@NonNull Context context) {
        super(context);
    }

    @Override
    protected void onStartLoading() {
        super.onStartLoading();
        forceLoad();
    }

    @Nullable
    @Override
    public String loadInBackground() {

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

}
