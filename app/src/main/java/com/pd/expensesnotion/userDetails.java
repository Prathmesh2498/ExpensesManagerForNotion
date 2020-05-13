package com.pd.expensesnotion;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

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

                if(Token_v2.length()!=0&&db_URL.length()!=0) {
                    Intent intent = new Intent();
                    intent.putExtra("TOKENv2", Token_v2);
                    intent.putExtra("DATABASE_URL", db_URL);
                    setResult(2, intent);
                    finish();//finishing activity
                }else{
                    Toast.makeText(userDetails.this, "Please Enter both values!", Toast.LENGTH_LONG).show();
                }
            }
        });



    }
}
