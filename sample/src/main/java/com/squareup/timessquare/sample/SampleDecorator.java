package com.squareup.timessquare.sample;

import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.RelativeSizeSpan;
import com.squareup.timessquare.CalendarCellDecorator;
import com.squareup.timessquare.CalendarCellView;

import java.util.Calendar;
import java.util.Date;

public class SampleDecorator implements CalendarCellDecorator {
  @Override
  public void decorate(CalendarCellView cellView, Date date) {
    Calendar calendar = Calendar.getInstance();
    calendar.setTime(date);
    String dateString = Integer.toString(calendar.get(Calendar.DAY_OF_MONTH));
    SpannableString string = new SpannableString(dateString + "\ntitle");
    string.setSpan(new RelativeSizeSpan(0.5f), 0, dateString.length(),
        Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
    cellView.getDayOfMonthTextView().setText(string);
  }
}
