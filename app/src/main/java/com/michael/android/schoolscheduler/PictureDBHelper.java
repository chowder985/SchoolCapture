package com.michael.android.schoolscheduler;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
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
        String memoSQL= "create table picture_data " +
                "(_id integer primary key autoincrement,"
                + "subject text,"
                + "image_data blob,"
                + "image_date text)";

        db.execSQL(memoSQL);
    }

    public void insert(byte[] img, int day, String subject) {
        // 읽고 쓰기가 가능하게 DB 열기
        SQLiteDatabase db = getWritableDatabase();
        // DB에 입력한 값으로 행 추가

       // db.execSQL("INSERT INTO picture_data (date, count ,classtime, dates) VALUES (" + date + ", " + count + ", " + classtime + ", '"+dates+"');");
        db.close();
    }

    public void update(int date, int count, int classtime, String dates) {
        SQLiteDatabase db = getWritableDatabase();
        // 입력한 항목과 일치하는 행의 가격 정보 수정
        db.execSQL("UPDATE picture_data SET classtime=" + classtime + " WHERE date=" + date + ";");
        db.execSQL("UPDATE picture_data SET count=" + count + " WHERE date=" + date + ";");
        db.execSQL("UPDATE picture_data SET dates='" + dates + "' WHERE date=" + date + ";");
        db.close();
    }

    public void delete(int date) {
        SQLiteDatabase db = getWritableDatabase();
        // 입력한 항목과 일치하는 행 삭제
        db.execSQL("DELETE FROM picture_data WHERE date=" + date + ";");
        db.close();
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if(newVersion==DATABASE_VERSION){
            db.execSQL("drop table picture_data");
            onCreate(db);
        }

    }

}