package com.michael.android.schoolscheduler;

import android.content.ClipData;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ExifInterface;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Time;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements PopupMenu.OnMenuItemClickListener {

    SQLiteDatabase db, timetableDb, pictureDB;
    Cursor c2;
    TimetableDB timetableDB;

    ListView subjectList;
    ArrayList<String> subjects = new ArrayList<String>();
    ArrayAdapter<String> adapter;

    String imageEncoded;
    List<String> imagesEncodedList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(toolbar);

        SubjectDB helper = new SubjectDB(this);
        db = helper.getWritableDatabase();

        subjectList = (ListView) findViewById(R.id.list);
        timetableDB = new TimetableDB(this);
        timetableDb = timetableDB.getWritableDatabase();

        PictureDBHelper pictureDBHelper = new PictureDBHelper(this);
        pictureDB = pictureDBHelper.getWritableDatabase();

        subjectList = (ListView)findViewById(R.id.list);
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, android.R.id.text1, subjects);
        subjectList.setAdapter(adapter);

        Cursor c1 = db.rawQuery("select subject from SUBJECT", null);
        subjects.clear();
        for (int k = 0; k < c1.getCount(); k++) {
            c1.moveToNext();
            String subject = c1.getString(0);
            subjects.add(subject);
        }
        adapter.notifyDataSetChanged();

        subjectList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Intent intent = new Intent(MainActivity.this, GalleryActivity.class);
                intent.putExtra("subject_name", subjects.get(position));
                startActivity(intent);
            }
        });

        c2 = timetableDb.rawQuery("select * from TIMETABLE;", null);
        subjectList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, final int position, long l) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("과목 삭제");
                builder.setMessage(subjects.get(position) + " 과목을 삭제하시겠습니까?");
                builder.setPositiveButton("네", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        while(c2.moveToNext()){
                            for(int k=2; k<=9; k++){
                                if(c2.getString(k) != null && c2.getString(k).equals(subjects.get(position))){
                                    timetableDB.update(c2.getInt(1), k-1, "");
                                }
                            }
                        }
                        c2.moveToFirst();

                        pictureDB.execSQL("delete from picture_data where subject = '"+subjects.get(position)+"';");

                        String[] selectionArgs = {subjects.get(position)};
                        subjects.remove(position);
                        db.delete("SUBJECT", "subject LIKE ?", selectionArgs);
                        adapter.notifyDataSetChanged();
                    }
                }).setNegativeButton("아니요", null);
                AlertDialog dialog = builder.create();
                dialog.show();
                return true;
            }
        });
    }

    public void showPopup(View v) {
        PopupMenu popup = new PopupMenu(this, v);
        popup.setOnMenuItemClickListener(this);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.button_items, popup.getMenu());
        popup.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.items, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.set_schedule:
                Intent intent = new Intent(this, Timetable.class);
                startActivity(intent);
                return true;
            case R.id.donate:
                Toast.makeText(this, "하나은행 620-232984-409 김택서", Toast.LENGTH_LONG).show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onMenuItemClick(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.add_subject://과목추가
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                LayoutInflater inflater = this.getLayoutInflater();
                final View view = inflater.inflate(R.layout.dialog_subject, null);

                builder.setView(view)
                        .setPositiveButton("추가", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                EditText subjectName = (EditText) view.findViewById(R.id.subject_name);
                                EditText teacherName = (EditText) view.findViewById(R.id.subject_t);
                                EditText teacherLocation = (EditText) view.findViewById(R.id.t_location);
                                EditText teacherEmail = (EditText) view.findViewById(R.id.t_email);
                                EditText teacherPhone = (EditText) view.findViewById(R.id.t_phone);

                                db.execSQL("insert into SUBJECT (subject, teacher, location, email, number) values ('" +
                                        subjectName.getText().toString() + "', '" +
                                        teacherName.getText().toString() + "', '" +
                                        teacherLocation.getText().toString() + "', '" +
                                        teacherEmail.getText().toString() + "', '" +
                                        teacherPhone.getText().toString() + "');");

                                Cursor c1 = db.rawQuery("select subject from SUBJECT", null);
                                subjects.clear();
                                for (int k = 0; k < c1.getCount(); k++) {
                                    c1.moveToNext();
                                    String subject = c1.getString(0);
                                    subjects.add(subject);
                                }
                                adapter.notifyDataSetChanged();
                            }
                        })
                        .setNegativeButton("취소", null);
                AlertDialog dialog = builder.create();
                dialog.show();
                return true;
            case R.id.add_picture://사진추가
//                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
//                startActivityForResult(intent, 0);
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), 0);
                return true;
            default:
                return false;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {//사진받아오는곳
        super.onActivityResult(requestCode, resultCode, data);

        try {
            // When an Image is picked
            // Get the Image from data
            if (requestCode == 0 && resultCode == RESULT_OK && data != null) {//이미지를 고르면
                String[] filePathColumn = {MediaStore.Images.Media.DATA};
                imagesEncodedList = new ArrayList<String>();
                if (data.getData() != null) {//사진하나

                    Uri mImageUri = data.getData();
                    setImageonDB(mImageUri.getPath());

                } else {//사진여러개
                    if (data.getClipData() != null) {
                        ClipData mClipData = data.getClipData();
                        ArrayList<Uri> mArrayUri = new ArrayList<Uri>();
                        for (int i = 0; i < mClipData.getItemCount(); i++) {

                            ClipData.Item item = mClipData.getItemAt(i);
                            Uri uri = item.getUri();
                            mArrayUri.add(uri);
                            setImageonDB(mArrayUri.get(i).getPath());

                        }
                    }
                }
            } else {//이미지를 고르지 않으면
                Toast.makeText(this, "You haven't picked Image",
                        Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Something went wrong", Toast.LENGTH_LONG)
                    .show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        Cursor c1 = db.rawQuery("select subject from SUBJECT", null);
        subjects.clear();
        for (int k = 0; k < c1.getCount(); k++) {
            c1.moveToNext();
            String subject = c1.getString(0);
            subjects.add(subject);
        }
        adapter.notifyDataSetChanged();
    }

    public void setImageonDB(String loot)//통합 사진저장메소드
    {
        int m, y, d;
        int classcount=7, classtime=50;
        //날짜받음
        ExifInterface exif = null;
        try {
            exif = new ExifInterface(loot);
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "EXIF LOADING ERROR", Toast.LENGTH_SHORT).show();
        }
        String getdate = exif.TAG_DATETIME;
        Toast.makeText(this, getdate, Toast.LENGTH_SHORT).show();

        //요일변수 세팅
        //예외처리DB검색
        //교시 시간변수 세팅


        //요일,교시,시간변수참조하여 시간표DB에서 과목검색
        //사진DB에 추가

    }

}