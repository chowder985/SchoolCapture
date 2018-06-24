package com.michael.android.schoolscheduler;

import android.Manifest;
import android.app.PendingIntent;
import android.content.ClipData;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
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

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
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
                if ((ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) &&
                        (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)) {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                }else {
                    Intent intent = new Intent();
                    intent.setType("image/*");
                    intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                    intent.setAction(Intent.ACTION_OPEN_DOCUMENT);
                    startActivityForResult(Intent.createChooser(intent, "Select Picture"), 0);
                }
                return true;
            default:
                return false;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    Intent intent = new Intent();
                    intent.setType("image/*");
                    intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                    intent.setAction(Intent.ACTION_OPEN_DOCUMENT);
                    startActivityForResult(Intent.createChooser(intent, "Select Picture"), 0);
//                    Intent i = getBaseContext().getPackageManager()
//                            .getLaunchIntentForPackage( getBaseContext().getPackageName() );
//                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                    finishAndRemoveTask();
//                    startActivity(i);
                } else {
                    //do something like displaying a message that he didn`t allow the app to access gallery and you wont be able to let him select from gallery
                }
                break;
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

                    Cursor cursor = this.getContentResolver().query(mImageUri,
                            null, null, null, null, null);
                    // Move to first row
                    cursor.moveToFirst();

                    String displayName = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                    cursor.close();

                    //Toast.makeText(this, displayName, Toast.LENGTH_SHORT).show();

                    setImageonDB("/storage/emulated/0/DCIM/Camera/"+displayName, mImageUri);

                } else {//사진여러개
                    if (data.getClipData() != null) {
                        ClipData mClipData = data.getClipData();
                        ArrayList<Uri> mArrayUri = new ArrayList<Uri>();
                        for (int i = 0; i < mClipData.getItemCount(); i++) {
                            ClipData.Item item = mClipData.getItemAt(i);
                            Uri uri = item.getUri();
                            mArrayUri.add(uri);
                            Cursor cursor = this.getContentResolver().query(mArrayUri.get(i),
                                    null, null, null, null, null);
                            // Move to first row
                            cursor.moveToFirst();
                            String displayName = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                            cursor.close();
                            setImageonDB("/storage/emulated/0/DCIM/Camera/"+displayName, mArrayUri.get(i));
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

    public void setImageonDB(String loot, Uri uri) throws Exception//통합 사진저장메소드
    {
        int m=6, y=2018, d=23;
        int takenh, takenm, takens;
        int day = 1, classcount=7, classtime=50;
        //날짜받음
        ExifInterface exif = null;
        try {
            exif = new ExifInterface(loot);
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "EXIF LOADING ERROR", Toast.LENGTH_SHORT).show();
        }
        String getdate = exif.getAttribute(ExifInterface.TAG_DATETIME);
        //Toast.makeText(this, getdate, Toast.LENGTH_SHORT).show();
        //Log.d("Date", getdate);

        //요일변수 세팅
        //예외처리DB검색
        //교시 시간변수 세팅
        String s[] = getdate.split(" ");//format 2018:06:23 20:13:21
        String date[] = s[0].split(":");//날짜 분리저장
        y=Integer.parseInt(date[0]);
        m=Integer.parseInt(date[1]);
        d=Integer.parseInt(date[2]);
        String time[] = s[1].split(":");//시간 분리저장
        takenh = Integer.parseInt(time[0]);
        takenm = Integer.parseInt(time[1]);
        takens = Integer.parseInt(time[2]);

//        day = getDateDay(date[0], date[1], date[2], "yyyy-M-dd");//요일변수 세팅
//        if (day == 0) {
//            Toast.makeText(this, "시간표에 지정되지 않은 시간에 촬영된 사진입니다", Toast.LENGTH_SHORT).show();
//            return;
//        }
//        Log.d("DAY", Integer.toString(day));
//
//        int search = dateTOint(y,m,d);
//        ExceptionDB exceptionDB = new ExceptionDB(getApplicationContext());
//        boolean def = exceptionDB.overlap(search);//예외처리DB검색
//        if (def)//예외처리된 날짜일경우
//        {
//            String exception = exceptionDB.getResult(search);
//            String exval[] = exception.split("-");
//            classcount = Integer.parseInt(exval[1]);//교시 시간변수 세팅
//            classtime = Integer.parseInt(exval[1]);
//        }

        //요일,교시,시간변수참조하여 시간표DB에서 과목검색
        //사진DB에 추가
        switch (classtime) {
            case 50:
                if (classcount >= 1 && (Integer.parseInt(getdate.substring(11, 13)) == 8 && Integer.parseInt(getdate.substring(14, 16)) >= 40) ||
                        (Integer.parseInt(getdate.substring(11, 13)) == 9 && Integer.parseInt(getdate.substring(14, 16)) < 40)) {
                    Log.d("교시", "1교시");
                    searchTimetableDB(day, "first", uri, getdate.substring(0, 10));
                } else if (classcount >= 2 && (Integer.parseInt(getdate.substring(11, 13)) == 9 && Integer.parseInt(getdate.substring(14, 16)) >= 40) ||
                        (Integer.parseInt(getdate.substring(11, 13)) == 10 && Integer.parseInt(getdate.substring(14, 16)) < 40)) {
                    Log.d("교시", "2교시");
                    searchTimetableDB(day, "second", uri, getdate.substring(0, 10));
                } else if (classcount >= 3 && (Integer.parseInt(getdate.substring(11, 13)) == 10 && Integer.parseInt(getdate.substring(14, 16)) >= 40) ||
                        (Integer.parseInt(getdate.substring(11, 13)) == 11 && Integer.parseInt(getdate.substring(14, 16)) < 40)) {
                    Log.d("교시", "3교시");
                    searchTimetableDB(day, "third", uri, getdate.substring(0, 10));
                } else if (classcount >= 4 && (Integer.parseInt(getdate.substring(11, 13)) == 11 && Integer.parseInt(getdate.substring(14, 16)) >= 40) ||
                        (Integer.parseInt(getdate.substring(11, 13)) == 12 && Integer.parseInt(getdate.substring(14, 16)) < 30)) {
                    Log.d("교시", "4교시");
                    searchTimetableDB(day, "forth", uri, getdate.substring(0, 10));
                } else if (classcount >= 5 && (Integer.parseInt(getdate.substring(11, 13)) == 13 && Integer.parseInt(getdate.substring(14, 16)) >= 20) ||
                        (Integer.parseInt(getdate.substring(11, 13)) == 14 && Integer.parseInt(getdate.substring(14, 16)) < 20)){
                    Log.d("교시", "5교시");
                    searchTimetableDB(day, "fifth", uri, getdate.substring(0, 10));
                } else if (classcount >= 6 && (Integer.parseInt(getdate.substring(11, 13)) == 14 && Integer.parseInt(getdate.substring(14, 16)) >= 20) ||
                        (Integer.parseInt(getdate.substring(11, 13)) == 15 && Integer.parseInt(getdate.substring(14, 16)) < 20)) {
                    Log.d("교시", "6교시");
                    searchTimetableDB(day, "sixth", uri, getdate.substring(0, 10));
                } else if (classcount >= 7 && (Integer.parseInt(getdate.substring(11, 13)) == 15 && Integer.parseInt(getdate.substring(14, 16)) >= 20) ||
                        (Integer.parseInt(getdate.substring(11, 13)) == 16 && Integer.parseInt(getdate.substring(14, 16)) < 20)) {
                    Log.d("교시", "7교시");
                    searchTimetableDB(day, "seventh", uri, getdate.substring(0, 10));
                } else if (classcount >= 8 && (Integer.parseInt(getdate.substring(11, 13)) == 16 && Integer.parseInt(getdate.substring(14, 16)) >= 20) ||
                        (Integer.parseInt(getdate.substring(11, 13)) == 17 && Integer.parseInt(getdate.substring(14, 16)) <= 10)) {
                    Log.d("교시", "8교시");
                    searchTimetableDB(day, "eighth", uri, getdate.substring(0, 10));
                } else {
                    Toast.makeText(this, "Not at School", Toast.LENGTH_SHORT).show();
                }
                break;
            case 45:
                if (classcount >= 1 && (Integer.parseInt(getdate.substring(11, 13)) == 8 && Integer.parseInt(getdate.substring(14, 16)) >= 40) ||
                        (Integer.parseInt(getdate.substring(11, 13)) == 9 && Integer.parseInt(getdate.substring(14, 16)) < 35)) {
                    Log.d("교시", "1교시");
                    searchTimetableDB(day, "first", uri, getdate.substring(0, 10));
                } else if (classcount >= 2 && (Integer.parseInt(getdate.substring(11, 13)) == 9 && Integer.parseInt(getdate.substring(14, 16)) >= 35) ||
                        (Integer.parseInt(getdate.substring(11, 13)) == 10 && Integer.parseInt(getdate.substring(14, 16)) < 30)) {
                    Log.d("교시", "2교시");
                    searchTimetableDB(day, "second", uri, getdate.substring(0, 10));
                } else if (classcount >= 3 && (Integer.parseInt(getdate.substring(11, 13)) == 10 && Integer.parseInt(getdate.substring(14, 16)) >= 30) ||
                        (Integer.parseInt(getdate.substring(11, 13)) == 11 && Integer.parseInt(getdate.substring(14, 16)) < 25)) {
                    Log.d("교시", "3교시");
                    searchTimetableDB(day, "third", uri, getdate.substring(0, 10));
                } else if (classcount >= 4 && (Integer.parseInt(getdate.substring(11, 13)) == 11 && Integer.parseInt(getdate.substring(14, 16)) >= 25) ||
                        (Integer.parseInt(getdate.substring(11, 13)) == 12 && Integer.parseInt(getdate.substring(14, 16)) < 20)) {
                    Log.d("교시", "4교시");
                    searchTimetableDB(day, "forth", uri, getdate.substring(0, 10));
                } else if (classcount >= 5 && (Integer.parseInt(getdate.substring(11, 13)) == 13 && Integer.parseInt(getdate.substring(14, 16)) >= 0) ||
                        (Integer.parseInt(getdate.substring(11, 13)) == 13 && Integer.parseInt(getdate.substring(14, 16)) < 55)){
                    Log.d("교시", "5교시");
                    searchTimetableDB(day, "fifth", uri, getdate.substring(0, 10));
                } else if (classcount >= 6 && (Integer.parseInt(getdate.substring(11, 13)) == 13 && Integer.parseInt(getdate.substring(14, 16)) >= 55) ||
                        (Integer.parseInt(getdate.substring(11, 13)) == 14 && Integer.parseInt(getdate.substring(14, 16)) < 50)) {
                    Log.d("교시", "6교시");
                    searchTimetableDB(day, "sixth", uri, getdate.substring(0, 10));
                } else if (classcount >= 7 && (Integer.parseInt(getdate.substring(11, 13)) == 14 && Integer.parseInt(getdate.substring(14, 16)) >= 50) ||
                        (Integer.parseInt(getdate.substring(11, 13)) == 15 && Integer.parseInt(getdate.substring(14, 16)) < 45)) {
                    Log.d("교시", "7교시");
                    searchTimetableDB(day, "seventh", uri, getdate.substring(0, 10));
                } else if (classcount >= 8 && (Integer.parseInt(getdate.substring(11, 13)) == 15 && Integer.parseInt(getdate.substring(14, 16)) >= 45) ||
                        (Integer.parseInt(getdate.substring(11, 13)) == 16 && Integer.parseInt(getdate.substring(14, 16)) < 40)) {
                    Log.d("교시", "8교시");
                    searchTimetableDB(day, "eighth", uri, getdate.substring(0, 10));
                }else {
                    Toast.makeText(this, "Not at School", Toast.LENGTH_SHORT).show();
                }
                break;
            case 40:
                if (classcount >= 1 && (Integer.parseInt(getdate.substring(11, 13)) == 8 && Integer.parseInt(getdate.substring(14, 16)) >= 40) ||
                        (Integer.parseInt(getdate.substring(11, 13)) == 9 && Integer.parseInt(getdate.substring(14, 16)) < 30)) {
                    Log.d("교시", "1교시");
                    searchTimetableDB(day, "first", uri, getdate.substring(0, 10));
                } else if (classcount >= 2 && (Integer.parseInt(getdate.substring(11, 13)) == 9 && Integer.parseInt(getdate.substring(14, 16)) >= 30) ||
                        (Integer.parseInt(getdate.substring(11, 13)) == 10 && Integer.parseInt(getdate.substring(14, 16)) < 20)) {
                    Log.d("교시", "2교시");
                    searchTimetableDB(day, "second", uri, getdate.substring(0, 10));
                } else if (classcount >= 3 && (Integer.parseInt(getdate.substring(11, 13)) == 10 && Integer.parseInt(getdate.substring(14, 16)) >= 20) ||
                        (Integer.parseInt(getdate.substring(11, 13)) == 11 && Integer.parseInt(getdate.substring(14, 16)) < 10)) {
                    Log.d("교시", "3교시");
                    searchTimetableDB(day, "third", uri, getdate.substring(0, 10));
                } else if (classcount >= 4 && (Integer.parseInt(getdate.substring(11, 13)) == 11 && Integer.parseInt(getdate.substring(14, 16)) >= 10) ||
                        (Integer.parseInt(getdate.substring(11, 13)) == 12 && Integer.parseInt(getdate.substring(14, 16)) < 0)) {
                    Log.d("교시", "4교시");
                    searchTimetableDB(day, "forth", uri, getdate.substring(0, 10));
                } else if (classcount >= 5 && (Integer.parseInt(getdate.substring(11, 13)) == 12 && Integer.parseInt(getdate.substring(14, 16)) >= 0) ||
                        (Integer.parseInt(getdate.substring(11, 13)) == 12 && Integer.parseInt(getdate.substring(14, 16)) < 50)) {
                    Log.d("교시", "5교시");
                    searchTimetableDB(day, "fifth", uri, getdate.substring(0, 10));
                } else if (classcount >= 6 && (Integer.parseInt(getdate.substring(11, 13)) == 13 && Integer.parseInt(getdate.substring(14, 16)) >= 30) ||
                        (Integer.parseInt(getdate.substring(11, 13)) == 14 && Integer.parseInt(getdate.substring(14, 16)) < 20)) {
                    Log.d("교시", "6교시");
                    searchTimetableDB(day, "sixth", uri, getdate.substring(0, 10));
                } else if (classcount >= 7 && (Integer.parseInt(getdate.substring(11, 13)) == 14 && Integer.parseInt(getdate.substring(14, 16)) >= 20) ||
                        (Integer.parseInt(getdate.substring(11, 13)) == 15 && Integer.parseInt(getdate.substring(14, 16)) < 10)) {
                    Log.d("교시", "7교시");
                    searchTimetableDB(day, "seventh", uri, getdate.substring(0, 10));
                } else if (classcount >= 8 && (Integer.parseInt(getdate.substring(11, 13)) == 15 && Integer.parseInt(getdate.substring(14, 16)) >= 10) ||
                        (Integer.parseInt(getdate.substring(11, 13)) == 16 && Integer.parseInt(getdate.substring(14, 16)) < 0)) {
                    Log.d("교시", "8교시");
                    searchTimetableDB(day, "eighth", uri, getdate.substring(0, 10));
                }else {
                    Toast.makeText(this, "Not at School", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    // 시간표 DB에서 검색하고 사진 DB에 넣기
    public void searchTimetableDB(int day, String classtime, Uri uri, String date) {
        // 시간표 DB 검색
        Cursor cursor = timetableDb.rawQuery("select "+classtime+" from TIMETABLE where day = "+day+";", null);
        cursor.moveToNext();
        Log.d("subject", cursor.getString(0));

        //사진 DB에 넣기
        Bitmap image;
        try {
            image = BitmapFactory.decodeStream(getContentResolver().openInputStream(uri));
            final byte[] imageInByte = getBytes(image);
            if(cursor.getString(0) != null){
                ContentValues cv = new  ContentValues();
                cv.put("subject", cursor.getString(0));
                cv.put("image_data", imageInByte);
                cv.put("image_date", date);
                pictureDB.insert( "picture_data", null, cv);
            }
                //pictureDB.execSQL("insert into picture_data (subject, image_data, image_date) values ('"+cursor.getString(0)+"', "+imageInByte+", '"+date+"');");
        }catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static byte[] getBytes(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 0, stream);
        return stream.toByteArray();
    }

    public int getDateDay(String year, String month, String days, String dateType) throws Exception {

        String date = year + "-" + month + "-" + days;
        int day = 0;

        SimpleDateFormat dateFormat = new SimpleDateFormat(dateType);
        Date nDate = dateFormat.parse(date);

        Calendar cal = Calendar.getInstance();
        cal.setTime(nDate);
        int dayNum = cal.get(Calendar.DAY_OF_WEEK);

        switch (dayNum) {
            case 1:
                break;
            case 2:
                day = 1;//월
                break;
            case 3:
                day = 2;
                break;
            case 4:
                day = 3;
                break;
            case 5:
                day = 4;
                break;
            case 6:
                day = 5;//금
                break;
            case 7:
                break;

        }
        return day;
    }

    public int dateTOint(int y, int m, int d)//입력된 날짜를 일수로 변환
    {
        int idate, upcount = 0;
        for (int i = 1; i < y; i++) {
            if ((0 == (i % 4) && 0 != (i % 100)) || 0 == i % 400)
                upcount++;
        }
        idate = y * 365 + upcount;
        switch (m) {
            case 12:
                idate += 31;
            case 11:
                idate += 30;
            case 10:
                idate += 31;
            case 9:
                idate += 30;
            case 8:
                idate += 31;
            case 7:
                idate += 31;
            case 6:
                idate += 30;
            case 5:
                idate += 31;
            case 4:
                idate += 30;
            case 3:
                idate += 31;
            case 2:
                if ((0 == (y % 4) && 0 != (y % 100)) || 0 == y % 400)
                    idate += 29;
                else
                    idate += 28;
            case 1:
                idate += 31;

        }
        idate += d;
        return idate;
    }
}