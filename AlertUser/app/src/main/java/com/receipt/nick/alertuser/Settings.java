/*
 * Nick Bild
 * nick.bild@gmail.com
 * 2018-03-10
 * An experiment in deep learning that solves
 * an interesting and important problem in
 * public safety.
*/

package com.receipt.nick.alertuser;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

public class Settings extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
    }

    // Save and go back to Main Activity.
    public void openMain(View v) {
        Intent intent = new Intent(Settings.this,MainActivity.class);

        // Make fields available to MainActivity.
        EditText hostET = findViewById(R.id.host);
        intent.putExtra("host",hostET.getText().toString());

        EditText portET = findViewById(R.id.port);
        intent.putExtra("port",portET.getText().toString());

        EditText instET = findViewById(R.id.inst_id);
        intent.putExtra("inst_id",instET.getText().toString());

        // Launch MainActivity.
        startActivityIfNeeded(intent,0);

    }
}
