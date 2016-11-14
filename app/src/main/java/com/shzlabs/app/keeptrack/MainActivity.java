package com.shzlabs.app.keeptrack;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.nbsp.materialfilepicker.ui.FilePickerActivity;
import com.shzlabs.app.keeptrack.util.PermUtil;
import com.shzlabs.app.keeptrack.view.CustomViewPager;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int REQUEST_CODE_PICKFILE = 1010;
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
            case R.id.action_about_me : {
                Intent intent = new Intent(context, AboutMeActivity.class);
                startActivity(intent);
                return true;
            }
            case R.id.action_export_db: {
                return exportData();
            }

            case R.id.action_import_db: {
                return importData();
            }


        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        // Get Selected Database file to import
        if (requestCode == REQUEST_CODE_PICKFILE && resultCode == RESULT_OK) {
            String filePath = data.getStringExtra(FilePickerActivity.RESULT_FILE_PATH);
            Log.d(TAG, "onActivityResult: File Path: " + filePath);
            importDataReceived(filePath);
        }
    }

    private boolean importData() {

        // Display File picker
        Intent intent = new Intent(context, FilePickerActivity.class);
        intent.putExtra(FilePickerActivity.ARG_FILTER, Pattern.compile(".*\\.db$"));
        startActivityForResult(intent, REQUEST_CODE_PICKFILE);
        return true;
    }

    private void importDataReceived(String filePath) {
        EventDBHelper db = new EventDBHelper(context);
        try {
            db.importDatabase(filePath);
        } catch (IOException e) {
            Log.e(TAG, "importDataReceived: Error importing database");
            Toast.makeText(MainActivity.this, "Error importing database", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    private boolean exportData() {

        // Permissions check
        PermUtil.verifyStoragePermissions(MainActivity.this);

        String appDir = Environment.getExternalStorageDirectory().getPath()
                + File.separator
                + getResources().getString(R.string.app_name);
        Log.d(TAG, "exportData: App Directory path: " + appDir);

        File f = new File(appDir);

        // Handle Directory
        if( !f.exists() && !f.isDirectory()) {
            if ( f.mkdir() ) {
                Log.d(TAG, "exportData: Directory created!");
            }else {
                Log.e(TAG, "exportData: Error creating directory");
                Toast.makeText(MainActivity.this, "Error creating directory", Toast.LENGTH_SHORT).show();
                return false;
            }
        }

        // Directory available, Good to go!
        String exportPath = appDir
                + File.separator
                + EventDBHelper.DATABASE_NAME;

        // Checking if database available
        File databaseFile = new File(exportPath);

        Log.d(TAG, "exportData: Exporting database to following location: " + exportPath);
        EventDBHelper db = new EventDBHelper(context);

        try {
            db.exportDatabase(exportPath);
        } catch (IOException e) {
            Log.e(TAG, "exportData: Error Exporting database.");
            e.printStackTrace();
            Toast.makeText(MainActivity.this, "Error Exporting database.", Toast.LENGTH_SHORT).show();
            return false;
        }

        Toast.makeText(MainActivity.this, "Database stored in " + exportPath, Toast.LENGTH_SHORT).show();

        return true;
    }
}
