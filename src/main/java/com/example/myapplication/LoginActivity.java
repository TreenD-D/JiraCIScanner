package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.EditText;

public class LoginActivity extends AppCompatActivity {

    public void setAuth(View view){
        EditText editTextLogin = (EditText) findViewById(R.id.editTextLogin);
        EditText editTextPassword = (EditText) findViewById(R.id.editTextPassword);
        String auth = Base64.encodeToString(String.format("%s:%s", editTextLogin.getText(), editTextPassword.getText()).getBytes(), Base64.NO_WRAP);;

        Intent intent = new Intent();
        intent.putExtra("AUTHENTICATION_STRING", auth);
        setResult(RESULT_OK,intent);
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
    }
}
