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
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import java.net.*;
import java.io.*;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

    }

    // Called when "Update" button tapped.
    public void sendMessage(View view) {
        // Make socket communication asynchronous.
        try {
            new AsyncAction().execute();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Update the message box.
    public void updateMessage(String message) {
        TextView textView = findViewById(R.id.result);
        textView.setText(message);
    }

    // Open settings.
    public void openSettings(View v) {
        Intent intent = new Intent(MainActivity.this, Settings.class);
        startActivityIfNeeded(intent,0);
    }

    // Communication with server.
    public class AsyncAction extends AsyncTask<String, Void, String> {
        protected String doInBackground(String... args) {
            // Message returned from server.
            String message = "";

            // Get settings.
            String host = "";
            String inst_id = "";
            int port = 0;

            try {
                host = getIntent().getExtras().getString("host");
                port = Integer.parseInt(getIntent().getExtras().getString("port"));
                inst_id = getIntent().getExtras().getString("inst_id");

                if (host.equals("")) {
                    return null;
                }

            } catch (Exception e) {
                System.err.println("Error communicating with server. Have you set the host and port?");
                return null;
            }

            try ( // Open socket connection.
                Socket serverConn = new Socket(host, port);

                PrintWriter out = new PrintWriter(serverConn.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(serverConn.getInputStream()));

            ) { // Perform requested operation.


                // Send message to server.
                out.println("QUERY NA " + inst_id);

                // Get server response.
                String line = "";
                while ((line = in.readLine()) != null) {
                    if (line.equals("END-MESSAGE")) { break; }

                    message += line + "\n";
                }

                serverConn.close();

            } catch (UnknownHostException e) { // Host connection error.
                System.err.println("Failed to connect to host: " + host);
                System.exit(1);

            } catch (IOException e) { // Socket I/O error.
                System.err.println("Error getting I/O from: " + host);
                System.exit(1);
            }

            final String finalMessage = message;

            // Update status icon.
            runOnUiThread(new Runnable() {
                public void run() {
                    ImageView image = findViewById(R.id.status_icon);
                    if (finalMessage.equals("")) {
                        image.setImageResource(R.drawable.ok);
                    } else {
                        image.setImageResource(R.drawable.alert);
                    }
                }
            });

            // Send server response to UI.
            runOnUiThread(new Runnable() {
                public void run() {
                    updateMessage(finalMessage);

                }
            });

            return null;
        }
    }
}
