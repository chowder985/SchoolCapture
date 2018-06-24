package com.michael.android.schoolscheduler;

import android.Manifest;
import android.content.ClipData;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ExifInterface;
import android.net.Uri;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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


    final static int breaktime = 10;
    final static int lunchtime = 50;


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

        subjectList = (ListView) findViewById(R.id.list);
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
                        while (c2.moveToNext()) {
                            for (int k = 2; k <= 9; k++) {
                                if (c2.getString(k) != null && c2.getString(k).equals(subjects.get(position))) {
                                    timetableDB.update(c2.getInt(1), k - 1, "");
                                }
                            }
                        }
                        c2.moveToFirst();

                        pictureDB.execSQL("delete from picture_data where subject = '" + subjects.get(position) + "';");

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
                if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.MANAGE_DOCUMENTS}, 1);
                } else {
                    Intent intent = new Intent();
                    intent.setType("image/*");
                    intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                    intent.setAction(Intent.ACTION_GET_CONTENT);
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
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Intent intent = new Intent();
                    intent.setType("image/*");
                    intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    startActivityForResult(Intent.createChooser(intent, "Select Picture"), 0);
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

                    Toast.makeText(this, displayName, Toast.LENGTH_SHORT).show();

                    setImageonDB("/storage/emulated/0/DCIM/Camera/" + displayName);

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
                            setImageonDB("/storage/emulated/0/DCIM/Camera/" + displayName);
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

    public void setImageonDB(String loot) throws Exception//통합 사진저장메소드
    {
        int m, y, d;
        int takenh, takenm, takens;
        int day = 0, classcount = 7, classtime = 50;
        String thatsubject="";
        //날짜받음
        ExifInterface exif = null;
        try {
            exif = new ExifInterface(loot);
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "EXIF LOADING ERROR", Toast.LENGTH_SHORT).show();
        }
        String getdate = exif.getAttribute(ExifInterface.TAG_DATETIME);
        Toast.makeText(this, getdate, Toast.LENGTH_SHORT).show();
        String s[] = getdate.split(" ");//format 2018:06:23 20:13:21
        String date[] = s[0].split(":");//날짜 분리저장
        y = Integer.parseInt(date[0]);
        m = Integer.parseInt(date[1]);
        d = Integer.parseInt(date[2]);
        String time[] = s[1].split(":");//시간 분리저장
        takenh = Integer.parseInt(time[0]);
        takenm = Integer.parseInt(time[1]);
        takens = Integer.parseInt(time[2]);

        day = getDateDay(date[0], date[1], date[2], "yyyy-M-dd");//요일변수 세팅
        if (day == 0) {
            Toast.makeText(this, "시간표에 지정되지 않은 시간에 촬영된 사진입니다", Toast.LENGTH_SHORT).show();
            return;
        }
        Log.d("DAY", Integer.toString(day));
        Log.d("DAY", loot);

        int search = dateTOint(y, m, d);
        ExceptionDB exceptionDB = new ExceptionDB(getApplicationContext());
        boolean def = exceptionDB.overlap(search);//예외처리DB검색
        if (def)//예외처리된 날짜일경우
        {
            String exception = exceptionDB.getResult(search);
            String exval[] = exception.split("-");
            classcount = Integer.parseInt(exval[1]);//교시 시간변수 세팅
            classtime = Integer.parseInt(exval[1]);
        }

        for (int i = 1; i <= classcount; i++) {//해당요일 / 교시의 과목이름 불러오기
            int takentime = takenh * 10000 + takenm * 100 + takens;//요일,교시,시간변수참조하여 시간표DB에서 과목검색
            if (getstarttime(i, classtime) < takentime && getendtime(i, classtime) >= takentime)//찍은시간이 i교시이면// 종료시각은 다음시간 시작시간과 같음
            {
                TimetableDB timetableDB = new TimetableDB(this);
                thatsubject = timetableDB.getsubjectname(day, i);
            }
        }

        Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), Uri.parse(loot));//DB삽입용 이미지 변환
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        byte[] data = stream.toByteArray();

        PictureDBHelper pictureDBHelper = new PictureDBHelper(this);
        pictureDBHelper.insert(data, day, thatsubject);//사진DB에 추가

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

    public static int getstarttime(int n, int runtime)//n교시의 시작하는 시간
    {
        //set form hhmmss
        int h = 8;
        int m = 40;
        int result = 0;
        m += (runtime * (n - 1)) + (breaktime * (n - 1));
        if (n >= 5) {
            m += lunchtime - 10;
        }
        if (m > 59) {
            h = h + m / 60;
            m = m % 60;
        }
        result = h * 10000 + m * 100;
        return result;
    }

    public static int getendtime(int n, int runtime)//n교시의 끝나는 시간
    {
        //set form hhmmss
        int h = 8;
        int m = 40;
        int result = 0;
        m += runtime * n + breaktime * n;
        if (n >= 5) {
            m += lunchtime - 10;
        }
        if (m > 59) {
            h = h + m / 60;
            m = m % 60;
        }
        result = h * 10000 + m * 100;
        return result;
    }


}