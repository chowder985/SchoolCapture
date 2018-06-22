package com.michael.android.schoolscheduler;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class ExceptionHandler extends AppCompatActivity implements View.OnClickListener {

    Button start, end, save;
    Spinner count;
    RadioGroup time;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exception_handler);
        start = findViewById(R.id.startdate);
        end = findViewById(R.id.enddate);
        save = findViewById(R.id.saveexception);
        count = findViewById(R.id.classcountselect);
        time = findViewById(R.id.classtimeselect);

        start.setOnClickListener(this);
        end.setOnClickListener(this);
        save.setOnClickListener(this);

        long now = System.currentTimeMillis();
        Date date = new Date(now);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-M-dd");
        String getTime = sdf.format(date);
        start.setText(getTime);
        end.setText(getTime);
        count.setSelection(6);//7교시로 미리설정

    }


    @Override
    public void onClick(View v) {
        if (v == start) {
            String input = start.getText().toString();
            String[] inputs = input.split("-");

            DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                    new DatePickerDialog.OnDateSetListener() {
                        @Override
                        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                            int c1, c2;
                            c1 = dateTOint(end.getText().toString());
                            c2 = dateTOint(year, monthOfYear + 1, dayOfMonth);
                            if (c1 < c2) {//종료되는 날짜보다 이후이면
                                end.setText(year + "-" + (monthOfYear + 1) + "-" + dayOfMonth);
                                Toast.makeText(ExceptionHandler.this, "시작날짜는 종료날짜보다 이전이어야 합니다.", Toast.LENGTH_SHORT).show();
                            }
                            start.setText(year + "-" + (monthOfYear + 1) + "-" + dayOfMonth);

                        }
                    }, Integer.parseInt(inputs[0]), Integer.parseInt(inputs[1]) - 1, Integer.parseInt(inputs[2]));//지정된 날짜부터 시작
            datePickerDialog.show();

        } else if (v == end) {
            String input = end.getText().toString();
            String[] inputs = input.split("-");
            DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                    new DatePickerDialog.OnDateSetListener() {
                        @Override
                        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                            int c1, c2;
                            c1 = dateTOint(start.getText().toString());
                            c2 = dateTOint(year, monthOfYear + 1, dayOfMonth);
                            if (c1 > c2) {//시작되는 날짜보다 이전이면
                                start.setText(year + "-" + (monthOfYear + 1) + "-" + dayOfMonth);
                                Toast.makeText(ExceptionHandler.this, "종료날짜는 시작날짜보다 이후어야 합니다.", Toast.LENGTH_SHORT).show();
                            }
                            end.setText(year + "-" + (monthOfYear + 1) + "-" + dayOfMonth);
                        }
                    }, Integer.parseInt(inputs[0]), Integer.parseInt(inputs[1]) - 1, Integer.parseInt(inputs[2]));
            datePickerDialog.show();

        } else if (v == save) {

            ExceptionDB exceptionDB = new ExceptionDB(this);

            int startdate = 0, enddate = 0;
            int classcount = 0, classtime = 0;
            classcount = count.getSelectedItemPosition() + 1;
            int id = time.getCheckedRadioButtonId();
            if (id == R.id.forteen) {
                classtime = 40;
            } else if (id == R.id.fortifive) {
                classtime = 45;
            } else if (id == R.id.fifthin) {
                classtime = 50;
            }
            startdate = dateTOint(start.getText().toString());
            enddate = dateTOint(end.getText().toString());

            String insertdate = "not cleared";
            int i = startdate;
            boolean check;
            do{
                check = exceptionDB.overlap(i);
                insertdate = intTOstring(i);
                if(!check)
                    exceptionDB.insert(i, classcount, classtime, insertdate);

                else if(check)
                    exceptionDB.update(i, classcount, classtime, insertdate);
                i+=1;
            }while(i<=enddate);

            Toast.makeText(this, "Exception updated", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    public int dateTOint(String Sdate) {
        String[] YMD = Sdate.split("-");
        int y, m, d;
        y = Integer.parseInt(YMD[0]);
        m = Integer.parseInt(YMD[1]);
        d = Integer.parseInt(YMD[2]);
        return dateTOint(y, m, d);

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

    public String intTOstring(int date) {
        String y, m = "not cleared", d = "not cleared";
        int yearcount = 1;

        while (true) {
            if ((0 == (yearcount % 4) && 0 != (yearcount % 100)) || 0 == yearcount % 400)
                date--;
            yearcount += 1;
            date -= 365;
            if (date < 365) {
                break;
            }
        }

        y = Integer.toString(yearcount-1);

        int defind = 0;
        for (int i = 0; i <= 11; i++) {
            if (date <= 31) {
                m = Integer.toString(0);
                break;
            }
            if (i == 1) {
                if ((0 == (Integer.parseInt(y) % 4) && 0 != (Integer.parseInt(y) % 100)) || 0 == Integer.parseInt(y) % 400) {
                    date -= 31;
                    defind = 29;
                } else {
                    date -= 31;
                    defind = 28;
                }
            } else if (i == 2) {
                if ((0 == (Integer.parseInt(y) % 4) && 0 != (Integer.parseInt(y) % 100)) || 0 == Integer.parseInt(y) % 400) {
                    date -= 29;
                    defind = 31;
                } else {
                    date -= 28;
                    defind = 31;
                }
            } else if (i == 3) {
                date -= 31;
                defind = 30;
            } else if (i == 4) {
                date -= 30;
                defind = 31;
            } else if (i == 5) {
                date -= 31;
                defind = 30;
            } else if (i == 6) {
                date -= 30;
                defind = 31;
            } else if (i == 7) {
                date -= 31;
                defind = 31;
            } else if (i == 8) {
                date -= 31;
                defind = 30;
            } else if (i == 9) {
                date -= 30;
                defind = 31;
            } else if (i == 10) {
                date -= 31;
                defind = 30;
            } else if (i == 11) {
                date -= 30;
                defind = 31;
            }
            if (date <= defind) {
                m = Integer.toString(i);
                d = Integer.toString(date);
                break;
            }
        }
        return y + "-" + m + "-" + d;
    }

}
