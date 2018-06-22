package com.michael.android.schoolscheduler;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

public class Exceptioneddate extends AppCompatActivity {

    ListView list;
    ExceptionDB exceptionDB;
    SQLiteDatabase db;
    String sql;
    Cursor cursor;
    DatelistAdapter dbAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exceptioneddate);

        list = findViewById(R.id.modifieddate);
        exceptionDB = new ExceptionDB(this);
        selectDB();

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                cursor.moveToPosition(position);
                ExceptionDB exceptionDB = new ExceptionDB(getApplicationContext());
                exceptionDB.delete(cursor.getInt(cursor.getColumnIndex("date")));
                selectDB();
                refresh();
            }
        });
    }

    private void refresh()
    {
        dbAdapter.notifyDataSetChanged();
    }

    private void selectDB(){
        db = exceptionDB.getWritableDatabase();
        sql = "SELECT * FROM EXCEPTIONDATE;";

        cursor = db.rawQuery(sql, null);
        if(cursor.getCount() >= 0){
            dbAdapter = new DatelistAdapter(this, cursor);
            list.setAdapter(dbAdapter);
        }
    }
    
}
