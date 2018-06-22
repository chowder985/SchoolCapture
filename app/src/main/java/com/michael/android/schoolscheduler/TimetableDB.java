package com.michael.android.schoolscheduler;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class TimetableDB extends SQLiteOpenHelper {
    public TimetableDB(Context context) {
        super(context, "timetable_table", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS TIMETABLE (_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "day integer not null unique, first text, second text, third text, forth text, fifth text, sixth text, seventh text, eighth text);");
        db.execSQL("INSERT INTO TIMETABLE (day) VALUES(1);");
        db.execSQL("INSERT INTO TIMETABLE (day) VALUES(2);");
        db.execSQL("INSERT INTO TIMETABLE (day) VALUES(3);");
        db.execSQL("INSERT INTO TIMETABLE (day) VALUES(4);");
        db.execSQL("INSERT INTO TIMETABLE (day) VALUES(5);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public void update(int day, int classtime, String subject) {
        SQLiteDatabase db = getWritableDatabase();
        // 입력한 항목과 일치하는 행의 가격 정보 수정
        switch (classtime)
        {
            case 1:
                db.execSQL("UPDATE TIMETABLE SET first='" + subject + "' WHERE day=" + day + ";");
                break;
            case 2:
                db.execSQL("UPDATE TIMETABLE SET second='" + subject + "' WHERE day=" + day + ";");
                break;
            case 3:
                db.execSQL("UPDATE TIMETABLE SET third='" + subject + "' WHERE day=" + day + ";");
                break;
            case 4:
                db.execSQL("UPDATE TIMETABLE SET forth='" + subject + "' WHERE day=" + day + ";");
                break;
            case 5:
                db.execSQL("UPDATE TIMETABLE SET fifth='" + subject + "' WHERE day=" + day + ";");
                break;
            case 6:
                db.execSQL("UPDATE TIMETABLE SET sixth='" + subject + "' WHERE day=" + day + ";");
                break;
            case 7:
                db.execSQL("UPDATE TIMETABLE SET seventh='" + subject + "' WHERE day=" + day + ";");
                break;
            case 8:
                db.execSQL("UPDATE TIMETABLE SET eighth='" + subject + "' WHERE day=" + day + ";");
                break;
            default:
                break;
        }
        db.close();
    }


    public String getResult() {
        // 읽기가 가능하게 DB 열기
        SQLiteDatabase db = getReadableDatabase();
        String result = "";

        // DB에 있는 데이터를 쉽게 처리하기 위해 Cursor를 사용하여 테이블에 있는 모든 데이터 출력
        Cursor cursor = db.rawQuery("SELECT * FROM TIMETABLE", null);
        while (cursor.moveToNext()) {
            result += cursor.getString(0)
                    + " : "
                    + cursor.getString(1)
                    + " | "
                    + "\n";
        }

        return result;
    }
}