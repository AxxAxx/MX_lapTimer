package com.AxRACE_Lap_Timer;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.timerapp.mxlaptimer.R;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    public static boolean firstStart = true;

    TextView textView, thename, thetrack ;
    Button start, pause, reset, lap;
    Switch sortswitch;
    EditText inputtext1, inputtext2;

    long MillisecondTime, StartTime, TimeBuff, UpdateTime = 0L ;
    Handler handler;
    int Seconds, Minutes, MilliSeconds ;
    ListView listView ;
    String[] ListElements = new String[] {  };
    List<String> ListElementsArrayList ;
    ArrayAdapter<String> adapter ;
    ArrayList<Rider> riders = new ArrayList<Rider>();

    int bestMillis = 100000;
    int ridernumber = 0;
    // Tag for logging

    // AsyncTask object that manages the connection in a separate thread
    WiFiSocketTask wifiTask = null;

    // UI elements
    TextView textStatus, textRX, textTX;

    TextView count;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Save references to UI elements
        textStatus = (TextView)findViewById(R.id.bluetoothStatus);
        textRX = (TextView)findViewById(R.id.readBuffer);
        textView = (TextView)findViewById(R.id.textView);
        start = (Button)findViewById(R.id.button);
        pause = (Button)findViewById(R.id.button2);
        reset = (Button)findViewById(R.id.button3);
        thename = (TextView) findViewById(R.id.racername);
        thetrack = (TextView) findViewById(R.id.trackname);
        sortswitch = (Switch) findViewById(R.id.switch1);
        inputtext1 = (EditText) findViewById(R.id.racername);
        inputtext2 = (EditText) findViewById(R.id.trackname);

        handler = new Handler() ;

        ListElementsArrayList = new ArrayList<String>(Arrays.asList(ListElements));

        adapter = new ArrayAdapter<String>(MainActivity.this,
                android.R.layout.simple_list_item_1,
                ListElementsArrayList
        );



        inputtext1.setOnKeyListener(new View.OnKeyListener()
        {
            public boolean onKey(View v, int keyCode, KeyEvent event)
            {
                if (event.getAction() == KeyEvent.ACTION_DOWN)
                {
                    switch (keyCode)
                    {
                        case KeyEvent.KEYCODE_DPAD_CENTER:
                        case KeyEvent.KEYCODE_ENTER:
                            inputtext1.clearFocus();
                            return true;
                        default:
                            break;
                    }
                }
                return false;
            }
        });

        inputtext2.setOnKeyListener(new View.OnKeyListener()
        {
            public boolean onKey(View v, int keyCode, KeyEvent event)
            {
                if (event.getAction() == KeyEvent.ACTION_DOWN)
                {
                    switch (keyCode)
                    {
                        case KeyEvent.KEYCODE_DPAD_CENTER:
                        case KeyEvent.KEYCODE_ENTER:
                            inputtext2.clearFocus();
                            return true;
                        default:
                            break;
                    }
                }
                return false;
            }
        });


        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            startstop();



            }
        });

        pause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                handler.removeCallbacks(runnable);
                reset.setEnabled(true);
                MillisecondTime = 0L ;
                StartTime = 0L ;
                TimeBuff = 0L ;
                UpdateTime = 0L ;
                Seconds = 0 ;
                Minutes = 0 ;
                MilliSeconds = 0 ;
                textView.setText("00:00:000");
                firstStart=true;


            }
        });

        reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                handler.removeCallbacks(runnable);
                reset.setEnabled(true);
                MillisecondTime = 0L ;
                StartTime = 0L ;
                TimeBuff = 0L ;
                UpdateTime = 0L ;
                Seconds = 0 ;
                Minutes = 0 ;
                MilliSeconds = 0 ;
                textView.setText("00:00:000");

                riders.clear();

                ridernumber = 0;


                firstStart=true;
                ListElementsArrayList.clear();
                adapter.notifyDataSetChanged();
            }
        });

        sortswitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    sortscore();
                }
                else {
                    sortnumber();
                }
            }
        });
    }

    public void startstop()
    {

        if(firstStart)
        {

            StartTime = SystemClock.uptimeMillis();
            handler.postDelayed(runnable, 0);

            reset.setEnabled(false);
            firstStart=false;
        }
        else
        {
        ridernumber++;

            String[] parts = textView.getText().toString().split(":");
            String part1 = parts[0]; // 004
            String part2 = parts[1]; // 004
            String part3 = parts[2]; // 004

            int result1 = Integer.parseInt(part1)*60000;
            int result2 = Integer.parseInt(part2)*1000;
            int result3 = Integer.parseInt(part3);

            int score = result1 + result2 + result3;

            if (score < bestMillis){
                bestMillis = score;
            }

            riders.add(new Rider(ridernumber, thename.getText().toString(), textView.getText().toString() , "-00:00:000", score, ridernumber));


            // check current state of a Switch (true or false).
            Boolean switchState = sortswitch.isChecked();

            if (switchState) {
                sortscore();            }
            else {
                sortnumber();            }


            int bestTime = 999999999;
            for (Rider temprider : riders)
            {
                if (temprider.getScore() < bestTime){
                    bestTime = temprider.getScore();
                }
            }

            for (Rider temprider : riders)
            {
                int diffInt = temprider.getScore() - bestTime;


                Seconds = (int) (diffInt / 1000);

                Minutes = Seconds / 60;

                Seconds = Seconds % 60;

                MilliSeconds = (int) (diffInt % 1000);

                String c = String.format("%02d", Minutes) + ":" + String.format("%02d", Seconds) + ":" + String.format("%03d", MilliSeconds);


                temprider.setDifftime(c);
            }






            ListView mListView = (ListView) findViewById(R.id.listView);

            PersonListAdapter adapter = new PersonListAdapter(this, R.layout.adapter_view_layout, riders);
            mListView.setAdapter(adapter);


            handler.removeCallbacks(runnable);
            reset.setEnabled(true);
            MillisecondTime = 0L ;
            StartTime = 0L ;
            TimeBuff = 0L ;
            UpdateTime = 0L ;
            Seconds = 0 ;
            Minutes = 0 ;
            MilliSeconds = 0 ;

            textView.setText("00:00:000");
            StartTime = SystemClock.uptimeMillis();
            handler.postDelayed(runnable, 0);
        }
    }

    public Runnable runnable = new Runnable() {

        public void run() {

            MillisecondTime = SystemClock.uptimeMillis() - StartTime;

            UpdateTime = TimeBuff + MillisecondTime;

            Seconds = (int) (UpdateTime / 1000);

            Minutes = Seconds / 60;

            Seconds = Seconds % 60;

            MilliSeconds = (int) (UpdateTime % 1000);

            textView.setText("" + String.format("%02d", Minutes) + ":"
                    + String.format("%02d", Seconds) + ":"
                    + String.format("%03d", MilliSeconds));

            handler.postDelayed(this, 0);
        }

    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_action, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.action_favorite:
                // Vibrate for 400 milliseconds
                Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                v.vibrate(100);
                connectButtonPressed(null);
                return true;
            case R.id.action_share:
                StringBuilder str=new StringBuilder();
                String[] arrStr=new String[ListElementsArrayList.size()];
                ListElementsArrayList.toArray(arrStr);
                    str.append("*Racetrack: " + thetrack.getText().toString() + "*\n");

                    Date c = Calendar.getInstance().getTime();

                    SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy");
                    String formattedDate = df.format(c);
                    str.append("*" + formattedDate + "*\n");



                    for (Rider temp : riders)
                    {
                        str.append(temp.getName() + " - " + temp.getTime()+ " - " + temp.getDifftime());
                        str.append("\n");
                    }



                    for(int i=0;i<arrStr.length;i++){
                    str.append(arrStr[i]);
                    str.append("\n");
                }

                try {
                    Intent sendIntent = new Intent();//"android.intent.action.MAIN");
                    //sendIntent.setComponent(new ComponentName("com.whatsapp", "com.whatsapp.Conversation"));
                    sendIntent.setAction(Intent.ACTION_SEND);
                    sendIntent.setType("text/plain");
                    sendIntent.putExtra(Intent.EXTRA_TEXT, str.toString());
                    // sendIntent.putStringArrayListExtra(Intent.EXTRA_TEXT,finalList);

                    sendIntent.setPackage("com.whatsapp");
                    startActivity(sendIntent);
                } catch(Exception e) {
                    Toast.makeText(this, "Error/n" + e.toString(), Toast.LENGTH_SHORT).show();
                }

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Helper function, print a status to both the UI and program log.
     */
    void setStatus(String s) {
        textStatus.setText(s);
    }

    public void sortscore(){
        Collections.sort(riders, new scoreComparator());

        ListView mListView = (ListView) findViewById(R.id.listView);

        PersonListAdapter adapter = new PersonListAdapter(this, R.layout.adapter_view_layout, riders);
        mListView.setAdapter(adapter);
    }



    public void sortnumber(){
        Collections.sort(riders, new numberComparator());

        ListView mListView = (ListView) findViewById(R.id.listView);

        PersonListAdapter adapter = new PersonListAdapter(this, R.layout.adapter_view_layout, riders);
        mListView.setAdapter(adapter);

    }

    /**
     * Try to start a connection with the specified remote host.
     */
    public void connectButtonPressed(View v) {

        if(wifiTask != null) {
            setStatus("Already connected!");
            return;
        }

        try {
            // Get the remote host from the UI and start the thread
            String host = "192.168.4.1";
            int port = 80;

            // Start the asyncronous task thread
            setStatus("connecting...");
            wifiTask = new WiFiSocketTask(host, port);
            wifiTask.execute();

        } catch (Exception e) {
            e.printStackTrace();
            setStatus("Invalid address/port!");
        }
    }


    /**
     * Invoked by the AsyncTask when the connection is successfully established.
     */
    private void connected() {
        // Vibrate for 100 milliseconds
        Vibrator v2 = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        v2.vibrate(100);
        setStatus("Connected.");
    }


    /**
     * Invoked by the AsyncTask when a newline-delimited message is received.
     */
    private void gotMessage(String msg) {
        textRX.setText(msg);
    }




    class scoreComparator implements Comparator<Rider> {
        @Override
        public int compare(Rider a, Rider b) {
            return a.getScore() > b.getScore() ? 1 : a.getScore() == b.getScore() ? 0 : -1;
        }
    }

    class numberComparator implements Comparator<Rider> {
        @Override
        public int compare(Rider a, Rider b) {
            return a.getNumber() > b.getNumber() ? -1 : a.getNumber() == b.getNumber() ? 0 : 1;
        }
    }
    

    /**
     * AsyncTask that connects to a remote host over WiFi and reads/writes the connection
     * using a socket. The read loop of the AsyncTask happens in a separate thread, so the
     * main UI thread is not blocked. However, the AsyncTask has a way of sending data back
     * to the UI thread. Under the hood, it is using Threads and Handlers.
     */
    public class WiFiSocketTask extends AsyncTask<Void, String, Void> {

        // Location of the remote host
        String address;
        int port;

        // Special messages denoting connection status
        private static final String PING_MSG = "2";
        private static final String CONNECTED_MSG = "1";
        private static final String LAP_MSG = "0";
        private static final String DISCONNECTED_MSG = "DISCONNECTED";


        Socket socket = null;
        BufferedReader inStream = null;
        OutputStream outStream = null;

        // Signal to disconnect from the socket
        private boolean disconnectSignal = false;

        // Socket timeout - close if no messages received (ms)
        private int timeout = 10000;

        // Constructor

        WiFiSocketTask(String address, int port) {
            this.address = address;
            this.port = port;
        }

        /**
         * Main method of AsyncTask, opens a socket and continuously reads from it
         */
        @Override
        protected Void doInBackground(Void... arg) {

            try {

                // Open the socket and connect to it
                socket = new Socket();
                socket.connect(new InetSocketAddress(address, port), timeout);

                // Get the input and output streams
                inStream = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                outStream = socket.getOutputStream();

                // Confirm that the socket opened
                if(socket.isConnected()) {

                    // Make sure the input stream becomes ready, or timeout
                    long start = System.currentTimeMillis();
                    while(!inStream.ready()) {
                        long now = System.currentTimeMillis();
                        if(now - start > timeout) {
                            disconnectSignal = true;
                            break;
                        }
                    }
                } else {
                    disconnectSignal = true;
                }

                // Read messages in a loop until disconnected
                while(!disconnectSignal) {

                    // Parse a message with a newline character
                    String msg = inStream.readLine();

                    // Send it to the UI thread
                    publishProgress(msg);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

            // Send a disconnect message
            publishProgress(DISCONNECTED_MSG);

            // Once disconnected, try to close the streams
            try {
                if (socket != null) socket.close();
                if (inStream != null) inStream.close();
                if (outStream != null) outStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        /**
         * This function runs in the UI thread but receives data from the
         * doInBackground() function running in a separate thread when
         * publishProgress() is called.
         */
        @Override
        protected void onProgressUpdate(String... values) {

            String msg = values[0];
            if(msg == null) return;

            // Handle meta-messages
            if(msg.equals(CONNECTED_MSG)) {
                connected();
            }
            else if(msg.equals(LAP_MSG))
            {
                gotMessage(msg);
                Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                v.vibrate(200);
                startstop();
            }

            else if(msg.equals(PING_MSG))
            {}


            super.onProgressUpdate(values);
        }

        /**
         * Write a message to the connection. Runs in UI thread.
         */
        public void sendMessage(String data) {

            try {
                outStream.write(data.getBytes());
                outStream.write('\n');
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        /**
         * Set a flag to disconnect from the socket.
         */
        public void disconnect() {
            disconnectSignal = true;
        }

    }

}