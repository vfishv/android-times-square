package com.squareup.timessquare.sample;

import android.app.Activity;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.squareup.timessquare.CalendarPickerView;
import com.squareup.timessquare.MonthView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class DemoActivity extends Activity {

    private static final String TAG = "DemoActivity";

    private CalendarPickerView calendar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demo);

        calendar = (CalendarPickerView) findViewById(R.id.calendar_view);

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMinimum(Calendar.DAY_OF_MONTH));
        Date minDate = cal.getTime();
        printDate(minDate);

        int days =cal.getActualMaximum(Calendar.DAY_OF_MONTH);
        Log.e(TAG, "days: " + days);

        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH) + 1);
        Date maxDate = cal.getTime();

        printDate(maxDate);

        Date selectedDates = new Date();

        calendar.init(minDate,maxDate).inMode(CalendarPickerView
                .SelectionMode.SINGLE)
                .withSelectedDate(selectedDates);

        calendar.setOnInvalidDateSelectedListener(new CalendarPickerView.OnInvalidDateSelectedListener() {
            @Override
            public void onInvalidDateSelected(Date date) {
                Log.e(TAG, "onInvalidDateSelected: " + printDate(date));
            }
        });

        calendar.setOnDateSelectedListener(new CalendarPickerView.OnDateSelectedListener() {
            @Override
            public void onDateSelected(Date date) {
                Log.e(TAG, "onDateSelected: " + printDate(date));
            }

            @Override
            public void onDateUnselected(Date date) {
                Log.e(TAG, "onDateUnselected: " + printDate(date));
            }
        });

        MonthView monthView = (MonthView) findViewById(R.id.month_view);

        //MonthDescriptor month, List<java.util.List<MonthCellDescriptor>> cells;
        //boolean displayOnly, Typeface titleTypeface, android.graphics.Typeface dateTypeface;
        //monthView.init(null,null,false,null,null);


        findViewById(R.id.test).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                calendar.clearHighlightedDates();
                Calendar cal = Calendar.getInstance();
                cal.set(Calendar.DAY_OF_MONTH, cal.getActualMinimum(Calendar.DAY_OF_MONTH));
                Date minDate = cal.getTime();
                calendar.init(minDate,new Date()).inMode(CalendarPickerView
                        .SelectionMode.SINGLE);
                        //.withSelectedDate(minDate);
            }
        });

    }

    private String printDate(Date date)
    {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        String ret = df.format(date);
        Log.e(TAG, "date: "  + ret);
        return ret;
    }
}
