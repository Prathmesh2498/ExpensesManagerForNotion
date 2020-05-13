package com.pd.expensesnotion;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Pattern;

import static java.lang.Boolean.TRUE;

public class MainActivity extends AppCompatActivity {
    public static final String SAVED_PREFERENCES = "com.example.expensesnotion.savedData";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button info = findViewById(R.id.info);
        final EditText getData = findViewById(R.id.editText);

        info.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this,ShowDetails.class);
                startActivity(intent);
            }
        });

        SharedPreferences getEditor = getSharedPreferences(SAVED_PREFERENCES, MODE_PRIVATE);



        //IF KEY DOES NOT EXIST, IT RETURNS THE VALUE PARAM SO VALUE IS LIKE DEFAULT VALUE
        Boolean  isInitialized = getEditor.getBoolean("Init",Boolean.FALSE);


        if(!isInitialized){

            Intent intent = new Intent(MainActivity.this,userDetails.class);
            startActivityForResult(intent,2);
        }

        Button submit = findViewById(R.id.button);

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String editTextString;
                int amount;
                String d[];

                editTextString = getData.getText().toString();
                d=editTextString.split(",");
                Boolean flag=Boolean.TRUE;
                try {
                    amount = Integer.parseInt(d[1]);
                }catch(Exception e){
                    Toast.makeText(MainActivity.this, "Please Enter a number for amount", Toast.LENGTH_LONG).show();
                    flag=Boolean.FALSE;
                }
                if(flag){
                new AsyncCaller().execute();
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
        if(requestCode==2)
        {
            v2_Token = data.getStringExtra("TOKENv2");
            db_URL = data.getStringExtra("DATABASE_URL");

            putEditor.putString("v2_Token",v2_Token);
            putEditor.putString("db_URL",db_URL);
            putEditor.putBoolean("Init", TRUE);
            putEditor.commit();
        }
    }



    private static String viewSource(Context context, String[]d,int amount, Date date,String TOKEN_V2,String DB_URL) throws IOException {
        if (! Python.isStarted()) {
            Python.start(new AndroidPlatform(context));
        }
        //PYTHON CODE
        String return_value;
        Python py = Python.getInstance();
        PyObject add = py.getModule("Notion");
        Log.e("Sending data...",d[0]+" "+d[2]+" "+d[3]+" "+amount);
        PyObject call = add.callAttr("addRecord", d[0],amount,d[2],d[3],TOKEN_V2,DB_URL);
        return_value = call.toString();
        return return_value;

    }

    private class AsyncCaller extends AsyncTask<String, Void, String>
    {

        Boolean flag=Boolean.FALSE;

        final Date[] date = new Date[1];
        final int amount[] = new int[1];


        final EditText getData = findViewById(R.id.editText);
        final TextView tv = findViewById(R.id.tv);

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
            Log.e("g",editTextString[0]);
            getData.setText("");
            d[0] = editTextString[0].split(Pattern.quote(","));

            amount[0] = Integer.parseInt(d[0][1]);


            date[0] = Calendar.getInstance().getTime();

        }

        @Override
        protected String doInBackground(String... params) {
            try{
                returned_value = viewSource(MainActivity.this, d[0], amount[0], date[0], TOKEN_V2, DB_URL);

                return returned_value;
            }catch (Exception e){
                Log.e("Can't do python", e.toString());

                return returned_value;
            }

        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            SharedPreferences.Editor putEditor = getSharedPreferences(SAVED_PREFERENCES, MODE_PRIVATE).edit();
            if(result.equals("Success")) {
                Toast.makeText(MainActivity.this, result, Toast.LENGTH_SHORT).show();
            }
            else{

                if(result.equals("V2Error")){
                    Toast.makeText(MainActivity.this, "Your V2 Token is wrong. Please Enter Details again!", Toast.LENGTH_LONG).show();
                    putEditor.remove("Init");
                    putEditor.commit();
                    Intent intent = new Intent(MainActivity.this,userDetails.class);
                    startActivityForResult(intent,2);


                }
                else if(result.equals("dbError")){
                    Toast.makeText(MainActivity.this, "Your db URL is wrong. Please Enter Details again!", Toast.LENGTH_LONG).show();
                    putEditor.remove("Init");
                    putEditor.commit();
                    Intent intent = new Intent(MainActivity.this,userDetails.class);
                    startActivityForResult(intent,2);
                }
                else{
                    String sourceString = "<b>" + result + "</b> " ;
                    tv.setText(Html.fromHtml(sourceString, Html.FROM_HTML_MODE_LEGACY));
                    Toast.makeText(MainActivity.this, "Record Deleted!\nPlease add again.", Toast.LENGTH_LONG).show();
                }


            }


        }

    }

}
