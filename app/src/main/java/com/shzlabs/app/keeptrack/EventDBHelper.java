package com.shzlabs.app.keeptrack;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.shzlabs.app.keeptrack.EventContract.Event;
import com.shzlabs.app.keeptrack.EventContract.EventLog;
import com.shzlabs.app.keeptrack.model.EventLogModel;

import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by personal on 22-11-2015.
 */
public class EventDBHelper extends SQLiteOpenHelper{

    public static final String TAG = EventDBHelper.class.getSimpleName();

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME  = "keeptrack.db";

    public static final String SQL_CREATE_EVENT_TABLE =
            "CREATE TABLE " + Event.TABLE_NAME + "(" +
                    Event._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    Event.COLUMN_NAME_EVENT_NAME + " TEXT," +
                    Event.COLUMN_NAME_LAST_CHECKIN + " INTEGER," +
                    Event.COLUMN_NAME_INSERT_DATE + " INTEGER," +
                    Event.COLUMN_NAME_MAX_DAYS + " INTEGER )";

    public static final String SQL_CREATE_EVENT_LOG_TABLE =
            "CREATE TABLE " + EventLog.TABLE_NAME + "(" +
                    EventLog._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    EventLog.COLUMN_NAME_EVENT_ID + " INTEGER NOT NULL, " +
                    EventLog.COLUMN_NAME_CHECK_DATE + " INTEGER, " +
                    EventLog.COLUMN_NAME_INSERT_DATE + " INTEGER )";

    public static final String SQL_DELETE_EVENT_TABLE =
            "DROP TABLE IF EXISTS" + Event.TABLE_NAME;

    public static final String SQL_DELETE_EVENT_LOG_TABLE =
            "DROP TABLE IF EXISTS" + EventLog.TABLE_NAME;



    public EventDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_EVENT_TABLE);
        db.execSQL(SQL_CREATE_EVENT_LOG_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DELETE_EVENT_TABLE);
        db.execSQL(SQL_DELETE_EVENT_LOG_TABLE);
    }

    public long addNewEvent(String name, int maxDays){
        // NOTE: maxDays not used currently, will be required for features yet to be added.
        ContentValues values = new ContentValues();
        values.put(Event.COLUMN_NAME_EVENT_NAME, name);
        values.put(Event.COLUMN_NAME_MAX_DAYS, maxDays);
        values.put(Event.COLUMN_NAME_LAST_CHECKIN, "0");
        Calendar calendar = Calendar.getInstance();
        values.put(Event.COLUMN_NAME_INSERT_DATE, calendar.getTimeInMillis());
        long writeCatch = getWritableDatabase().insert(Event.TABLE_NAME, null, values);
        Log.d(TAG, "addNewEvent DB write status: " + writeCatch);
        return writeCatch;
    }

    public long addNewEventLogEntry(int eventID, long checkInTime){
        ContentValues values = new ContentValues();
        values.put(EventLog.COLUMN_NAME_EVENT_ID, eventID);
        values.put(EventLog.COLUMN_NAME_CHECK_DATE, checkInTime);
        values.put(EventLog.COLUMN_NAME_INSERT_DATE, checkInTime);
        long writeCatch = getWritableDatabase().insert(EventLog.TABLE_NAME, null, values);
        Log.d(TAG, "addNewEventLog DB write status: " + writeCatch);
        return writeCatch;
    }

    public void checkIn(int ID, long timeInMillis){
        ContentValues values = new ContentValues();
        values.put(Event.COLUMN_NAME_LAST_CHECKIN, timeInMillis);
        // Kinda redundant and useless, needs to be modified
        long writeCatch = getWritableDatabase().update(Event.TABLE_NAME, values, Event._ID + " = '" + ID + "' ", null);
        Log.d(TAG, "Event CheckIn DB write status: " + writeCatch);
        addNewEventLogEntry(ID, timeInMillis);
    }

    public EventItemModel getEventDetails(int eventID){
        EventItemModel item = new EventItemModel();
        String query = "SELECT * FROM " + Event.TABLE_NAME + " WHERE " + Event._ID + " = '" + eventID + "' ";
        SQLiteDatabase db = getReadableDatabase();
        Cursor cr = db.rawQuery(query, null);
        if(cr.moveToFirst()){
            item.id = eventID;
            item.eventName = cr.getString(cr.getColumnIndex(Event.COLUMN_NAME_EVENT_NAME));
            item.maxLimitDays = cr.getInt(cr.getColumnIndex(Event.COLUMN_NAME_MAX_DAYS));
            item.insertDateInMillis = cr.getLong(cr.getColumnIndex(Event.COLUMN_NAME_INSERT_DATE));
            item.lastCheckInDateInMillis = getLatestCheckIn(item.id).checkInDate;
        }
        cr.close();
        return item;
    }

    public ArrayList<EventItemModel> getEventsList(){
        ArrayList<EventItemModel> itemList = new ArrayList<>();
        String query = "SELECT * FROM " + Event.TABLE_NAME;
        SQLiteDatabase db = getReadableDatabase();
        Cursor cr = db.rawQuery(query, null);
        if(cr.moveToFirst()){
            do{
                EventItemModel item = new EventItemModel();
                item.id = cr.getInt(cr.getColumnIndex(EventContract.Event._ID));
                item.eventName = cr.getString(cr.getColumnIndex(EventContract.Event.COLUMN_NAME_EVENT_NAME));
                item.maxLimitDays = cr.getInt(cr.getColumnIndex(EventContract.Event.COLUMN_NAME_MAX_DAYS));
                item.lastCheckInDateInMillis = getLatestCheckIn(item.id).checkInDate;
                item.insertDateInMillis = cr.getLong(cr.getColumnIndex(EventContract.Event.COLUMN_NAME_INSERT_DATE));

                itemList.add(item);
            }while(cr.moveToNext());
        }
        cr.close();
        return itemList;
    }

    public ArrayList<EventLogModel> getEventLog(int eventID){
        ArrayList<EventLogModel> itemList = new ArrayList<>();
        String query = "SELECT * FROM " + EventLog.TABLE_NAME + " WHERE " +
                EventLog.COLUMN_NAME_EVENT_ID + " = '" + eventID + "'" +
                "ORDER BY " + EventLog.COLUMN_NAME_CHECK_DATE + " DESC";
        SQLiteDatabase db = getReadableDatabase();
        Cursor cr = db.rawQuery(query, null);
        if(cr.moveToFirst()){
            do{
                EventLogModel item = new EventLogModel();
                item.id = cr.getInt(cr.getColumnIndex(EventLog._ID));
                item.eventID = cr.getInt(cr.getColumnIndex(EventLog.COLUMN_NAME_EVENT_ID));
                item.checkInDate = cr.getLong(cr.getColumnIndex(EventLog.COLUMN_NAME_CHECK_DATE));
                item.insertDate = cr.getLong(cr.getColumnIndex(EventLog.COLUMN_NAME_INSERT_DATE));

                itemList.add(item);
            }while(cr.moveToNext());
        }
        cr.close();
        return itemList;
    }

    public EventLogModel getEventLogDetails(int eventLogID){
        EventLogModel item = new EventLogModel();
        String query = "SELECT * FROM " + EventLog.TABLE_NAME + " WHERE " + EventLog._ID + " = '" +
                eventLogID + "' ";
        SQLiteDatabase db = getReadableDatabase();
        Cursor cr = db.rawQuery(query, null);
        if(cr.moveToFirst()){
            item.id = cr.getInt(cr.getColumnIndex(EventLog._ID));
            item.eventID = cr.getInt(cr.getColumnIndex(EventLog.COLUMN_NAME_EVENT_ID));
            item.checkInDate = cr.getLong(cr.getColumnIndex(EventLog.COLUMN_NAME_CHECK_DATE));
            item.insertDate = cr.getLong(cr.getColumnIndex(EventLog.COLUMN_NAME_INSERT_DATE));
        }
        cr.close();
        return item;
    }

    public EventLogModel getLatestCheckIn(int eventID){
        EventLogModel item = new EventLogModel();
        String query = "SELECT * FROM " + EventLog.TABLE_NAME + " WHERE " + EventLog.COLUMN_NAME_EVENT_ID + " = '" +
                eventID + "' ORDER BY " + EventLog.COLUMN_NAME_CHECK_DATE + " DESC";
        SQLiteDatabase db = getReadableDatabase();
        Cursor cr = db.rawQuery(query, null);
        if(cr.moveToFirst()){
            item.id = cr.getInt(cr.getColumnIndex(EventLog._ID));
            item.eventID = cr.getInt(cr.getColumnIndex(EventLog.COLUMN_NAME_EVENT_ID));
            item.checkInDate = cr.getLong(cr.getColumnIndex(EventLog.COLUMN_NAME_CHECK_DATE));
            item.insertDate = cr.getLong(cr.getColumnIndex(EventLog.COLUMN_NAME_INSERT_DATE));
        }
        cr.close();
        return item;
    }

    public int updateEventItem(int ID, String name){
        SQLiteDatabase db = getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        contentValues.put(Event.COLUMN_NAME_EVENT_NAME, name);

        return db.update(Event.TABLE_NAME, contentValues, Event._ID + " = '" + ID + "' ", null);
    }

    public int updateEventLogEntry(int ID, long checkDate){
        SQLiteDatabase db = getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        contentValues.put(EventLog.COLUMN_NAME_CHECK_DATE, checkDate);

        return db.update(EventLog.TABLE_NAME, contentValues, EventLog._ID + " = '" + ID + "' ", null);
    }

    public void deleteEvent(int ID){
        getWritableDatabase().delete(Event.TABLE_NAME, Event._ID + " = '" + ID + "' ", null);
    }
    public void deleteEventLogEntry(int ID){
        getWritableDatabase().delete(EventLog.TABLE_NAME, EventLog._ID + " = '" + ID + "' ", null);
    }
}
