package com.shzlabs.app.keeptrack;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

public class AboutMeActivity extends AppCompatActivity {

    TextView website;
    ImageButton github, gplus, linkedin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_me);
        setTitle("About");
        website = (TextView) findViewById(R.id.website_url);
        github = (ImageButton) findViewById(R.id.github_link);
        gplus = (ImageButton) findViewById(R.id.gplus_link);
        linkedin = (ImageButton) findViewById(R.id.linkedin_link);

        // Personal website
        website.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = "http://shahzar.tk";
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url)) ;
                startActivity(intent);
            }
        });

        // Github
        github.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = "https://github.com/shahzar";
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url)) ;
                startActivity(intent);
            }
        });

        // Google Plus
        gplus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = "https://plus.google.com/+ShahzarAhmed";
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url)) ;
                startActivity(intent);
            }
        });

        // LinkedIn
        linkedin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = "https://www.linkedin.com/in/shahzar-ahmed-37013183";
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url)) ;
                startActivity(intent);
            }
        });

    }
}
