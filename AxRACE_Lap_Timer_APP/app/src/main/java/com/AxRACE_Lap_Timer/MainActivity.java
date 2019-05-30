package com.AxRACE_Lap_Timer;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


public class MainActivity extends AppCompatActivity {
    public static boolean firstStart = true;

    TextView textView, thename ;
    Button start, pause, reset, lap ;
    Switch sortswitch;

    long MillisecondTime, StartTime, TimeBuff, UpdateTime = 0L ;
    Handler handler;
    int Seconds, Minutes, MilliSeconds ;
    ListView listView ;
    String[] ListElements = new String[] {  };
    List<String> ListElementsArrayList ;
    ArrayAdapter<String> adapter ;
    ArrayList<Rider> riders = new ArrayList<Rider>();

    int ridernumber = 0;
    // Tag for logging
    private final String TAG = getClass().getSimpleName();

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
        listView = (ListView)findViewById(R.id.listview1);
        thename = (TextView) findViewById(R.id.racername);
        sortswitch = (Switch) findViewById(R.id.switch1);

        handler = new Handler() ;

        ListElementsArrayList = new ArrayList<String>(Arrays.asList(ListElements));

        adapter = new ArrayAdapter<String>(MainActivity.this,
                android.R.layout.simple_list_item_1,
                ListElementsArrayList
        );

        listView.setAdapter(adapter);

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
                textView.setText("00:00:00");
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
                textView.setText("00:00:00");

                riders.clear();



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
            ridernumber = 0;

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

            int sum = result1 + result2 + result3;

            riders.add(new Rider(thename.getText().toString(),textView.getText().toString() , sum, ridernumber));

            // check current state of a Switch (true or false).
            Boolean switchState = sortswitch.isChecked();

            if (switchState) {
                sortscore();            }
            else {
                sortnumber();            }



            handler.removeCallbacks(runnable);
            reset.setEnabled(true);
            MillisecondTime = 0L ;
            StartTime = 0L ;
            TimeBuff = 0L ;
            UpdateTime = 0L ;
            Seconds = 0 ;
            Minutes = 0 ;
            MilliSeconds = 0 ;

            textView.setText("00:00:00");
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

            textView.setText("" + Minutes + ":"
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
                // Vibrate for 400 milliseconds
                Vibrator v2 = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                v2.vibrate(100);

                StringBuilder str=new StringBuilder();
                String[] arrStr=new String[ListElementsArrayList.size()];
                ListElementsArrayList.toArray(arrStr);

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
        ListElementsArrayList.clear();
        for(Rider temp: riders){
            ListElementsArrayList.add(0, temp.getriderName() +  "   -   " + temp.getTime() + "   -   " + temp.getNumber());
        }
        adapter.notifyDataSetChanged();
    }


    public void sortnumber(){
        Collections.sort(riders, new numberComparator());
        ListElementsArrayList.clear();
        for(Rider temp: riders){
            ListElementsArrayList.add(0, temp.getriderName() +  "   -   " + temp.getTime() + "   -   " + temp.getNumber());
        }
        adapter.notifyDataSetChanged();
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
        setStatus("Connected.");
    }


    /**
     * Invoked by the AsyncTask when a newline-delimited message is received.
     */
    private void gotMessage(String msg) {
        textRX.setText(msg);
    }



    public class Rider {

        private String riderName;
        private String riderTime;
        private int riderScore;
        private int riderNumber;

        public Rider(String riderName, String riderTime, int riderScore, int riderNumber) {
            super();
            this.riderName = riderName;
            this.riderTime = riderTime;
            this.riderScore = riderScore;
            this.riderNumber = riderNumber;
        }

        public String getriderName() {
            return riderName;
        }
        public void setriderName(String riderName) {
            this.riderName = riderName;
        }
        public String getTime() {
            return riderTime;
        }
        public void setTime(String riderTime) {
            this.riderTime = riderTime;
        }
        public int getScore() {
            return riderScore;
        }
        public void setriderScore(int riderScore) {
            this.riderScore = riderScore;
        }
        public int getNumber() {
            return riderNumber;
        }
        public void setriderNumber(int riderNumber) {
            this.riderNumber = riderNumber;
        }
    }
    class scoreComparator implements Comparator<Rider> {
        @Override
        public int compare(Rider a, Rider b) {
            return a.getScore() < b.getScore() ? 1 : a.getScore() == b.getScore() ? 0 : -1;
        }
    }

    class numberComparator implements Comparator<Rider> {
        @Override
        public int compare(Rider a, Rider b) {
            return a.getNumber() < b.getNumber() ? -1 : a.getNumber() == b.getNumber() ? 0 : 1;
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