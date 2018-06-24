package com.michael.android.schoolscheduler;

import android.graphics.Bitmap;

import java.util.ArrayList;

public class GalleryItem {
    private ArrayList<String> mList;
    private String date;

    public GalleryItem(ArrayList<String> list, String date){
        mList = list;
        this.date = date;
    }

    public ArrayList<String> getList() {
        return mList;
    }

    public String getDate() {
        return date;
    }
}
