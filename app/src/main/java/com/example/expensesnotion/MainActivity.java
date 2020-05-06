package com.example.expensesnotion;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.NoCopySpan;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;

import org.w3c.dom.Text;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        Button submit = findViewById(R.id.button);

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                new AsyncCaller().execute();
            }
        });



   }
    private static String viewSource(Context context, String[]d,int amount, Date date) throws IOException {
        if (! Python.isStarted()) {
            Python.start(new AndroidPlatform(context));
        }
        //PYTHON CODE
        String return_value;
        Python py = Python.getInstance();
        PyObject add = py.getModule("Notion");
        Log.e("Sending data...",d[0]+" "+d[2]+" "+d[3]+" "+amount);
        PyObject call = add.callAttr("addRecord", d[0],amount,d[2],d[3]);
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

        String returned_value;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            editTextString[0] = getData.getText().toString();
            Log.e("g",editTextString[0]);
            getData.setText("");
            d[0] = editTextString[0].split(Pattern.quote("+"));

            amount[0] = Integer.parseInt(d[0][1]);


            date[0] = Calendar.getInstance().getTime();

        }

        @Override
        protected String doInBackground(String... params) {
            try{
                returned_value = viewSource(MainActivity.this, d[0], amount[0], date[0]);

                return returned_value;
            }catch (Exception e){
                Log.e("Can't do python", e.toString());

                return returned_value;
            }

        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if(result.equals("Success")) {
                Toast.makeText(MainActivity.this, result, Toast.LENGTH_SHORT).show();
            }
            else{
                tv.setText(result);
            }





        }

    }

}
