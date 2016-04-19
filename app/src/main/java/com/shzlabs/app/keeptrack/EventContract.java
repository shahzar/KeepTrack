package com.shzlabs.app.keeptrack;

import android.provider.BaseColumns;

/**
 * Created by personal on 22-11-2015.
 */
public final class EventContract {
    public EventContract(){}

    public static abstract class Event implements BaseColumns{
        public static final String TABLE_NAME = "event";
        public static final String COLUMN_NAME_EVENT_NAME = "name";
        public static final String COLUMN_NAME_MAX_DAYS = "max_days";
        public static final String COLUMN_NAME_LAST_CHECKIN = "last_checkin";
        public static final String COLUMN_NAME_INSERT_DATE = "insert_date";
    }

    public static abstract class EventLog implements BaseColumns{
        public static final String TABLE_NAME = "event_log";
        public static final String COLUMN_NAME_EVENT_ID = "event_id";
        public static final String COLUMN_NAME_CHECK_DATE = "check_date";
        public static final String COLUMN_NAME_INSERT_DATE = "insert_date";
    }

}
