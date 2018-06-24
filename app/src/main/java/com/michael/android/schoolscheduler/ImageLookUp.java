package com.michael.android.schoolscheduler;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import com.github.chrisbanes.photoview.PhotoView;

public class ImageLookUp extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_look_up);

        Toolbar toolbar = (Toolbar)findViewById(R.id.image_toolbar);
        setSupportActionBar(toolbar);

        ActionBar ab = getSupportActionBar();

        ab.setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        byte[] imageInByte = intent.getByteArrayExtra("image_data");
        Bitmap bitmap = BitmapFactory.decodeByteArray(imageInByte, 0, imageInByte.length);

        PhotoView photoView = (PhotoView) findViewById(R.id.photo_view);
        photoView.setImageBitmap(bitmap);
    }
}
