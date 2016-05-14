package com.shzlabs.app.keeptrack;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 */
public class MainListFragment extends Fragment {

    Context context;
    View rootView;
    ListView mainListView;
    EventDBHelper dbHelper;
    ArrayList<EventItemModel> itemList = new ArrayList<>();
    MainListAdapter mAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_main_list, container, false);
        context = getActivity();
        dbHelper = new EventDBHelper(context);

        /* Floating action button */
        FloatingActionButton fab = (FloatingActionButton) rootView.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, NewEventActivity.class);
                startActivityForResult(intent, 2);
            }
        });

        // Get event list from DB
        itemList = dbHelper.getEventsList();

        mainListView = (ListView) rootView.findViewById(R.id.mainListView);

        mAdapter = new MainListAdapter(context, itemList);
        Log.d("CustomErr", "Content of mainListView:- " + mainListView);
        mainListView.setAdapter(mAdapter);

        mainListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ((MainActivity)getActivity()).addPageAndSlide(itemList.get(position).id);
            }
        });

        mainListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                CharSequence items[] = new CharSequence[] {"Delete"};
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(which == 0){
                            // Delete event from DB
                            dbHelper.deleteEvent(itemList.get(position).id);
                            // Update list
                            mAdapter.updateItemList(dbHelper.getEventsList());
                            Toast.makeText(context, "Event Deleted", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                builder.show();

                return true;
            }
        });

        return rootView;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 2){
            if(resultCode == 1){
                Snackbar.make(mainListView, "Event Added Successfully!", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
            // Update list
            mAdapter.updateItemList(dbHelper.getEventsList());
        }
    }

    class MainListAdapter extends BaseAdapter {

        private LayoutInflater mInflator;
        private ArrayList<EventItemModel> mItemList;

        public MainListAdapter(Context context, ArrayList<EventItemModel> mList){
            mInflator = LayoutInflater.from(context);
            mItemList = mList;
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
                return mItemList.get(position);
            }
            return null;
        }

        @Override
        public long getItemId(int position) {
            if(!(mItemList.isEmpty())){
                return position;
            }
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view;
            ViewHolder holder;
            if(convertView == null) {
                view = mInflator.inflate(R.layout.main_list_item, parent, false);
                holder = new ViewHolder();
                holder.name = (TextView) view.findViewById(R.id.textViewName);
                holder.lastCheckIn = (TextView) view.findViewById(R.id.textViewLastCheckIn);
                holder.checkInIcon = (LinearLayout) view.findViewById(R.id.checkInIcon);
                view.setTag(holder);
            }else{
                view = convertView;
                holder = (ViewHolder) view.getTag();
            }
            // Get item from list
            final EventItemModel mItem = mItemList.get(position);
            // Set event name
            holder.name.setText(mItem.eventName);
            // Set last checkIn
            if(mItem.lastCheckInDateInMillis == 0){
                holder.lastCheckIn.setText("Never");
            }else {
                holder.lastCheckIn.setText("" + getTimeAgo(mItem.lastCheckInDateInMillis));
            }
            holder.checkInIcon.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    dbHelper.checkIn(mItem.id, System.currentTimeMillis());
                    mAdapter.updateItemList(dbHelper.getEventsList());
                    Toast.makeText(context, "Event checked in", Toast.LENGTH_SHORT).show();
                }
            });

            return view;
        }

        private void updateItemList(ArrayList<EventItemModel> newList){
            itemList.clear();
            itemList.addAll(newList);
            this.notifyDataSetChanged();
        }

        private class ViewHolder{
            public TextView name, lastCheckIn;
            public LinearLayout checkInIcon;
        }
    }

    public static String getTimeAgo(long time) {
        final int SECOND_MILLIS = 1000;
        final int MINUTE_MILLIS = 60 * SECOND_MILLIS;
        final int HOUR_MILLIS = 60 * MINUTE_MILLIS;
        final int DAY_MILLIS = 24 * HOUR_MILLIS;

        if (time < 1000000000000L) {
            // if timestamp given in seconds, convert to millis
            time *= 1000;
        }

        long now = System.currentTimeMillis();
        if (time > now || time <= 0) {
            return null;
        }

        // TODO: localize
        final long diff = now - time;
        if (diff < MINUTE_MILLIS) {
            return "just now";
        } else if (diff < 2 * MINUTE_MILLIS) {
            return "a minute ago";
        } else if (diff < 50 * MINUTE_MILLIS) {
            return diff / MINUTE_MILLIS + " minutes ago";
        } else if (diff < 90 * MINUTE_MILLIS) {
            return "an hour ago";
        } else if (diff < 24 * HOUR_MILLIS) {
            return diff / HOUR_MILLIS + " hours ago";
        } else if (diff < 48 * HOUR_MILLIS) {
            return "yesterday";
        } else {
            return diff / DAY_MILLIS + " days ago";
        }
    }

}
