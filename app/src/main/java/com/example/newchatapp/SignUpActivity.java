package com.example.newchatapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class SignUpActivity extends AppCompatActivity {

    EditText username,email,password,conpassword;
    TextView toLogin;
    Button  registerButton;

    FirebaseAuth auth;
    DatabaseReference reference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        Toolbar toolbar = findViewById(R.id.toolbarsign);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Register");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        username = findViewById(R.id.signUpUserName);
        email = findViewById(R.id.signUpEmail);
        password = findViewById(R.id.signUpPassword);
        conpassword = findViewById(R.id.signUpConfirmPassword);
        registerButton = findViewById(R.id.buttonInSignUP);
        toLogin = findViewById(R.id.toLoginText);

        toLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SignUpActivity.this,LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });

        auth = FirebaseAuth.getInstance();

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String str_username,str_email,str_password,str_conpassword;

                str_username = username.getText().toString();
                str_email = email.getText().toString();
                str_password = password.getText().toString();
                str_conpassword = conpassword.getText().toString();
                
                if(TextUtils.isEmpty(str_username) || TextUtils.isEmpty(str_email) || TextUtils.isEmpty(str_password) || TextUtils.isEmpty(str_conpassword)) {
                    Toast.makeText(SignUpActivity.this, "Please enter all required information!", Toast.LENGTH_SHORT).show();
                }
                else if(str_password.length() < 6 || str_conpassword.length() < 6) {
                    Toast.makeText(SignUpActivity.this, "Password must be at least 6 characters!", Toast.LENGTH_SHORT).show();
                }
                else if(!str_password.equals(str_conpassword)){
                    Toast.makeText(SignUpActivity.this, "Password does not match!", Toast.LENGTH_SHORT).show();
                }
                else{
                    register(str_username, str_email, str_password);
                }
            }
        });
    }

    private void register(String username,String email,String password){

        auth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()) {
                    FirebaseUser firebaseUser = auth.getCurrentUser();
                    assert firebaseUser != null;
                    String userid = firebaseUser.getUid();

                    reference = FirebaseDatabase.getInstance().getReference("Users").child(userid);

                    HashMap<String, String> hashMap = new HashMap<>();
                    hashMap.put("id",userid);
                    hashMap.put("username", username);
                    hashMap.put("E-Mail",email);
                    hashMap.put("Password",password);
                    hashMap.put("image","default");

                    reference.setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                Intent intent = new Intent(SignUpActivity.this, MainActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                                finish();

                            }
                        }
                    });

                }
                else{
                    Toast.makeText(SignUpActivity.this, "You can not register with this E-Mail or password", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}