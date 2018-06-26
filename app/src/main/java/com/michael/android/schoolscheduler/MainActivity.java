package com.michael.android.schoolscheduler;

import android.Manifest;
import android.content.ClipData;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
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

    List<String> imagesEncodedList;
    final static int breaktime = 10;
    final static int lunchtime = 50;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.my_toolbar);
        toolbar.setTitleTextColor(Color.parseColor("#ffffff"));
        setSupportActionBar(toolbar);

        SubjectDB helper = new SubjectDB(this);
        db = helper.getWritableDatabase();

        subjectList = (ListView) findViewById(R.id.list);
        timetableDB = new TimetableDB(this);
        timetableDb = timetableDB.getWritableDatabase();

        final PictureDBHelper pictureDBHelper = new PictureDBHelper(this);
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
                        PictureDBHelper delter = new PictureDBHelper(getApplicationContext());
                        SQLiteDatabase del = delter.getWritableDatabase();
                        //pictureDB.execSQL("delete from picture_data where subject = '" + subjects.get(position) + "';");
                        Cursor delete = del.rawQuery("select * from picture_data", null);
                        while(delete.moveToNext())
                        {
                            if(delete.getString(1).equals(subjects.get(position)))
                            {
                                delter.delete(delete.getString(2));
                            }
                        }

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

                if ((ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) &&
                        (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)) {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                } else {
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
                imagesEncodedList = new ArrayList<String>();
                if (data.getData() != null) {//사진하나
                    Uri mImageUri = data.getData();
                    String path = getRealPathFromURI_API19(this, mImageUri);
                    Log.d("REAL2",path);
                    setImageonDB(path, mImageUri);

                } else {//사진여러개
                    if (data.getClipData() != null) {
                        ClipData mClipData = data.getClipData();
                        ArrayList<Uri> mArrayUri = new ArrayList<Uri>();
                        for (int i = 0; i < mClipData.getItemCount(); i++) {
                            ClipData.Item item = mClipData.getItemAt(i);
                            Uri uri = item.getUri();
                            mArrayUri.add(uri);
                            String path = getRealPathFromURI_API19(this, mArrayUri.get(i));
                            Log.d("REAL2",path);
                            setImageonDB(path, mArrayUri.get(i));
                        }
                    }
                }
            } else {//이미지를 고르지 않으면
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "로드할 수 없는 이미지가 있습니다", Toast.LENGTH_LONG)
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
        //loot input format /storage/emulated/0/DCIM/Camera/20180611_161245.jpg
        int m, y, d;
        int takenh, takenm, takens;
        int day, classcount = 7, classtime = 50, classposition=0;
        String thatsubject = "";
        String getdate = "";
        int orientation = -28;
        //날짜받음
        ExifInterface exif;
        try {
            exif = new ExifInterface(loot);
            Log.d("LOCATION", loot);
            getdate = exif.getAttribute(ExifInterface.TAG_DATETIME);
            orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);
           if(getdate==null||orientation==-28)
           {
               //Toast.makeText(this, "unable to load EXIF", Toast.LENGTH_SHORT).show();
               return;
           }

        } catch (IOException e) {
            e.printStackTrace();
            //Toast.makeText(this, "EXIF LOADING ERROR", Toast.LENGTH_SHORT).show();
            return;
        }

        String s[] = getdate.split(" ");//format 2018:06:23 20:13:21//날짜 시간 분리
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
            Toast.makeText(this, "시간표에 지정되지 않은 날짜에 촬영된 사진입니다", Toast.LENGTH_SHORT).show();
            return;
        }
        int search = dateTOint(y, m, d);
        ExceptionDB exceptionDB = new ExceptionDB(getApplicationContext());
        boolean def = exceptionDB.overlap(search);//예외처리DB검색
        if (def)//예외처리된 날짜일경우
        {
            String exception = exceptionDB.getResult(search);
            String exval[] = exception.split("-");
            classcount = Integer.valueOf(exval[1]);//교시 시간변수 세팅
            classtime = Integer.valueOf(exval[2]);
        }
        boolean seetted = false;
        for (int i = 1; i <= classcount; i++) {//해당요일 / 교시의 과목이름 불러오기
            int takentime = takenh * 10000 + takenm * 100 + takens;//요일,교시,시간변수참조하여 시간표DB에서 과목검색
            if (getstarttime(i, classtime) < takentime && getendtime(i, classtime) >= takentime)//찍은시간이 i교시이면// 종료시각은 다음시간 시작시간과 같음
            {
                TimetableDB timetableDB = new TimetableDB(this);
                thatsubject = timetableDB.getsubjectname(day, i);
                classposition = i;
                timetableDB.close();
                seetted = true;
            }
        }
        if(seetted)
            searchTimetableDB(thatsubject, uri, s[0], orientation, loot, classposition);//사진DB에 추가
        exceptionDB.close();
    }

    public void searchTimetableDB(@NonNull String subject, Uri uri, String date, int orientation, String path, int classtime) {//
        Bitmap image;
        try {
            Log.d("SUBJECT",subject);
            Toast.makeText(this, subject, Toast.LENGTH_SHORT).show();
            String inputpath[] = path.split("/");
            String imagename = inputpath[inputpath.length-1];
            String savepath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/.imagedatas/";
            image = BitmapFactory.decodeStream(getContentResolver().openInputStream(uri));
            Bitmap rotated = rotateBitmap(image, orientation);
            PictureDBHelper pictureDBHelper = new PictureDBHelper(this);
            File file_path;
            try {
                file_path = new File(savepath);
                if (!file_path.isDirectory()) {
                    file_path.mkdirs();
                }
                FileOutputStream out = new FileOutputStream(savepath + imagename);
                BufferedOutputStream bos = new BufferedOutputStream(out);
                rotated.compress(Bitmap.CompressFormat.JPEG, 100, bos);
                SQLiteDatabase db = pictureDBHelper.getWritableDatabase();
                // DB에 입력한 값으로 행 추가
                Cursor c1 = db.rawQuery("select * from picture_data", null);
                while (c1.moveToNext()) {
                    if (c1.getString(2).equals(savepath + imagename)) {
                        Toast.makeText(this, "이미 추가된 사진입니다", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
                pictureDBHelper.insert(savepath + imagename, date, subject, classtime);
                bos.close();
                out.close();
                pictureDBHelper.close();

            } catch (FileNotFoundException exception) {
                Log.e("FileNotFoundException", exception.getMessage());
            } catch (IOException exception) {
                Log.e("IOException", exception.getMessage());
            }
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
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

    public static int dateTOint(int y, int m, int d)//입력된 날짜를 일수로 변환
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

    public static Bitmap rotateBitmap(Bitmap bitmap, int orientation) {
        Matrix matrix = new Matrix();
        switch (orientation) {
            case ExifInterface.ORIENTATION_NORMAL:
                return bitmap;
            case ExifInterface.ORIENTATION_FLIP_HORIZONTAL:
                matrix.setScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                matrix.setRotate(180);
                break;
            case ExifInterface.ORIENTATION_FLIP_VERTICAL:
                matrix.setRotate(180);
                matrix.postScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_TRANSPOSE:
                matrix.setRotate(90);
                matrix.postScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_ROTATE_90:
                matrix.setRotate(90);
                break;
            case ExifInterface.ORIENTATION_TRANSVERSE:
                matrix.setRotate(-90);
                matrix.postScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_ROTATE_270:
                matrix.setRotate(-90);
                break;
            default:
                return bitmap;
        }
        try {
            Bitmap bmRotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            bitmap.recycle();
            return bmRotated;
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String getRealPathFromURI_API19(final Context context, final Uri uri) {

        // check here to KITKAT or new version
        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {

            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/"
                            + split[1];
                }
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"),
                        Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[] { split[1] };

                return getDataColumn(context, contentUri, selection,
                        selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {

            // Return the remote address
            if (isGooglePhotosUri(uri))
                return uri.getLastPathSegment();

            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

    public static String getDataColumn(Context context, Uri uri,
                                       String selection, String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = { column };

        try {
            cursor = context.getContentResolver().query(uri, projection,
                    selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                final int index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri
                .getAuthority());
    }

    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri
                .getAuthority());
    }

    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri
                .getAuthority());
    }

    public static boolean isGooglePhotosUri(Uri uri) {
        return "com.google.android.apps.photos.content".equals(uri
                .getAuthority());
    }


}