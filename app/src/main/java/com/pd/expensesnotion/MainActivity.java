package com.pd.expensesnotion;

import android.app.DatePickerDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Pattern;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;

public class MainActivity extends AppCompatActivity {
    public static final String SAVED_PREFERENCES = "com.pd.expensesnotion.savedData";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button submit = findViewById(R.id.button);

        Button info = findViewById(R.id.info);
        Button updateInfo = findViewById(R.id.up);
        final EditText getData = findViewById(R.id.editText);
        final TextView mTv = findViewById(R.id.ld);
        final Button getDate = findViewById(R.id.getDate);

        //CODE FOR DATE
        final Calendar myCalendar = Calendar.getInstance();
        updateLabel(getDate, myCalendar);
        final DatePickerDialog.OnDateSetListener dt = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                // TODO Auto-generated method stub
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, monthOfYear);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                updateLabel(getDate, myCalendar);
            }

        };


        getDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DatePickerDialog(MainActivity.this, dt, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });
        //END CODE FOR DATE


        info.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this,ShowDetails.class);
                startActivity(intent);
            }
        });

        //GET V2 and DBUrl
        SharedPreferences getEditor = getSharedPreferences(SAVED_PREFERENCES, MODE_PRIVATE);
        //IF KEY DOES NOT EXIST, IT RETURNS THE VALUE PARAM SO VALUE IS LIKE DEFAULT VALUE
        Boolean  isInitialized = getEditor.getBoolean("Init",Boolean.FALSE);

        if(!isInitialized){

            Intent intent = new Intent(MainActivity.this,userDetails.class);
            startActivityForResult(intent,2);
        }


       updateInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Are you sure you want to continue?")
                        .setMessage("This will invalidate current v2 and url")
                        .setNegativeButton(android.R.string.no, null)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                            public void onClick(DialogInterface arg0, int arg1) {
                                Intent intent = new Intent(MainActivity.this,userDetails.class);
                                startActivityForResult(intent,2);
                            }
                        }).create().show();


            }
        });

        try{
            new AsyncCaller().execute("getTags");
            Log.e("Call1", "getTags");
        }catch (Exception e){
            Log.e("TagsError", e.toString());
        }



        submit.setClickable(FALSE);

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if((mTv.getText().toString()).equals("Loading Tags please wait...")){
                    Toast.makeText(MainActivity.this, "Please wait till the tags load!", Toast.LENGTH_LONG).show();
                }else{
                    String editTextString;
                    float amount;
                    String d[];

                    editTextString = getData.getText().toString();
                    d=editTextString.split(",");
                    Boolean flag=Boolean.TRUE;
                    try {
                        amount = Float.parseFloat(d[1]);
                    }catch(Exception e){
                        Toast.makeText(MainActivity.this, "Please Enter a number for amount", Toast.LENGTH_LONG).show();
                        flag=Boolean.FALSE;
                    }


                    if(flag){
                        new AsyncCaller().execute("Submit");
                    }
                }


            }
        });



   }

   // Call Back method  to get the Message form other Activity
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        // check if the request code is same as what is passed  here it is 2
        SharedPreferences.Editor putEditor = getSharedPreferences(SAVED_PREFERENCES, MODE_PRIVATE).edit();
        SharedPreferences getEditor = getSharedPreferences(SAVED_PREFERENCES, MODE_PRIVATE);

        String v2_Token;
        String db_URL;
        if(resultCode==2)
        {
            v2_Token = data.getStringExtra("TOKENv2");
            db_URL = data.getStringExtra("DATABASE_URL");

            putEditor.putString("v2_Token",v2_Token);
            putEditor.putString("db_URL",db_URL);
            putEditor.putBoolean("Init", TRUE);
            putEditor.commit();
        }

    }



    private static String viewSource(Context context, String[]d, float amount, String date, String TOKEN_V2, String DB_URL) throws IOException {
        if (! Python.isStarted()) {
            Python.start(new AndroidPlatform(context));
        }
        //PYTHON CODE
        String return_value;
        Python py = Python.getInstance();
        PyObject add = py.getModule("Notion");
        Log.e("Sending data...",d[0]+" "+d[2]+" "+d[3]+" "+amount);
        PyObject call = add.callAttr("addRecord", d[0],amount,d[2],d[3],date,TOKEN_V2,DB_URL);
        return_value = call.toString();
        return return_value;

    }

    private class AsyncCaller extends AsyncTask<String, Void, String>
    {

        Boolean flag=Boolean.FALSE;

        String Date;
        final float amount[] = new float[1];
        final String tags[] = new String[100];
        final String arr[][] = {tags};
        final Button getDate = findViewById(R.id.getDate);

        final Button submit = findViewById(R.id.button);
        final EditText getData = findViewById(R.id.editText);
        final TextView tv = findViewById(R.id.tv);
        final TextView load = findViewById(R.id.ld);
        final Spinner mySpinner = findViewById(R.id.tags);

        final String data[] = new String[5];
        final String[][] d = {data};
        final String editTextString[] = new String[1];
        final String filename= "Notion.py";


        SharedPreferences getEditor = getSharedPreferences(SAVED_PREFERENCES, MODE_PRIVATE);
        final String TOKEN_V2 = getEditor.getString("v2_Token", null);
        final String DB_URL = getEditor.getString("db_URL", null);

        String returned_value;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            editTextString[0] = getData.getText().toString();
            if(editTextString[0].length()>1) {
                Log.e("g", editTextString[0]);
                getData.setText("");
                editTextString[0] += "," + mySpinner.getSelectedItem().toString();
                d[0] = editTextString[0].split(Pattern.quote(","));

                amount[0] = Float.parseFloat(d[0][1]);
                Date = getDate.getText().toString();


            }

        }

        @Override
        protected String doInBackground(String... params) {
            Log.e("params", params[0]);
            if (params[0].equals("Submit")){
                    try {
                        returned_value = viewSource(MainActivity.this, d[0], amount[0], Date, TOKEN_V2, DB_URL);

                        return returned_value;
                    } catch (Exception e) {
                        Log.e("Can't do python", e.toString());

                        return returned_value;
                    }
            }
            if(params[0].equals("getTags")){
                Log.e("Calling..", "getTags");
                arr[0] = loadTags();
                return "Tags Loaded Successfully";

            }
            return "";

        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            SharedPreferences.Editor putEditor = getSharedPreferences(SAVED_PREFERENCES, MODE_PRIVATE).edit();
            if(result.equals("Success")) {
                final String[] msgToNotificationService = new String[2];
                msgToNotificationService[0] = "Success!";
                msgToNotificationService[1] = "Data added to Notion, and will be reflected in your database.";
                addNotification(msgToNotificationService);
                Toast.makeText(MainActivity.this, result, Toast.LENGTH_SHORT).show();
            }
            else if(result.equals("")){
                Log.e("HereNULL", result);
            }
            else if(result.equals("Tags Loaded Successfully")){
                Log.e("Here", result);
                ArrayAdapter<String>dataAdapter = new ArrayAdapter<String>(MainActivity.this,android.R.layout.simple_spinner_dropdown_item, arr[0]);
                mySpinner.setAdapter(dataAdapter);
                submit.setClickable(TRUE);
                load.setText("Tags Loaded Successfully, You can now add records!");
            }
            else{

                if(result.equals("V2Error")){
                    Toast.makeText(MainActivity.this, "Your V2 Token is wrong/invalidated. Please Enter Details again!", Toast.LENGTH_LONG).show();
                    putEditor.remove("Init");
                    putEditor.commit();
                    Intent intent = new Intent(MainActivity.this,userDetails.class);
                    startActivityForResult(intent,2);
                    final String[] msgToNotificationService = new String[2];
                    msgToNotificationService[0] = "Failed!";
                    msgToNotificationService[1] = "Your V2 Token might be invalidated. Please check through the browser and update the V2 Token!";
                    addNotification(msgToNotificationService);

                }
                else if(result.equals("dbError")){
                    Toast.makeText(MainActivity.this, "Your db URL is wrong/invalidated. Please Enter Details again!", Toast.LENGTH_LONG).show();
                    putEditor.remove("Init");
                    putEditor.commit();
                    Intent intent = new Intent(MainActivity.this,userDetails.class);
                    startActivityForResult(intent, 2);
                    final String[] msgToNotificationService = new String[2];
                    msgToNotificationService[0] = "Failed!";
                    msgToNotificationService[1] = "Your database URL is invalid. Please check through the browser and update the URL!";
                    addNotification(msgToNotificationService);
                }
                else{
                    String sourceString = "<b>" + result + "</b> " ;
                    tv.setText(Html.fromHtml(sourceString, Html.FROM_HTML_MODE_LEGACY));
                    Toast.makeText(MainActivity.this, "Record Deleted!\nPlease add again.", Toast.LENGTH_LONG).show();
                    final String[] msgToNotificationService = new String[2];
                    msgToNotificationService[0] = "Failed!";
                    msgToNotificationService[1] = "Failed due to internal error. Please contact prathu10@gmail.com";
                    addNotification(msgToNotificationService);
                }


            }


        }

    }
    private String[] loadTags(){

        String a[] = {""};

        try{

            SharedPreferences getEditor = getSharedPreferences(SAVED_PREFERENCES, MODE_PRIVATE);
            final String TOKEN_V2 = getEditor.getString("v2_Token", null);
            final String DB_URL = getEditor.getString("db_URL", null);
            Log.e("Sending data...",TOKEN_V2+" "+DB_URL);
            if (! Python.isStarted()) {
                Python.start(new AndroidPlatform(MainActivity.this));
            }
            //PYTHON CODE
            Object return_value[];
            Python py = Python.getInstance();
            PyObject add = py.getModule("Notion");

            PyObject call = add.callAttr("getTags",TOKEN_V2,DB_URL);
            return_value = call.asList().toArray();
            Log.e("Tagsval", Integer.toString(return_value.length));

            if(return_value.length!=0) {
                String arr[] = new String[return_value.length];
                for(int i=0;i<return_value.length;i++){
                    arr[i] = return_value[i].toString();
                }
                return arr;

            }
            else{
                Log.e("TagsFail","Failed to load");
            }

        }catch (Exception e){

            Log.e("Error", e.toString());
        }
        return a;
    }
    private void updateLabel(Button getDate, Calendar myCalendar) {
        String myFormat = "yyyy/MM/dd h:mm a"; //In which you need put here
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);

        getDate.setText(sdf.format(myCalendar.getTime()));
    }


    public void addNotification(String[] msg){
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "1")
                .setSmallIcon(R.mipmap.app_icon)
                .setContentTitle(msg[0])
                .setContentText(msg[1])
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                // Set the intent that will fire when the user taps the notification
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        final int notificationId = 1;
        createChannel(notificationManager);
        notificationManager.notify(notificationId, builder.build());
        Log.e("Notification Status","Done Notifying!");
    }

    public void createChannel(NotificationManager notificationManager){
        Log.e("Notification Status",Integer.toString(Build.VERSION.SDK_INT));
        if (Build.VERSION.SDK_INT < 26) {
            return;
        }
        NotificationChannel channel = new NotificationChannel("1","Default", NotificationManager.IMPORTANCE_HIGH);
        channel.setDescription("All Notifications come through here");
        notificationManager.createNotificationChannel(channel);
    }
}
