package com.michael.android.schoolscheduler;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

public class ExceptionDB extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 1;

    public ExceptionDB(Context context) {
        super(context, "exception_table", null, DATABASE_VERSION);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS EXCEPTIONDATE (_id INTEGER PRIMARY KEY AUTOINCREMENT, date INTEGER not null unique, count INTEGER , classtime INTEGER, dates TEXT);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (newVersion == DATABASE_VERSION) {
            db.execSQL("drop table EXCEPTIONDATE");
            onCreate(db);
        }
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

    public String getResult(int date) {
        // 읽기가 가능하게 DB 열기
        SQLiteDatabase db = getReadableDatabase();
        String result = "";

        // DB에 있는 데이터를 쉽게 처리하기 위해 Cursor를 사용하여 테이블에 있는 모든 데이터 출력
        Cursor cursor = db.rawQuery("SELECT * FROM EXCEPTIONDATE", null);
        while (cursor.moveToNext()) {
            if(cursor.getInt(cursor.getColumnIndex("date")) == date)//해당 날짜의 값만 출력
            {
                result += cursor.getString(1)//date
                        + "-"
                        + cursor.getString(2)//count
                        + "-"
                        + cursor.getInt(3)//time
                        + "-"
                        + "\n";
            }
        }
        return result;
    }

    public int getCount() {
        SQLiteDatabase db = getReadableDatabase();
        // DB에 있는 데이터를 쉽게 처리하기 위해 Cursor를 사용하여 테이블에 있는 모든 데이터 출력
        Cursor cursor = db.rawQuery("SELECT * FROM EXCEPTIONDATE", null);
        return cursor.getCount();
    }

    public boolean overlap(int startdate) {
        SQLiteDatabase db = getWritableDatabase();
        Cursor c1 = db.rawQuery("select date from EXCEPTIONDATE", null);
        c1.moveToFirst();
        for (int k = 0; k < c1.getCount(); k++) {
            if (c1.getInt(c1.getColumnIndex("date")) == startdate) {
                return true;
            }
            c1.moveToNext();
        }
        return false;
    }
}