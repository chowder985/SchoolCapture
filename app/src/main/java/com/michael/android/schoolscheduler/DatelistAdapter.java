package com.michael.android.schoolscheduler;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

public class DatelistAdapter extends CursorAdapter implements View.OnClickListener{
    public DatelistAdapter(Context context, Cursor c) {
        super(context, c);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        final TextView start = view.findViewById(R.id.startitem);
        final TextView end = view.findViewById(R.id.enditem);
        final TextView hipen = view.findViewById(R.id.hipen);
        final TextView count = view.findViewById(R.id.countitem);
        final TextView time = view.findViewById(R.id.timeitem);
        hipen.setVisibility(View.INVISIBLE);
        end.setVisibility(View.INVISIBLE);

        start.setText(cursor.getString(cursor.getColumnIndex("dates")));
        time.setText(cursor.getString(cursor.getColumnIndex("classtime")) + "분 수업");
        count.setText(cursor.getString(cursor.getColumnIndex("count")) + "교시  ");

    }


    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View v = inflater.inflate(R.layout.datelistitem, parent, false);
        return v;
    }

    @Override
    public void onClick(View v) {

    }
}
