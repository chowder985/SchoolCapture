package com.michael.android.schoolscheduler;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.graphics.Bitmap;
import android.provider.MediaStore;

import java.io.ByteArrayOutputStream;


public class PictureDBHelper extends SQLiteOpenHelper {

    public static final int DATABASE_VERSION = 1;

    public PictureDBHelper(Context context) {
        super(context, "picturedb", null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String memoSQL = "create table picture_data " +
                "(_id integer primary key autoincrement,"
                + "subject text,"
                + "image_location text not null unique ,"
                + "image_date text, "
                + "classtime integer)";

        db.execSQL(memoSQL);
    }

    public void insert(String imagelocation, String day, String subject, int classtime) {
        // 읽고 쓰기가 가능하게 DB 열기
        SQLiteDatabase db = getWritableDatabase();
        // DB에 입력한 값으로 행 추가
        db.execSQL("INSERT INTO picture_data (image_location, subject, image_date, classtime) values ('" + imagelocation + "','" + subject + "', '" + day + "', " + classtime + ");");
        db.close();
    }

    public void update(String imagelocation, String subject) {
        // 읽고 쓰기가 가능하게 DB 열기
        SQLiteDatabase db = getWritableDatabase();
        // DB에 입력한 값으로 행 추가
        db.execSQL("UPDATE picture_data SET subject= '" + subject + "' WHERE image_location='" + imagelocation + "';");
        db.close();
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (newVersion == DATABASE_VERSION) {
            db.execSQL("drop table picture_data");
            onCreate(db);
        }

    }

}