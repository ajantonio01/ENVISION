package com.zaidimarvels.voiceapp;

import android.Manifest;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Locale;

public class learning_interface extends AppCompatActivity implements View.OnClickListener {
    private static final int MY_PERMISSIONS_REQUEST_RECORD_AUDIO = 1;
    private TextToSpeech tts;
    private SpeechRecognizer speechRecog;
    Button pdfDL, startRead;
    boolean isReading = false;
    TextView content;
    long queueID;

    DownloadManager dm;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_learning_interface);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        pdfDL = findViewById(R.id.PDFdownload);
        pdfDL.setOnClickListener(this);
        startRead = findViewById(R.id.btn_startRead);
        startRead.setOnClickListener(this);

        content = findViewById(R.id.textView3);

        BroadcastReceiver receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(action)) {
                    DownloadManager.Query reqq = new DownloadManager.Query();
                    reqq.setFilterById(queueID);

                    Cursor c = dm.query(reqq);

                    if (c.moveToFirst()) {
                        int columnIndex = c.getColumnIndex(DownloadManager.COLUMN_STATUS);

                        if (DownloadManager.STATUS_SUCCESSFUL == c.getInt(columnIndex)) {
                            Toast.makeText(learning_interface.this, "Done DOwnloading", Toast.LENGTH_LONG).show();
                        }
                    }
                }
            }
        };

        FloatingActionButton fab = findViewById(R.id.fab2);
        fab.setOnClickListener(this);

        initializeTextToSpeech();
        initializeSpeechRecognizer();
    }

    private void initializeSpeechRecognizer() {
        if (SpeechRecognizer.isRecognitionAvailable(this)) {
            speechRecog = SpeechRecognizer.createSpeechRecognizer(this);
            speechRecog.setRecognitionListener(new RecognitionListener() {
                @Override
                public void onReadyForSpeech(Bundle params) {

                }

                @Override
                public void onBeginningOfSpeech() {

                }

                @Override
                public void onRmsChanged(float rmsdB) {

                }

                @Override
                public void onBufferReceived(byte[] buffer) {

                }

                @Override
                public void onEndOfSpeech() {

                }

                @Override
                public void onError(int error) {

                }

                @Override
                public void onResults(Bundle results) {
                    List<String> result_arr = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                    processResult(result_arr.get(0));
                }

                @Override
                public void onPartialResults(Bundle partialResults) {

                }

                @Override
                public void onEvent(int eventType, Bundle params) {

                }
            });
        }
    }

    private void processResult(String result_message) {
        result_message = result_message.toLowerCase();

//        Handle at least four sample cases

//        First: What is your Name?
//        Second: What is the time?
//        Third: Is the earth flat or a sphere?
//        Fourth: Open a browser and open url
        if (result_message.indexOf("go back") != -1){
            Intent intent = new Intent(learning_interface.this,
                    com.zaidimarvels.voiceapp.Main2Activity.class);
            startActivity(intent);
        } else if (result_message.indexOf("download pdf") == 0) {
            downloadPDF();
        }
        else if ((result_message.indexOf("start reading") == 0) || (result_message.indexOf("read again") == 0)){
            speak(content.getText().toString());
        }
        else if (result_message.indexOf("stop reading") == 0){
//            stop(content);
        } else if (result_message.indexOf("repeat") == 0) {
            speak("You chose basics of computer."+".Say start reading to start."+
                    ".Say download pdf to download.");
        }
    }

    private void initializeTextToSpeech() {
        tts = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (tts.getEngines().size() == 0 ){
                    Toast.makeText(learning_interface.this, getString(R.string.tts_no_engines),Toast.LENGTH_LONG).show();
                    finish();
                } else {
                    tts.setLanguage(Locale.US);
                    String stringer = getString(R.string.Introduction);
                    String Title = getString(R.string.basicsComputer);
//                    speak("You choose basics of computer." + "." + "." +
//                                   Title + "." + stringer
//                            );
                    speak("You chose basics of computer."+".Say. start reading. to start."+
                            ".Say. download pdf. to download."+".Tap the button again to stop reading.");
                }
            }
        });
    }

    private void speak(String message) {
        if(Build.VERSION.SDK_INT >= 21){
            tts.speak(message,TextToSpeech.QUEUE_FLUSH,null,null);
        } else {
            tts.speak(message, TextToSpeech.QUEUE_FLUSH,null);
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

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPause() {
        super.onPause();
        tts.shutdown();
    }

    @Override
    protected void onResume() {
        super.onResume();
//        Reinitialize the recognizer and tts engines upon resuming from background such as after openning the browser
        initializeSpeechRecognizer();
        initializeTextToSpeech();
    }

    public boolean isFilePresent(Context context, String fileName) {
        String path = context.getFilesDir().getAbsolutePath() + "/" + fileName;
        File file = new File(path);
        return file.exists();
    }

    private void downloadPDF() {
        dm = (DownloadManager)getSystemService(Context.DOWNLOAD_SERVICE);
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse("http://download.nos.org/coa631/ch1.pdf"));
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        String dlPath = Environment.getExternalStorageDirectory().getPath();

        request.setDestinationInExternalFilesDir(this, dlPath, "basics_of_computer.txt");
        queueID = dm.enqueue(request);
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.PDFdownload) {

            if (ContextCompat.checkSelfPermission(learning_interface.this, Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(learning_interface.this,
                        new String[]{Manifest.permission.INTERNET},1);

                if (ContextCompat.checkSelfPermission(learning_interface.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(learning_interface.this,
                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                }

            } else {
                downloadPDF();

                String newText = "";

                try {

                    InputStream is = getAssets().open("basics_of_computer.txt");
                    int size = is.available();
                    byte[] buffer = new byte[size];
                    is.read(buffer);
                    is.close();
                    newText = new String(buffer);

                } catch (Exception e) {
//                    Toast.makeText(this, this.getFilesDir().getPath(), Toast.LENGTH_LONG).show();
                    Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show();
                }

                speak(newText);

                content.setText(newText);

            }

        } else if (view.getId() == R.id.fab2) {
            if (ContextCompat.checkSelfPermission(learning_interface.this,
                    Manifest.permission.RECORD_AUDIO)
                    != PackageManager.PERMISSION_GRANTED) {

                // Permission is not granted
                // Should we show an explanation?
                if (ActivityCompat.shouldShowRequestPermissionRationale(learning_interface.this,
                        Manifest.permission.RECORD_AUDIO)) {
                    // Show an explanation to the user *asynchronously* -- don't block
                    // this thread waiting for the user's response! After the user
                    // sees the explanation, try again to request the permission.
                } else {
                    // No explanation needed; request the permission
                    ActivityCompat.requestPermissions(learning_interface.this,
                            new String[]{Manifest.permission.RECORD_AUDIO},MY_PERMISSIONS_REQUEST_RECORD_AUDIO);

                    // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                    // app-defined int constant. The callback method gets the
                    // result of the request.
                }

            } else {
                // Permission has already been granted
                Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS,1);
                if (tts.isSpeaking()) {
                    tts.stop();
                    speechRecog.startListening(intent);
                } else {
                    speechRecog.startListening(intent);
                }
            }
        } else if (view.getId() == R.id.btn_startRead) {

            if (isReading) {
                tts.stop();
                isReading = false;
            } else {
                speak(content.getText().toString());
                isReading = true;
            }
        }
    }
}