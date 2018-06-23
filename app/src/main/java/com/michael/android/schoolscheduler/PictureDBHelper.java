package com.michael.android.schoolscheduler;

import android.content.Context;
import android.database.Cursor;
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

    public void changename(String ori, String subject)
    {
        SQLiteDatabase db = getReadableDatabase();
        db.execSQL("UPDATE picture_data SET subject='" + subject + "' WHERE subject='" + ori + "';");

        // DB에 있는 데이터를 쉽게 처리하기 위해 Cursor를 사용하여 테이블에 있는 모든 데이터 출력
        /*Cursor cursor = db.rawQuery("SELECT * FROM picture_data", null);
        while (cursor.moveToNext()) {
            if(cursor.getString(2).equals(ori))
            {

            }
        }*/
    }


    public void insert(int date, int count, int classtime, String dates) {
        // 읽고 쓰기가 가능하게 DB 열기
        SQLiteDatabase db = getWritableDatabase();
        // DB에 입력한 값으로 행 추가
        db.execSQL("INSERT INTO EXCEPTIONDATE (date, count ,classtime, dates) VALUES (" + date + ", " + count + ", " + classtime + ", '"+dates+"');");
        db.close();
    }

    public void update(int date, int count, int classtime, String dates) {
        SQLiteDatabase db = getWritableDatabase();
        // 입력한 항목과 일치하는 행의 가격 정보 수정
        db.execSQL("UPDATE EXCEPTIONDATE SET classtime=" + classtime + " WHERE date=" + date + ";");
        db.execSQL("UPDATE EXCEPTIONDATE SET count=" + count + " WHERE date=" + date + ";");
        db.execSQL("UPDATE EXCEPTIONDATE SET dates='" + dates + "' WHERE date=" + date + ";");
        db.close();
    }

    public void delete(int date) {
        SQLiteDatabase db = getWritableDatabase();
        // 입력한 항목과 일치하는 행 삭제
        db.execSQL("DELETE FROM EXCEPTIONDATE WHERE date=" + date + ";");
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