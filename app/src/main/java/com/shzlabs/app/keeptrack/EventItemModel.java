package com.shzlabs.app.keeptrack;

import java.util.ArrayList;
import java.util.Date;

/**
 * Created by personal on 22-11-2015.
 */
public class EventItemModel {
    int id;
    String eventName;
    ArrayList<occuranceItem> occurrenceList = new ArrayList<>();
    Integer maxLimitDays;
    long lastCheckInDateInMillis;
    long insertDateInMillis;
    Integer daysPast;

    public EventItemModel(){
        eventName = null;
        maxLimitDays = null;
        daysPast = null;
    }


    class occuranceItem{
        Date date;
        String occurrenceEvent;
    }
}
