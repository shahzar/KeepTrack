package com.shzlabs.app.keeptrack;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.shzlabs.app.keeptrack.model.EventLogModel;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class EventDetailsFragment extends Fragment {

    View rootView;
    Context context;
    ListView eventIdListview;
    TextView lastCheckedEt, eventName;
    ArrayList<EventLogModel> eventLogItemList;
    EventItemModel currentEvent;
    EventDBHelper db;
    Button checkInButton, checkInOlderButton;
    int eventID;
    public String[] DAYS = {"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};
    public String[] MONTHS = {"JANUARY", "FEBRUARY", "MARCH", "APRIL", "MAY", "JUNE", "JULY", "AUGUST", "SEPTEMBER", "OCTOBER", "NOVEMBER", "DECEMBER"};

    public static EventDetailsFragment newInstance(int eventID) {

        Bundle args = new Bundle();
        args.putInt("event_id", eventID);

        EventDetailsFragment fragment = new EventDetailsFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_event_details, null);
        context = getActivity();
        db = new EventDBHelper(context);

        // Get Bundle values
        Bundle args = getArguments();
        eventID = args.getInt("event_id");

        eventLogItemList = new ArrayList<>();

        // Link Views
        eventIdListview = (ListView) rootView.findViewById(R.id.event_id_listview);
        lastCheckedEt = (TextView) rootView.findViewById(R.id.last_checked);
        eventName = (TextView) rootView.findViewById(R.id.event_name);
        checkInButton = (Button) rootView.findViewById(R.id.checkInIcon);
        checkInOlderButton = (Button) rootView.findViewById(R.id.checkInOlderDate);

        // Get event details from DB
        currentEvent = new EventItemModel();
        currentEvent = db.getEventDetails(eventID);

        // Get event log details from DB
        eventLogItemList = db.getEventLog(eventID);

        /* Set views with respective values */

        // Set event name
        eventName.setText(currentEvent.eventName);
//        if(eventLogItemList.get(0).checkInDate)
        lastCheckedEt.setText(MainListFragment.getTimeAgo(eventLogItemList.get(0).checkInDate));

        // Setup event log list
        final EventLogAdapter eventLogAdapter = new EventLogAdapter(eventLogItemList);
        eventIdListview.setAdapter(null);
        TextView listViewHeader = new TextView(context);
        listViewHeader.setPadding(10,10,0,10);
        listViewHeader.setTextColor(getResources().getColor(R.color.less_emphasized));
        listViewHeader.setTextSize(16);
        listViewHeader.setText("History");
        eventIdListview.addHeaderView(listViewHeader);
        eventIdListview.setAdapter(eventLogAdapter);

        /* Click Listeners */
        checkInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Add entry to DB and update list
                db.checkIn(eventID, System.currentTimeMillis());
                eventLogAdapter.updateList(db.getEventLog(eventID));
            }
        });

        checkInOlderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar cal = Calendar.getInstance();
                int mYear = cal.get(Calendar.YEAR);
                int mMonth = cal.get(Calendar.MONTH);
                int mDay = cal.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog dateDialog = new DatePickerDialog(context, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int yearInt, int monthOfYear, int dayOfMonth) {
                        if(view.isShown()){
                            // Fix date
                            monthOfYear += 1;

                            // Fix values by prepending required 0s
                            String day = String.format("%02d", dayOfMonth);
                            String month = String.format("%02d", monthOfYear);
                            String year = String.valueOf(yearInt);

                            // Take string date and convert to Millis
                            Date date = new Date();
                            DateFormat formatter = new SimpleDateFormat("dd/MM/yy", Locale.US);
                            try {
                                date = formatter.parse(day + "/" + month + "/" + year);
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                            Calendar cal = Calendar.getInstance();
                            cal.setTime(date);

                            // Add to DB and update list
                            db.addNewEventLogEntry(eventID, cal.getTimeInMillis());
                            eventLogAdapter.updateList(db.getEventLog(eventID));
                        }
                    }
                }, mYear, mMonth, mDay);
                dateDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
                dateDialog.show();
            }
        });

        // List item click listener
        eventIdListview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, final long id) {
                CharSequence items[] = {"Edit date" , "Delete"};

                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Options");
                builder.setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(which == 0){
                            // Edit Date selected
                        }else if(which == 1){
                            // Delete selected and update list
                            // NOTE: here id is returned by getItemID() method of the Adapter
                            db.deleteEventLogEntry((int)id);
                            eventLogAdapter.updateList(db.getEventLog(eventID));
                        }
                    }
                });
                builder.create();
                builder.show();
            }
        });

        return rootView;
    }

    class EventLogAdapter extends BaseAdapter{

        private LayoutInflater mInflator;

        ArrayList<EventLogModel> mItemList;

        public EventLogAdapter(ArrayList<EventLogModel> itemList){
            mItemList = itemList;
        }

        @Override
        public int getCount() {
            if(!(mItemList.isEmpty())){
                return mItemList.size();
            }
            return 0;
        }

        @Override
        public Object getItem(int position) {
            if(!(mItemList.isEmpty())){
                // return DB row id
                return mItemList.get(position);
            }
            return null;
        }

        @Override
        public long getItemId(int position) {
            return mItemList.get(position).id;
        }

        public void updateList(ArrayList<EventLogModel> list){
            mItemList.clear();
            mItemList.addAll(list);
            notifyDataSetChanged();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view;
            ViewHolder holder;
            if(convertView == null) {
                mInflator = LayoutInflater.from(context);
                view = mInflator.inflate(R.layout.event_log_list_item, parent, false);
                holder = new ViewHolder();
                holder.relativeTime = (TextView) view.findViewById(R.id.relative_time_textview);
                holder.actualTime = (TextView) view.findViewById(R.id.actual_time_textview);
                view.setTag(holder);
            }else{
                view = convertView;
                holder = (ViewHolder) view.getTag();
            }

            EventLogModel item = mItemList.get(position);
            holder.relativeTime.setText(MainListFragment.getTimeAgo(item.checkInDate));
            Calendar actualTimeCal = Calendar.getInstance();
            actualTimeCal.setTimeInMillis(item.checkInDate);
            holder.actualTime.setText(
                    DAYS[actualTimeCal.get(Calendar.DAY_OF_WEEK) - 1] + " - " +
                    actualTimeCal.get(Calendar.DAY_OF_MONTH) + " " +
                    MONTHS[actualTimeCal.get(Calendar.MONTH)] + " " +
                    actualTimeCal.get(Calendar.YEAR));
            // actualTimeCal.get(Calendar.DAY_OF_WEEK) + 1

            return view;
        }

        class ViewHolder{
            TextView relativeTime;
            TextView actualTime;
            public ViewHolder(){}
        }
    }
}
