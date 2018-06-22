package com.michael.android.schoolscheduler;

import android.graphics.Bitmap;

import java.util.ArrayList;

public class GalleryItem {
    private ArrayList<Bitmap> mList;
    private String date;

    public GalleryItem(ArrayList<Bitmap> list, String date){
        mList = list;
        this.date = date;
    }

    public ArrayList<Bitmap> getList() {
        return mList;
    }

    public String getDate() {
        return date;
    }
}
