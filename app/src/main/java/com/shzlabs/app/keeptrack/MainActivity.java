package com.shzlabs.app.keeptrack;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.shzlabs.app.keeptrack.view.CustomViewPager;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    Context context;
    CustomViewPager mPager;
    ScreenSlidePagerAdapter mPagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // The usual stuff
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        context = this;

        // Set the pager
        mPager = (CustomViewPager) findViewById(R.id.view_pager);
        mPagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager());
        mPager.setAdapter(mPagerAdapter);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        mPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener(){
            @Override
            public void onPageSelected(int position) {
                if(position == 0){
                    getSupportActionBar().setDisplayHomeAsUpEnabled(false);
                    getSupportActionBar().setDisplayShowHomeEnabled(false);
                }else{
                    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                    getSupportActionBar().setDisplayShowHomeEnabled(true);
                }
                super.onPageSelected(position);
            }
        });
    }

    private class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {

        private List<Fragment> pages;

        public ScreenSlidePagerAdapter(FragmentManager fm) {
            super(fm);
            pages = new ArrayList<>();
            pages.add(new MainListFragment());
        }

        public void addPage(int eventID){
            pages.add(EventDetailsFragment.newInstance(eventID));
            notifyDataSetChanged();
        }

        public void removePage(int position){
            pages.remove(position);
            notifyDataSetChanged();
        }

        @Override
        public Fragment getItem(int position) {
            Log.d("pageID", "Displaying fragment at position:- " + position);
            return pages.get(position);
        }

        @Override
        public int getItemPosition(Object object) {
            if(object instanceof MainListFragment){
                return PagerAdapter.POSITION_UNCHANGED;
            }
            return PagerAdapter.POSITION_NONE;
        }

        @Override
        public int getCount() {
            return pages.size();
        }

    }

    public void addPageAndSlide(int eventID){
        int position = mPager.getCurrentItem() + 1;
        mPagerAdapter.addPage(eventID);
        mPager.setCurrentItem(position, true);
    }

    @Override
    public void onBackPressed() {
        if(mPager.getCurrentItem() == 0){
            super.onBackPressed();
        }else{
            // Go to first fragment and clear all views from list
            final int deleteItemPos = mPager.getCurrentItem();
            mPager.setCurrentItem(mPager.getCurrentItem() - 1, true);

            // Added delay coz it was preventing slide animation
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mPagerAdapter.removePage(deleteItemPos);
                }
            }, 500);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id){
            case R.id.action_settings :
                return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
