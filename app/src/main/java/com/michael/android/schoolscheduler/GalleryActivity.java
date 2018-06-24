package com.michael.android.schoolscheduler;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

public class GalleryActivity extends AppCompatActivity {

    ArrayList<String> subList;
    ArrayList<GalleryItem> list;

    String subjectName;
    RecyclerView galleryList;
    GalleryAdapter galleryAdapter;
    LinearLayoutManager galleryLayout;
    Intent intentFromMain;

    SQLiteDatabase pictureDB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);

        Toolbar toolbar = (Toolbar)findViewById(R.id.gallery_toolbar);
        setSupportActionBar(toolbar);

        ActionBar ab = getSupportActionBar();

        ab.setDisplayHomeAsUpEnabled(true);

        intentFromMain = getIntent();
        boolean isBlank = intentFromMain.getStringExtra("subject_name") == null;
        if(subjectName == null && !isBlank) {
            subjectName = intentFromMain.getStringExtra("subject_name");

            SharedPreferences.Editor editor = getSharedPreferences("myPref", MODE_PRIVATE).edit();
            editor.putString("subject_name", subjectName);
            editor.apply();
        }
        getSupportActionBar().setTitle(subjectName);
        bringDataFromDB();
        list = new ArrayList<GalleryItem>();
        ArrayList<String> dateList = new ArrayList<String>();

        PictureDBHelper pictureDBHelper = new PictureDBHelper(this);
        pictureDB = pictureDBHelper.getWritableDatabase();
        Cursor c1 = pictureDB.rawQuery("select subject, image_location, image_date from picture_data", null);


        c1.moveToFirst();
        while(c1.moveToNext()){
            if(!dateList.contains(c1.getString(2))&& c1.getString(0).equals(subjectName)){//해당과목중에 날짜리스트중 추가가 안되어있으면
                dateList.add(c1.getString(2));
            }
        }
        c1.moveToFirst();
        for(int i=0; i<dateList.size(); i++){
            subList = new ArrayList<String>();
            Cursor c2 = pictureDB.rawQuery("select image_location from picture_data where subject='"+subjectName+"' and image_date='"+dateList.get(i)+"'", null);
            while(c2.moveToNext())
            {
                subList.add(c2.getString(0));
            }
            GalleryItem item = new GalleryItem(subList, dateList.get(i));
            list.add(item);
        }

        galleryList = (RecyclerView) findViewById(R.id.gallery_list);
        galleryList.setHasFixedSize(false);

        galleryLayout = new LinearLayoutManager(this);
        galleryList.setLayoutManager(galleryLayout);

        galleryAdapter = new GalleryAdapter(list);
        galleryList.setAdapter(galleryAdapter);

        galleryAdapter.SetOnItemClickListener(new HorizontalRecyclerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position, String path) {
                Cursor cursor = pictureDB.rawQuery("select image_location from picture_data", null);
                while(cursor.moveToNext()){
                    Log.d("Location", cursor.getString(0));
                    if(path.equals(cursor.getString(0))){
                        //byte[] imageInByte = getBytes(bitmap);
                        //Toast.makeText(getApplicationContext(), "click " + position, Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(getApplicationContext(), ImageLookUp.class);
                        intent.putExtra("image_data", cursor.getString(0));
                        startActivityForResult(intent, 20);
                    }
                }
            }

            @Override
            public void onItemLongClick(View view, int position) {
                Toast.makeText(getApplicationContext(), "long click " + position, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void bringDataFromDB(){
        subList = new ArrayList<String>();
        list = new ArrayList<GalleryItem>();
        ArrayList<String> dateList = new ArrayList<String>();

        PictureDBHelper pictureDBHelper = new PictureDBHelper(this);
        pictureDB = pictureDBHelper.getWritableDatabase();
        Cursor c1 = pictureDB.rawQuery("select subject, image_location, image_date from picture_data", null);
        while(c1.moveToNext()){
            if((!dateList.contains(c1.getString(2))) && (c1.getString(0) != null && c1.getString(0).equals(subjectName))){
                dateList.add(c1.getString(2));
            }
        }
        c1.moveToFirst();
        for(int i=0; i<dateList.size(); i++){
            do{
                if ((c1.getString(0) != null && c1.getString(0).equals(subjectName)) &&
                        (c1.getString(2) != null && c1.getString(2).equals(dateList.get(i)))) {
                    Log.d("MUST SEE", "image in list");
                    subList.add(c1.getString(1));
                }
            }while(c1.moveToNext());
            GalleryItem item = new GalleryItem(subList, dateList.get(i));
            list.add(item);
            c1.moveToFirst();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.gallery_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.ascend:

                return true;
            case R.id.descend:

                return true;
            case R.id.detail:
                Intent intent = new Intent(this, SubjectDetailActivity.class);
                intent.putExtra("subject_name", subjectName);
                startActivityForResult(intent, 10);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == 10 && resultCode == RESULT_OK){
            subjectName = data.getStringExtra("changed_subject");
            getSupportActionBar().setTitle(subjectName);
            SharedPreferences.Editor editor = getSharedPreferences("myPref", MODE_PRIVATE).edit();
            editor.putString("subject_name", subjectName);
            editor.apply();
        } else if(requestCode == 20 && resultCode == RESULT_OK){
            Log.d("Delete on return", "Yes");
            finish();
            startActivity(getIntent());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        SharedPreferences prefs = getSharedPreferences("myPref", MODE_PRIVATE);
        subjectName = prefs.getString("subject_name", null);
        getSupportActionBar().setTitle(subjectName);
    }
}
