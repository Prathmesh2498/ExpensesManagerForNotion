package com.pd.expensesnotion;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;

import java.io.IOException;

public class userDetails extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_details);

        final EditText v2 = findViewById(R.id.v2);
        final EditText db = findViewById(R.id.db);

        Button submit = findViewById(R.id.btnSubmit);
        Button getDetails = findViewById(R.id.btnRead);

        getDetails.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(userDetails.this, ShowDetails.class);
                startActivity(intent);
            }
        });


        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String Token_v2 = v2.getText().toString();
                String db_URL = db.getText().toString();
                String checkValidity="";

                try {
                    if(Token_v2.length()!=0&&db_URL.length()!=0) {
                        checkValidity = viewSource(userDetails.this, Token_v2, db_URL);

                    }
                } catch (Exception e){
                    Log.e("Error", e.toString());
                    Toast.makeText(userDetails.this, "Please Enter both values!", Toast.LENGTH_SHORT).show();
                }
                if(checkValidity.equals("Success")) {
                    Toast.makeText(userDetails.this, "Both values validated", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent();
                    intent.putExtra("TOKENv2", Token_v2);
                    intent.putExtra("DATABASE_URL", db_URL);
                    setResult(2, intent);
                    finish();//finishing activity
                }else{
                    if(checkValidity.equals("V2Error")) {
                        Toast.makeText(userDetails.this, "Invalid V2 Token", Toast.LENGTH_LONG).show();
                    }
                    if(checkValidity.equals("dbError")) {
                        Toast.makeText(userDetails.this, "Invalid DATABASE_URL Token", Toast.LENGTH_LONG).show();
                    }
                }
            }
        });



    }
    private static String viewSource(Context context, String TOKEN_V2, String DB_URL) throws IOException {
        if (! Python.isStarted()) {
            Python.start(new AndroidPlatform(context));
        }
        //PYTHON CODE
        String return_value;
        Python py = Python.getInstance();
        PyObject add = py.getModule("Notion");
        Log.e("Sending..", TOKEN_V2+" "+DB_URL);
        PyObject call = add.callAttr("checkConnect",TOKEN_V2,DB_URL);
        return_value = call.toString();
        return return_value;

    }
}
