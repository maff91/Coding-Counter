package com.maff.codingcounter.ui;

import com.maff.codingcounter.data.Period;

import java.util.HashMap;
import java.util.Map;

public class UiStrings
{
    // Labels for different time periods
    public static final Map<Period, String> PERIOD_LABELS = new HashMap<>();

    static {
        PERIOD_LABELS.put(Period.TODAY, "Today");
        PERIOD_LABELS.put(Period.WEEK, "This Week");
        PERIOD_LABELS.put(Period.MONTH, "This Month");
        PERIOD_LABELS.put(Period.ALL_TIME, "All Time");
    }

    // Labels for different counters
    public static String LABEL_STAT_TYPE = "Chars typed";
    public static String LABEL_STAT_BACK_DEL = "Backspace/Del pressed";
    public static String LABEL_STAT_BACK_IMMEDIATE = "Backspace corrections";
    public static String LABEL_STAT_CUT = "Chars cut";
    public static String LABEL_STAT_PASTE = "Chars pasted";
    public static String LABEL_STAT_REMOVE = "Total chars removed";
    public static String LABEL_STAT_INSERTED = "Total chars added";

    // Table columns labels
    public static final String COLUMN_LABEL = "Counter";
    public static final String COLUMN_VALUE = "Value";

    public static final String WARNING_UI_PERIOD = "Pay attention: UI updates every 5 sec!";
}
