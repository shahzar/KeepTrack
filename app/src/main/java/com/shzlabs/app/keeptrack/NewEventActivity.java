package com.shzlabs.app.keeptrack;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class NewEventActivity extends AppCompatActivity {

    EditText eventName;
    Button eventSubmitButton;
    EventDBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_event);
        dbHelper = new EventDBHelper(getApplication());
        eventName = (EditText) findViewById(R.id.event_name);
        eventSubmitButton = (Button) findViewById(R.id.event_submit_button);

        eventSubmitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                long writeCatch = dbHelper.addNewEvent(eventName.getText().toString(),
                        1);
                if (writeCatch > 0) {
                    setResult(1);
                }
                finish();
            }
        });
    }
}
