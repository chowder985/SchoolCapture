package com.michael.android.schoolscheduler;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;

public class SubjectDetailActivity extends AppCompatActivity {

    SQLiteDatabase subjectDb, timetableDb, pictureDb;
    EditText subjectEditName, tName, tLocation, tEmail, tPhone;
    String subjectName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subject_detail);

        Toolbar toolbar = (Toolbar)findViewById(R.id.detail_toolbar);
        setSupportActionBar(toolbar);

        ActionBar ab = getSupportActionBar();

        ab.setDisplayHomeAsUpEnabled(true);

        subjectEditName = (EditText)findViewById(R.id.detail_subject_edit);
        tName = (EditText)findViewById(R.id.detail_t_edit);
        tLocation = (EditText)findViewById(R.id.detail_location_edit);
        tEmail = (EditText)findViewById(R.id.detail_email_edit);
        tPhone = (EditText)findViewById(R.id.detail_phone_edit);

        Intent intent = getIntent();
        subjectName = intent.getStringExtra("subject_name");

        getSupportActionBar().setTitle(subjectName+" 과목의 세부사항");

        SubjectDB helper = new SubjectDB(this);
        subjectDb = helper.getWritableDatabase();

        TimetableDB timetableHelper = new TimetableDB(this);
        timetableDb = timetableHelper.getWritableDatabase();

        PictureDBHelper pictureDBHelper = new PictureDBHelper(this);
        pictureDb = pictureDBHelper.getWritableDatabase();

        Cursor c1 = subjectDb.rawQuery("select subject, teacher, location, email, number from SUBJECT where subject = '"+subjectName+"'", null);
        c1.moveToNext();
        String subject = c1.getString(0);
        String name = c1.getString(1);
        String location = c1.getString(2);
        String email = c1.getString(3);
        String number = c1.getString(4);

        subjectEditName.setText(subject);
        tName.setText(name);
        tLocation.setText(location);
        tEmail.setText(email);
        tPhone.setText(number);
    }

    public void saveToDB(View view){
        subjectDb.execSQL("update SUBJECT set subject = '"+subjectEditName.getText().toString()+"', teacher = '"+tName.getText().toString()+"', location = '"+tLocation.getText().toString()+"', email = '"+tEmail.getText().toString()+"', number = '"+tPhone.getText().toString()+"' where subject = '"+subjectName+"';");
        Cursor c2 = timetableDb.rawQuery("select * from TIMETABLE;", null);
        while(c2.moveToNext()){
            for(int i=2; i<=9; i++){
                if(c2.getString(i)==subjectName){
                    ContentValues cv = new ContentValues();
                    switch (i){
                        case 2:
                            cv.put("first", subjectEditName.getText().toString());
                            break;
                        case 3:
                            cv.put("second", subjectEditName.getText().toString());
                            break;
                        case 4:
                            cv.put("third", subjectEditName.getText().toString());
                            break;
                        case 5:
                            cv.put("forth", subjectEditName.getText().toString());
                            break;
                        case 6:
                            cv.put("fifth", subjectEditName.getText().toString());
                            break;
                        case 7:
                            cv.put("sixth", subjectEditName.getText().toString());
                            break;
                        case 8:
                            cv.put("seventh", subjectEditName.getText().toString());
                            break;
                        case 9:
                            cv.put("eighth", subjectEditName.getText().toString());
                            break;
                        default:
                            break;
                    }
                    timetableDb.update("TIMETABLE", cv, "day=?", new String[]{String.valueOf(c2.getInt(1))});
                }
            }
        }
        pictureDb.execSQL("update picture_data set subject = '"+subjectEditName.getText().toString()+"' where subject = '"+subjectName+"';");
        Intent intent = new Intent();
        intent.putExtra("changed_subject", subjectEditName.getText().toString());
        setResult(RESULT_OK, intent);
        finish();
    }
}
