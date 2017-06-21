package com.example.parthnaik.beerbuddy;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

public class SignupActivity extends AppCompatActivity {

    private static final String TAG = "SignupActivity";
    TextView _loginLink;
    Button _signupButton;
    EditText _nameText;
    EditText _emailText;
    EditText _passwordText;
    String name,password,email, response="",msg="";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        _loginLink = (TextView) findViewById(R.id.link_login);
        _signupButton = (Button) findViewById(R.id.btn_signup);
        _nameText=(EditText)findViewById(R.id.input_name);
        _passwordText=(EditText)findViewById(R.id.input_password);
        _emailText=(EditText)findViewById(R.id.input_email);

        _loginLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        _signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                name=_nameText.getText().toString();
                email=_emailText.getText().toString();
                password=_passwordText.getText().toString();
                new Register_Web().execute();
            }
        });
    }

    class Register_Web extends AsyncTask {
        ProgressDialog pd;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pd = new ProgressDialog(SignupActivity.this);
            pd.setMessage("Please wait..");
            pd.setCancelable(false);
            pd.show();

        }

        @Override
        protected Object doInBackground(Object[] objects) {

           /* String filePath = fileUri.getPath();
            File sourceFile = new File(filePath);
            Log.e("Image",""+sourceFile);*/

            try {
                URL url = new URL("http://"+ WebServiceConstant.ip+"/beerbuddy/registerUser.php");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(10000);
                conn.setConnectTimeout(15000);
                conn.setRequestMethod("POST");
                conn.setDoInput(true);
                conn.setDoOutput(true);

                HashMap<String,String> params = new HashMap<>();
                params.put("Name", name);
                params.put("Email", email);
                params.put("Password", password);
               
                OutputStream os = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(os, "UTF-8"));
                writer.write(getPostDataString(params));
                writer.flush();
                writer.close();
                os.close();

                // conn.connect();

                int responseCode=conn.getResponseCode();
                Log.e("responceCode:",""+responseCode);
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    String line;
                    BufferedReader br=new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    while ((line=br.readLine()) != null) {
                        response+=line;
                    }
                }
                else {
                    response="";

                }
                Log.e("response",""+response);

           } catch (UnsupportedEncodingException e1) {
                //  e1.printStackTrace();
                Log.e("Unsupported",e1.getMessage()+"");
            } catch (MalformedURLException e) {
                Log.e("MalformedURLException",e.getMessage()+"");
                //  e.printStackTrace();
            } catch (IOException e) {
                Log.e("IOException",e.getMessage()+"");
                // e.printStackTrace();
            }

            return null;
        }

        private String getPostDataString(HashMap<String, String> params) throws UnsupportedEncodingException{
            StringBuilder result = new StringBuilder();
            boolean first = true;
            for(Map.Entry<String, String> entry : params.entrySet()){
                if (first)
                    first = false;
                else
                    result.append("&");

                result.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
                result.append("=");
                result.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
            }

            return result.toString();
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            pd.cancel();
            if (msg.equals("0")) {
                Toast.makeText(SignupActivity.this, "Problem in registration.", Toast.LENGTH_SHORT).show();

            }else{
                Toast.makeText(SignupActivity.this, "Successfully registered.", Toast.LENGTH_SHORT).show();


                Intent i = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(i);
            }

        }
    }
}


      
      
    

