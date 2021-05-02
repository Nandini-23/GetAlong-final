package com.example.getalong.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.getalong.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    TextView txt_signup;
    EditText login_email, login_password;
    Button signin_btn;
    FirebaseAuth auth;
    String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
    ProgressDialog progressDialog;
    private boolean doubleBackToExitPressedOnce = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Please Wait...");
        progressDialog.setCancelable(false);

        auth = FirebaseAuth.getInstance();

        txt_signup = findViewById(R.id.txt_signup);
        signin_btn = findViewById(R.id.signin_btn);
        login_email = findViewById(R.id.login_email);
        login_password = findViewById(R.id.login_password);

        signin_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                progressDialog.show();
                String email = login_email.getText().toString();
                String password = login_password.getText().toString();

                if(TextUtils.isEmpty(email) || TextUtils.isEmpty(password))
                {
                    progressDialog.dismiss();
                    Toast.makeText(LoginActivity.this, "Please fill all the fields!!!", Toast.LENGTH_SHORT).show();
                } else if(!email.matches(emailPattern))
                {
                    progressDialog.dismiss();
                    login_email.setError("Invalid Email");
                    Toast.makeText(LoginActivity.this, "Invalid Email", Toast.LENGTH_SHORT).show();
                } else if(password.length()<6)
                {
                    progressDialog.dismiss();
                    login_password.setError("Invalid Password");
                    Toast.makeText(LoginActivity.this,"Please enter valid password", Toast.LENGTH_SHORT).show();
                } else
                {
                    auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()){
                                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                                progressDialog.dismiss();
                                if(user.isEmailVerified()){
                                    startActivity(new Intent(LoginActivity.this, HomeActivity.class));
                                    finish();
                                }else{
                                    user.sendEmailVerification();
                                    Toast.makeText(LoginActivity.this, "check your mail to verify your account ", Toast.LENGTH_SHORT).show();

                                }

                            } else {
                                progressDialog.dismiss();
                                Toast.makeText(LoginActivity.this, "Error in login", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        });

        txt_signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, RegistrationActivity.class));
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            return;
        } else
        {
            Toast.makeText(this, "Please click BACK again to exit", Toast.LENGTH_SHORT).show();
            doubleBackToExitPressedOnce = true;
        }
    }
}