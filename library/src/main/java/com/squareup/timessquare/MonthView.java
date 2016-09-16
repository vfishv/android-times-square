// Copyright 2012 Square, Inc.
package com.squareup.timessquare;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static java.util.Calendar.DATE;
import static java.util.Calendar.DAY_OF_MONTH;
import static java.util.Calendar.DAY_OF_WEEK;
import static java.util.Calendar.MONTH;
import static java.util.Calendar.YEAR;

public class MonthView extends LinearLayout {
  Date date;
  View prev;
  View next;
  TextView title;
  CalendarGridView grid;
  private Listener listener;
  private List<CalendarCellDecorator> decorators;
  private boolean isRtl;
  private Locale locale;
  boolean displayOnly;

  public static MonthView create(ViewGroup parent, LayoutInflater inflater,
      DateFormat weekdayNameFormat, Listener listener, Calendar today, int dividerColor,
      int dayBackgroundResId, int dayTextColorResId, int titleTextColor, boolean displayHeader,
      int headerTextColor, Locale locale, DayViewAdapter adapter) {
    return create(parent, inflater, weekdayNameFormat, listener, today, dividerColor,
        dayBackgroundResId, dayTextColorResId, titleTextColor, displayHeader, headerTextColor, null,
        locale, adapter);
  }

  public static MonthView create(ViewGroup parent, LayoutInflater inflater,
      DateFormat weekdayNameFormat, Listener listener, Calendar today, int dividerColor,
      int dayBackgroundResId, int dayTextColorResId, int titleTextColor, boolean displayHeader,
      int headerTextColor, List<CalendarCellDecorator> decorators, Locale locale,
      DayViewAdapter adapter) {
    final MonthView view = (MonthView) inflater.inflate(R.layout.month, parent, false);
    view.setDayViewAdapter(adapter);
    view.setDividerColor(dividerColor);
    view.setDayTextColor(dayTextColorResId);
    view.setTitleTextColor(titleTextColor);
    view.setDisplayHeader(displayHeader);
    view.setHeaderTextColor(headerTextColor);

    if (dayBackgroundResId != 0) {
      view.setDayBackground(dayBackgroundResId);
    }

    final int originalDayOfWeek = today.get(Calendar.DAY_OF_WEEK);

    view.isRtl = isRtl(locale);
    view.locale = locale;
    int firstDayOfWeek = today.getFirstDayOfWeek();
    final CalendarRowView headerRow = (CalendarRowView) view.grid.getChildAt(0);
    for (int offset = 0; offset < 7; offset++) {
      today.set(Calendar.DAY_OF_WEEK, getDayOfWeek(firstDayOfWeek, offset, view.isRtl));
      final TextView textView = (TextView) headerRow.getChildAt(offset);
      String week = weekdayNameFormat.format(today.getTime());
      if(week!=null)
      {
        week = week.replace("周","");
      }
      textView.setText(week);
    }
    today.set(Calendar.DAY_OF_WEEK, originalDayOfWeek);
    view.listener = listener;
    view.decorators = decorators;
    return view;
  }

  private static int getDayOfWeek(int firstDayOfWeek, int offset, boolean isRtl) {
    int dayOfWeek = firstDayOfWeek + offset;
    if (isRtl) {
      return 8 - dayOfWeek;
    }
    return dayOfWeek;
  }

  private static boolean isRtl(Locale locale) {
    // TODO convert the build to gradle and use getLayoutDirection instead of this (on 17+)?
    final int directionality = Character.getDirectionality(locale.getDisplayName(locale).charAt(0));
    return directionality == Character.DIRECTIONALITY_RIGHT_TO_LEFT
        || directionality == Character.DIRECTIONALITY_RIGHT_TO_LEFT_ARABIC;
  }

  public MonthView(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public void setDecorators(List<CalendarCellDecorator> decorators) {
    this.decorators = decorators;
  }

  public List<CalendarCellDecorator> getDecorators() {
    return decorators;
  }

  @Override protected void onFinishInflate() {
    super.onFinishInflate();
    prev = findViewById(R.id.pre_month);
    next = findViewById(R.id.next_month);
    title = (TextView) findViewById(R.id.title);
    grid = (CalendarGridView) findViewById(R.id.calendar_grid);

    if(prev!=null)
    {
      prev.setOnClickListener(new OnClickListener() {
        @Override
        public void onClick(View v) {
          if(date!=null)
          {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            calendar.add(Calendar.MONTH, -1);
            date = calendar.getTime();
            //calendar.set(Calendar.DAY_OF_MONTH, 1);
            DateFormat monthNameFormat = new SimpleDateFormat(getResources().getString(R.string.month_name_format), locale);
            MonthDescriptor month =
                    new MonthDescriptor(calendar.get(MONTH), calendar.get(YEAR), date,
                            monthNameFormat.format(date));
            List<List<MonthCellDescriptor>> cells = getMonthCells(month, calendar);
            init(month,cells,displayOnly,null,null);
          }
        }
      });
    }

    if(next!=null)
    {
      next.setOnClickListener(new OnClickListener() {
        @Override
        public void onClick(View v) {
          Calendar calendar = Calendar.getInstance();
          calendar.setTime(date);
          calendar.add(Calendar.MONTH, 1);
          date = calendar.getTime();
          //calendar.set(Calendar.DAY_OF_MONTH, 1);
          DateFormat monthNameFormat = new SimpleDateFormat(getResources().getString(R.string.month_name_format), locale);
          MonthDescriptor month =
                  new MonthDescriptor(calendar.get(MONTH), calendar.get(YEAR), date,
                          monthNameFormat.format(date));
          List<List<MonthCellDescriptor>> cells = getMonthCells(month, calendar);
          init(month,cells,displayOnly,null,null);
        }
      });
    }

  }

  public void init(MonthDescriptor month, List<List<MonthCellDescriptor>> cells,
      boolean displayOnly, Typeface titleTypeface, Typeface dateTypeface) {
    Logr.d("Initializing MonthView (%d) for %s", System.identityHashCode(this), month);
    long start = System.currentTimeMillis();
    date = month.getDate();
    this.displayOnly = displayOnly;
    title.setText(month.getLabel());
    NumberFormat numberFormatter = NumberFormat.getInstance(locale);

    today = Calendar.getInstance(locale);
    minCal = Calendar.getInstance(locale);
    maxCal = Calendar.getInstance(locale);



    final int numRows = cells.size();
    grid.setNumRows(numRows);
    for (int i = 0; i < 6; i++) {
      CalendarRowView weekRow = (CalendarRowView) grid.getChildAt(i + 1);
      weekRow.setListener(listener);
      if (i < numRows) {
        weekRow.setVisibility(VISIBLE);
        List<MonthCellDescriptor> week = cells.get(i);
        for (int c = 0; c < week.size(); c++) {
          MonthCellDescriptor cell = week.get(isRtl ? 6 - c : c);
          CalendarCellView cellView = (CalendarCellView) weekRow.getChildAt(c);

          String cellDate = numberFormatter.format(cell.getValue());
          if (!cellView.getDayOfMonthTextView().getText().equals(cellDate)) {
            cellView.getDayOfMonthTextView().setText(cellDate);
          }
          cellView.setEnabled(cell.isCurrentMonth());
          cellView.setClickable(!displayOnly);

          cellView.setSelectable(cell.isSelectable());
          cellView.setSelected(cell.isSelected());
          cellView.setCurrentMonth(cell.isCurrentMonth());
          cellView.setToday(cell.isToday());
          cellView.setRangeState(cell.getRangeState());
          cellView.setHighlighted(cell.isHighlighted());
          cellView.setTag(cell);

          if (null != decorators) {
            for (CalendarCellDecorator decorator : decorators) {
              decorator.decorate(cellView, cell.getDate());
            }
          }
        }
      } else {
        weekRow.setVisibility(GONE);
      }
    }

    if (titleTypeface != null) {
      title.setTypeface(titleTypeface);
    }
    if (dateTypeface != null) {
      grid.setTypeface(dateTypeface);
    }

    Logr.d("MonthView.init took %d ms", System.currentTimeMillis() - start);
  }

  public void setDividerColor(int color) {
    grid.setDividerColor(color);
  }

  public void setDayBackground(int resId) {
    grid.setDayBackground(resId);
  }

  public void setDayTextColor(int resId) {
    grid.setDayTextColor(resId);
  }

  public void setDayViewAdapter(DayViewAdapter adapter) {
    grid.setDayViewAdapter(adapter);
  }

  public void setTitleTextColor(int color) {
    title.setTextColor(color);
  }

  public void setDisplayHeader(boolean displayHeader) {
    grid.setDisplayHeader(displayHeader);
  }

  public void setHeaderTextColor(int color) {
    grid.setHeaderTextColor(color);
  }

  public interface Listener {
    void handleClick(MonthCellDescriptor cell);
  }

  final List<Calendar> selectedCals = new ArrayList<>();
  final List<Calendar> highlightedCals = new ArrayList<>();
  Calendar today;
  private CalendarPickerView.DateSelectableFilter dateConfiguredListener;
  private Calendar minCal;
  private Calendar maxCal;

  List<List<MonthCellDescriptor>> getMonthCells(MonthDescriptor month, Calendar startCal) {
    Calendar cal = Calendar.getInstance(locale);
    cal.setTime(startCal.getTime());
    List<List<MonthCellDescriptor>> cells = new ArrayList<>();
    cal.set(DAY_OF_MONTH, 1);
    int firstDayOfWeek = cal.get(DAY_OF_WEEK);
    int offset = cal.getFirstDayOfWeek() - firstDayOfWeek;
    if (offset > 0) {
      offset -= 7;
    }
    cal.add(Calendar.DATE, offset);

    minCal.setTime(startCal.getTime());
    startCal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH) + 1);
    maxCal.setTime(startCal.getTime());

    Calendar minSelectedCal = minDate(selectedCals);
    Calendar maxSelectedCal = maxDate(selectedCals);

    while ((cal.get(MONTH) < month.getMonth() + 1 || cal.get(YEAR) < month.getYear()) //
            && cal.get(YEAR) <= month.getYear()) {
      Logr.d("Building week row starting at %s", cal.getTime());
      List<MonthCellDescriptor> weekCells = new ArrayList<>();
      cells.add(weekCells);
      for (int c = 0; c < 7; c++) {
        Date date = cal.getTime();
        boolean isCurrentMonth = cal.get(MONTH) == month.getMonth();
        boolean isSelected = isCurrentMonth && containsDate(selectedCals, cal);
        boolean isSelectable =
                isCurrentMonth && betweenDates(cal, minCal, maxCal) && isDateSelectable(date);
        boolean isToday = sameDate(cal, today);
        boolean isHighlighted = containsDate(highlightedCals, cal);
        int value = cal.get(DAY_OF_MONTH);

        MonthCellDescriptor.RangeState rangeState = MonthCellDescriptor.RangeState.NONE;
        if (selectedCals.size() > 1) {
          if (sameDate(minSelectedCal, cal)) {
            rangeState = MonthCellDescriptor.RangeState.FIRST;
          } else if (sameDate(maxDate(selectedCals), cal)) {
            rangeState = MonthCellDescriptor.RangeState.LAST;
          } else if (betweenDates(cal, minSelectedCal, maxSelectedCal)) {
            rangeState = MonthCellDescriptor.RangeState.MIDDLE;
          }
        }

        weekCells.add(
                new MonthCellDescriptor(date, isCurrentMonth, isSelectable, isSelected, isToday,
                        isHighlighted, value, rangeState));
        cal.add(DATE, 1);
      }
    }
    return cells;
  }

  private static boolean containsDate(List<Calendar> selectedCals, Calendar cal) {
    for (Calendar selectedCal : selectedCals) {
      if (sameDate(cal, selectedCal)) {
        return true;
      }
    }
    return false;
  }

  private static Calendar minDate(List<Calendar> selectedCals) {
    if (selectedCals == null || selectedCals.size() == 0) {
      return null;
    }
    Collections.sort(selectedCals);
    return selectedCals.get(0);
  }

  private static Calendar maxDate(List<Calendar> selectedCals) {
    if (selectedCals == null || selectedCals.size() == 0) {
      return null;
    }
    Collections.sort(selectedCals);
    return selectedCals.get(selectedCals.size() - 1);
  }

  private static boolean sameDate(Calendar cal, Calendar selectedDate) {
    return cal.get(MONTH) == selectedDate.get(MONTH)
            && cal.get(YEAR) == selectedDate.get(YEAR)
            && cal.get(DAY_OF_MONTH) == selectedDate.get(DAY_OF_MONTH);
  }

  private static boolean betweenDates(Calendar cal, Calendar minCal, Calendar maxCal) {
    final Date date = cal.getTime();
    return betweenDates(date, minCal, maxCal);
  }

  static boolean betweenDates(Date date, Calendar minCal, Calendar maxCal) {
    final Date min = minCal.getTime();
    return (date.equals(min) || date.after(min)) // >= minCal
            && date.before(maxCal.getTime()); // && < maxCal
  }

  private static boolean sameMonth(Calendar cal, MonthDescriptor month) {
    return (cal.get(MONTH) == month.getMonth() && cal.get(YEAR) == month.getYear());
  }

  private boolean isDateSelectable(Date date) {
    return dateConfiguredListener == null || dateConfiguredListener.isDateSelectable(date);
  }

}
