package com.michael.android.schoolscheduler;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

public class GalleryActivity extends AppCompatActivity {

    String subjectName;
    RecyclerView galleryList;
    GalleryAdapter galleryAdapter;
    LinearLayoutManager galleryLayout;
    Intent intentFromMain;

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

        ArrayList<Bitmap> subList = new ArrayList<Bitmap>();
        for(int i=0; i<10; i++){
            Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_action_sample);
            subList.add(bitmap);
        }

        ArrayList<GalleryItem> list = new ArrayList<GalleryItem>();
        for(int i=0; i<5; i++){
            GalleryItem item = new GalleryItem(subList, String.valueOf(i));
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
            public void onItemClick(View view, int position) {
                ImageView image = (ImageView)view.findViewById(R.id.image_1);
                Toast.makeText(getApplicationContext(), "click " + position, Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(getApplicationContext(), ImageLookUp.class);
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
}
