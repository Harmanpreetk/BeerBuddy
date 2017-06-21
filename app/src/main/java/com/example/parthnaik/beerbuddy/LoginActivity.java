package com.example.parthnaik.beerbuddy;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;

import static com.example.parthnaik.beerbuddy.Sessionmanager.KEY_NAME;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";
    private static final int REQUEST_SIGNUP = 0;
    Button _loginButton;
    EditText _emailText,_passwordText;
    TextView _signupLink;
    Sessionmanager sessionManager;
    HttpURLConnection httpURLConnection;
    JSONArray array;
    boolean res;
    String email,password,response;
    String name1, pass1, mail1;
    static int status=0, id1, i;
    ProgressDialog progressDialog;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        _loginButton = (Button) findViewById(R.id.btn_login);
        _signupLink = (TextView) findViewById(R.id.link_signup);
        _emailText = (EditText) findViewById(R.id.input_email);
        _passwordText = (EditText) findViewById(R.id.input_password);

        sessionManager = new Sessionmanager(getApplicationContext());
        res = sessionManager.checklogin();

        if (getIntent().getStringExtra("Key") != null) {
            String sop = getIntent().getStringExtra("Key");
            Log.e("SOP", sop);
        }

        if (res) {
            HashMap<String, String> hm = sessionManager.getuserdetails();
            String name=hm.get(KEY_NAME);
            Intent i = new Intent(LoginActivity.this, HomeActivity.class);
            startActivity(i);
            finish();
        } else {


            _loginButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    email = _emailText.getText().toString();
                    password = _passwordText.getText().toString();

                    // TODO: Implement your own authentication logic here.
                    if (email.equals("") || password.equals("")) {
                        Toast.makeText(LoginActivity.this, "Enter Email and Password", Toast.LENGTH_LONG).show();
                    } else {
                        if (EnablePermission.isInternetConnected(LoginActivity.this)) {
                            new Check_web().execute();
                        } else {
                            Toast.makeText(LoginActivity.this, "No internet connection", Toast.LENGTH_LONG).show();
                        }

                    }

                }
            });

            _signupLink.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getApplicationContext(), SignupActivity.class);
                    startActivityForResult(intent, REQUEST_SIGNUP);
                }
            });
        }
    }








    @Override
    public void onBackPressed() {
        // disable going back to the LoginActivity
        moveTaskToBack(true);
    }



    public class Check_web extends AsyncTask{
        ProgressDialog progressDialog;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(LoginActivity.this);
            progressDialog.setMessage("Authenticating...");
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        protected Object doInBackground(Object[] params) {
            try {


                String param = "Email=" + URLEncoder.encode(String.valueOf(email), "UTF-8") +
                        "&" + "Password=" + URLEncoder.encode(String.valueOf(password), "UTF-8");

                Log.e("params", param);
                URL url = new URL("http://" + WebServiceConstant.ip + "/beerbuddy/loginUser.php?" + param);
                Log.e("url", "" + url);

                Object obj = null;
                HttpURLConnection httpURLConnection = null;
                httpURLConnection = (HttpURLConnection) url.openConnection();
                Log.e("URLInfo", url + "");

                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setDoOutput(true);
                httpURLConnection.setDoInput(true);
                httpURLConnection.setRequestProperty("Accept", "application/json");
                httpURLConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                Log.e("yash", "" + httpURLConnection.getResponseCode());
                int i = httpURLConnection.getResponseCode();
                Log.e("yash", "" + i);
                if (i == HttpURLConnection.HTTP_OK) {
                    BufferedReader in = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
                    String inputLine;
                    StringBuffer responce = new StringBuffer();

                    while ((inputLine = in.readLine()) != null) {
                        responce.append(inputLine);
                    }

                    in.close();
                    Log.e("in if", "" + responce);
                    response = responce.toString();
                    Log.e("in if", "" + response);
                    //    Log.e("i",""+response);

                }

                array = new JSONArray(response);
                JSONObject temp = array.getJSONObject(0);
                status = temp.getInt("status");
                //  message = temp.getString("message");
                Log.e("in if", "" + status);
                if (status == 1) {
                    id1 = temp.getInt("id");
                    name1 = temp.getString("name");
                    pass1 = temp.getString("password");
                    mail1 = temp.getString("mail");

                }
            } catch (MalformedURLException e) {

            } catch (IOException e) {
                // Log.e("yash",e.getMessage()+"");
                e.printStackTrace();
            } catch (JSONException e) {
                //Log.e("yash",e.getMessage()+"");
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            Log.e("status", "" + status);
            if (status == 0) {
                Toast.makeText(LoginActivity.this, "Username or Password may be wrong.", Toast.LENGTH_SHORT).show();
                progressDialog.cancel();
                _emailText.setText("");
                _passwordText.setText("");
            } else if (status == 1) {
                progressDialog.cancel();
                Toast.makeText(LoginActivity.this, "Successful", Toast.LENGTH_SHORT).show();
                sessionManager.CreateLoginSession(id1, name1, mail1, pass1);

                    Intent i = new Intent(LoginActivity.this, HomeActivity.class);
                    startActivity(i);


                finish();


            } else {
                progressDialog.cancel();
                Toast.makeText(LoginActivity.this, "Already Logged In", Toast.LENGTH_SHORT).show();
            }
        }
    }
}

