package com.michael.android.schoolscheduler;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Sunrin on 2018-06-19.
 */

public class SubjectDB extends SQLiteOpenHelper {


    public SubjectDB(Context context) {
        super(context, "subject_table", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS SUBJECT (_id INTEGER PRIMARY KEY AUTOINCREMENT, subject text not null unique, teacher text, location text, email text, number text);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public String getsubject() {
        // 읽기가 가능하게 DB 열기
        SQLiteDatabase db = getReadableDatabase();
        String result = "";

        // DB에 있는 데이터를 쉽게 처리하기 위해 Cursor를 사용하여 테이블에 있는 모든 데이터 출력
        Cursor cursor = db.rawQuery("SELECT * FROM SUBJECT", null);
        while (cursor.moveToNext()) {
            result += cursor.getString(cursor.getColumnIndex("subject"))
                    + "\n";
        }
        return result;
    }
}