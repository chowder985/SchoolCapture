package com.michael.android.schoolscheduler;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


public class PictureDBHelper extends SQLiteOpenHelper {

    public static final int DATABASE_VERSION = 1;

    public PictureDBHelper(Context context) {
        super(context, "picturedb", null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String memoSQL= "create table picture_data " +
                "(_id integer primary key autoincrement,"
                + "subject text,"
                + "image_data blob,"
                + "image_date text)";

        db.execSQL(memoSQL);
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if(newVersion==DATABASE_VERSION){
            db.execSQL("drop table picture_data");
            onCreate(db);
        }

    }

}