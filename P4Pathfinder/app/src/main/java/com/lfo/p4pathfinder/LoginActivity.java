package com.lfo.p4pathfinder;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class LoginActivity extends AppCompatActivity {

    private EditText inputUsername;
    private EditText inputPassword;
    private DBHelper dbHelper = new DBHelper(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        inputUsername = findViewById(R.id.inputUsername);
        inputPassword = findViewById(R.id.inputPassword);
    }

    public void login(View view) {
        String username = inputUsername.getText().toString();
        String password = inputPassword.getText().toString();
        if (username.length() < 3 || password.length() < 3) {
            Toast.makeText(
                    LoginActivity.this,
                    "Please input minimum 3 character username and password",
                    Toast.LENGTH_LONG).show();
            inputUsername.setText("");
            inputPassword.setText("");
        } else if (dbHelper.checkUsernamePassword("login", username, password)) {
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            intent.putExtra("username", username);
            intent.putExtra("password", password);
            LoginActivity.this.startActivity(intent);
            Toast.makeText(this, "Welcome " + username, Toast.LENGTH_SHORT);
        } else {
            Toast.makeText(this,
                    "Wrong username or password, please try again", Toast.LENGTH_LONG).show();
            inputUsername.setText("");
            inputPassword.setText("");
        }

    }

    public void registerUser(View view) {
        LinearLayout linearLayout = new LinearLayout(this);
        View regUserView = getLayoutInflater().inflate(R.layout.register_user, null);
        linearLayout.addView(regUserView);
        final EditText inputNewUsername = regUserView.findViewById(R.id.inputNewUsername);
        final EditText inputNewPassword = regUserView.findViewById(R.id.inputNewPassword);
        AlertDialog.Builder builder;
        builder = new AlertDialog.Builder(this);
        builder.setView(linearLayout);
        builder.setTitle("Register new user")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        String newUsername = inputNewUsername.getText().toString();
                        String newPassword = inputNewPassword.getText().toString();

                        if (newUsername.length() >= 3 && newPassword.length() >= 3) {
                            if (!dbHelper.checkUsernamePassword("newUser", newUsername, newPassword)) {
                                dbHelper.registerUser(newUsername, newPassword);
                                Toast.makeText(
                                        LoginActivity.this,
                                        "New user saved in the database, proceed to login",
                                        Toast.LENGTH_LONG).show();
                                inputUsername.setText(newUsername);
                                inputPassword.setText(newPassword);
                            } else {
                                Toast.makeText(
                                        LoginActivity.this,
                                        "Username already exists, please choose another",
                                        Toast.LENGTH_LONG).show();
                            }
                        } else {
                            Toast.makeText(
                                    LoginActivity.this,
                                    "Please input minimum 3 character username and password",
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // do nothing
                    }
                });
        builder.show();
    }
}
