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
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

public class GalleryActivity extends AppCompatActivity {

    ArrayList<Bitmap> subList;
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

        subList = new ArrayList<Bitmap>();
        list = new ArrayList<GalleryItem>();
        ArrayList<String> dateList = new ArrayList<String>();

        PictureDBHelper pictureDBHelper = new PictureDBHelper(this);
        pictureDB = pictureDBHelper.getWritableDatabase();
        Cursor c1 = pictureDB.rawQuery("select subject, image_data, image_date from picture_data", null);
        while(c1.moveToNext()){
            if(!dateList.contains(c1.getString(2)) && c1.getString(0) != null && c1.getString(0).equals(subjectName)){
                dateList.add(c1.getString(2));
            }
        }
        c1.moveToFirst();
        for(int i=0; i<dateList.size(); i++){
            do{
                if (c1.getString(0) != null && c1.getString(0).equals(subjectName) &&
                        c1.getString(2) != null && c1.getString(2).equals(dateList.get(i))) {
                    Log.d("MUST SEE", "image in list");
                    Bitmap bitmap = BitmapFactory.decodeByteArray(c1.getBlob(1), 0, c1.getBlob(1).length);
                    subList.add(bitmap);
                }
            }while(c1.moveToNext());
            GalleryItem item = new GalleryItem(subList, dateList.get(i));
            list.add(item);
            c1.moveToFirst();
        }

//        for(int i=0; i<10; i++){
//            Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_action_sample);
//            subList.add(bitmap);
//        }
//        for(int i=0; i<5; i++){
//            GalleryItem item = new GalleryItem(subList, String.valueOf(i));
//            list.add(item);
//        }

        galleryList = (RecyclerView) findViewById(R.id.gallery_list);
        galleryList.setHasFixedSize(false);

        galleryLayout = new LinearLayoutManager(this);
        galleryList.setLayoutManager(galleryLayout);

        galleryAdapter = new GalleryAdapter(list);
        galleryList.setAdapter(galleryAdapter);
        galleryAdapter.SetOnItemClickListener(new HorizontalRecyclerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                ImageView image = (ImageView)view.findViewById(R.id.image_1);
                image.buildDrawingCache();
                Bitmap bitmap = image.getDrawingCache();
                byte[] imageInByte = getBytes(bitmap);
                Toast.makeText(getApplicationContext(), "click " + position, Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(getApplicationContext(), ImageLookUp.class);
                intent.putExtra("image_data", imageInByte);
                startActivity(intent);
            }

            @Override
            public void onItemLongClick(View view, int position) {
                Toast.makeText(getApplicationContext(), "long click " + position, Toast.LENGTH_SHORT).show();
            }
        });
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
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        SharedPreferences prefs = getSharedPreferences("myPref", MODE_PRIVATE);
        subjectName = prefs.getString("subject_name", null);
        getSupportActionBar().setTitle(subjectName);
    }

    public static byte[] getBytes(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 0, stream);
        return stream.toByteArray();
    }
}
