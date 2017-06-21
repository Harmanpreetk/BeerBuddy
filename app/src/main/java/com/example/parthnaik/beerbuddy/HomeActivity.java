package com.example.parthnaik.beerbuddy;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HttpsURLConnection;

public class HomeActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    Sessionmanager sessionmanager;
    String title;
    String[] empNames,emplat,emplon,barNames,barlat,barlng;
    double[] latitude,latit;
    double[] longitude,longit;
    String[] empIds,barIds;
    JSONArray array = null;
    JSONObject object = null;
    int status;
    String msg;
    static int UId;
    static String Uname;

    private static final long ONE_MIN = 1000 * 60;
    private static final long TWO_MIN = ONE_MIN * 2;
    private static final long FIVE_MIN = ONE_MIN * 5;
    private static final long MEASURE_TIME = 1000 * 30;
    private static final long POLLING_FREQ = 1000 * 10;
    private static final float MIN_ACCURACY = 25.0f;
    private static final float MIN_LAST_READ_ACCURACY = 500.0f;
    private static final float MIN_DISTANCE = 10.0f;
    double lat, lng;

    // Current best location estimate
    private Location mBestReading;

    // Reference to the LocationManager and LocationListener
    private LocationManager mLocationManager;
    private LocationListener mLocationListener;

    private final String TAG = "GetLocationActivity";

    private boolean mFirstUpdate = true;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        EnablePermission.checklocationservice(HomeActivity.this);
        //showPermissionDialog(this);

        sessionmanager = new Sessionmanager(this);
        HashMap hm =  sessionmanager.getuserdetails();

        HashMap<String, String> user = sessionmanager.getuserdetails();
        // name
        if (sessionmanager.isLoggedIn()) {
            UId = Integer.parseInt(user.get(Sessionmanager.KEY_ID));
            Uname = (user.get(Sessionmanager.KEY_NAME));
        }

        // Acquire reference to the LocationManager
        if (null == (mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE)))
            finish();

        // Get best last location measurement
        mBestReading = bestLastKnownLocation(MIN_LAST_READ_ACCURACY, FIVE_MIN);

        // Display last reading information
        if (null != mBestReading) {

            updateDisplay(mBestReading);

        } else {

            Toast.makeText(getApplicationContext(),"No Initial Reading Available",Toast.LENGTH_SHORT).show();

        }

        mLocationListener = new LocationListener() {

            // Called back when location changes

            public void onLocationChanged(Location location) {



                // Determine whether new location is better than current best
                // estimate

                if (null == mBestReading
                        || location.getAccuracy() < mBestReading.getAccuracy()) {

                    // Update best estimate
                    mBestReading = location;

                    // Update display
                    updateDisplay(location);

                    if (mBestReading.getAccuracy() < MIN_ACCURACY)

                        mLocationManager.removeUpdates(mLocationListener);
                }
            }

            public void onStatusChanged(String provider, int status,
                                        Bundle extras) {
                // NA
            }

            public void onProviderEnabled(String provider) {
                // NA
            }

            public void onProviderDisabled(String provider) {
                // NA
            }
        };


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        View header=navigationView.getHeaderView(0);
        TextView setName = (TextView) header.findViewById(R.id.setName);
      //  TextView setEmail = (TextView) header.findViewById(R.id.setEmail);
        setName.setText(Uname);
    }
    @Override
    protected void onStart() {
        super.onStart();
        new getLocation().execute();
        new getBarLocation().execute();
    }

    @Override
    protected void onResume() {
        super.onResume();


        if (!EnablePermission.isInternetConnected(HomeActivity.this)) {
            Toast.makeText(HomeActivity.this, "Please Connect to internet", Toast.LENGTH_LONG).show();
        }

        // Determine whether initial reading is
        // "good enough". If not, register for
        // further location updates

        if (null == mBestReading
                || mBestReading.getAccuracy() > MIN_LAST_READ_ACCURACY
                || mBestReading.getTime() < System.currentTimeMillis()
                - TWO_MIN) {

            // Register for network location updates
            if (null != mLocationManager
                    .getProvider(LocationManager.NETWORK_PROVIDER)) {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                mLocationManager.requestLocationUpdates(
                        LocationManager.NETWORK_PROVIDER, POLLING_FREQ,
                        MIN_DISTANCE, mLocationListener);
            }

            // Register for GPS location updates
            if (null != mLocationManager
                    .getProvider(LocationManager.GPS_PROVIDER)) {
                mLocationManager.requestLocationUpdates(
                        LocationManager.GPS_PROVIDER, POLLING_FREQ,
                        MIN_DISTANCE, mLocationListener);
            }

            // Schedule a runnable to unregister location listeners
            Executors.newScheduledThreadPool(1).schedule(new Runnable() {

                @Override
                public void run() {

                    Log.i(TAG, "location updates cancelled");

                    mLocationManager.removeUpdates(mLocationListener);

                }
            }, MEASURE_TIME, TimeUnit.MILLISECONDS);
        }
    }
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        mLocationManager.removeUpdates(mLocationListener);

    }

    // Get the last known location from all providers
    // return best reading that is as accurate as minAccuracy and
    // was taken no longer then minAge milliseconds ago. If none,
    // return null.

    private Location bestLastKnownLocation(float minAccuracy, long maxAge) {

        Location bestResult = null;
        float bestAccuracy = Float.MAX_VALUE;
        long bestAge = Long.MIN_VALUE;

        List<String> matchingProviders = mLocationManager.getAllProviders();

        for (String provider : matchingProviders) {

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return null;
            }
            Location location = mLocationManager.getLastKnownLocation(provider);

            if (location != null) {

                float accuracy = location.getAccuracy();
                long time = location.getTime();

                if (accuracy < bestAccuracy) {

                    bestResult = location;
                    bestAccuracy = accuracy;
                    bestAge = time;
                }
            }
        }

        // Return best reading or null
        if (bestAccuracy > minAccuracy
                || (System.currentTimeMillis() - bestAge) > maxAge) {
            return null;
        } else {
            return bestResult;
        }
    }

    // Update display
    private void updateDisplay(Location location) {

        Toast.makeText(getApplicationContext(),"Longitude:" + location.getLongitude(),Toast.LENGTH_LONG).show();
        Toast.makeText(getApplicationContext(),"Latitude:" + location.getLatitude(),Toast.LENGTH_LONG).show();

        lat = location.getLatitude();
        lng = location.getLongitude();

        new SendPostRequest().execute();
    }

    public class SendPostRequest extends AsyncTask<String, Void, String> {

        protected void onPreExecute(){}

        protected String doInBackground(String... arg0) {

            try {
                // Random rand = new Random();

                // int  n = rand.nextInt(50) + 1;

                URL url = new URL("http://"+WebServiceConstant.ip+"/beerbuddy/addUserPosition.php?"); // here is your URL path
                Log.e("url", ""+url);
                JSONObject postDataParams = new JSONObject();
                postDataParams.put("id",UId);
                postDataParams.put("name",Uname);
                postDataParams.put("lat",""+lat );
                postDataParams.put("lng", ""+lng);
                Log.e("params",postDataParams.toString());

                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(15000 /* milliseconds */);
                conn.setConnectTimeout(15000 /* milliseconds */);
                conn.setRequestMethod("POST");
                conn.setDoInput(true);
                conn.setDoOutput(true);

                OutputStream os = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(os, "UTF-8"));
                writer.write(getPostDataString(postDataParams));

                writer.flush();
                writer.close();
                os.close();

                int responseCode=conn.getResponseCode();

                if (responseCode == HttpsURLConnection.HTTP_OK) {

                    BufferedReader in=new BufferedReader(new
                            InputStreamReader(
                            conn.getInputStream()));

                    StringBuffer sb = new StringBuffer("");
                    String line="";

                    while((line = in.readLine()) != null) {

                        sb.append(line);
                        break;
                    }

                    in.close();
                    return sb.toString();

                }
                else {
                    return new String("false : "+responseCode);
                }
            }
            catch(Exception e){
                return new String("Exception: " + e.getMessage());
            }

        }

        @Override
        protected void onPostExecute(String result) {
            Toast.makeText(getApplicationContext(), result,
                    Toast.LENGTH_LONG).show();
        }
    }

        @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            sessionmanager.LogOut1(HomeActivity.this, true);

        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.findbuddy) {
            Intent intent = new Intent(getApplicationContext(), mapLocate.class);


            Bundle bundle = new Bundle();

            bundle.putDoubleArray("lat", latitude);
            bundle.putDoubleArray("lng", longitude);
            bundle.putStringArray("Uname", empNames);
            bundle.putDoubleArray("latit", latit);
            bundle.putDoubleArray("longit", longit);
            bundle.putStringArray("bName", barNames);

            // fragment = new mapLocate();
            intent.putExtras(bundle);
            //fragment.setArguments(bundle);

            startActivity(intent);
            title="Locate";
        }


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

public class getLocation extends AsyncTask<String, Void, String> {
    String result;

    protected void onPreExecute() {
    }

    protected String doInBackground(String... arg0) {

        try {

            URL url = new URL("http://"+ WebServiceConstant.ip+"/beerbuddy/getLocation.php?"); // here is your URL path1
            Log.e("url", "" + url);
            JSONObject postDataParams = new JSONObject();
            //postDataParams.put("id", UId);
            Log.e("params", postDataParams.toString());

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(15000 /* milliseconds */);
            conn.setConnectTimeout(15000 /* milliseconds */);
            conn.setRequestMethod("POST");
            conn.setDoInput(true);
            conn.setDoOutput(true);

            OutputStream os = conn.getOutputStream();
            BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(os, "UTF-8"));
            writer.write(getPostDataString(postDataParams));

            writer.flush();
            writer.close();
            os.close();

            int responseCode = conn.getResponseCode();

            if (responseCode == HttpsURLConnection.HTTP_OK) {

                BufferedReader in = new BufferedReader(new
                        InputStreamReader(
                        conn.getInputStream()));

                StringBuffer sb = new StringBuffer("");
                String line = "";

                while ((line = in.readLine()) != null) {
                    sb.append(line);
                    break;
                }

                result = "responce" + sb.toString();
                String[] res = result.split("\\[");
                result = "[" + res[1];

                msg = result;
                Log.e("Msg", "" + msg);
            }


            array = new JSONArray(msg);
            object = array.getJSONObject(0);
            status = object.getInt("Status");
            int size = array.length();
            empNames = new String[size];
            empIds = new String[size];
            latitude = new double[size];
            longitude = new double[size];
            emplat = new String[size];
            emplon = new String[size];

            if (status == 1) {
                for (int j = 0; j < size; j++) {
                    object = array.getJSONObject(j);
                    empNames[j] = object.getString("name");
                    Log.e("EmpName:", "" + empNames[j]);
                    empIds[j] = object.getString("id");
                    latitude[j] = object.getDouble("lat");
                    longitude[j] = object.getDouble("lng");

                    Log.e("Lat:"+j, "" + latitude[j]);
                    Log.e("EmpId:"+j, "" + empIds[j]);
                }
            }
        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }  catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }



    @Override
    protected void onPostExecute (String result){
//            Toast.makeText(getApplicationContext(), result,
//                    Toast.LENGTH_LONG).show();
        // Toast.makeText(getApplicationContext(), "Latitude" + latitude, Toast.LENGTH_SHORT).show();
        //	Toast.makeText(getApplicationContext(), "Longitude" + Ulng, Toast.LENGTH_SHORT).show();
    }
}
public class getBarLocation extends AsyncTask<String, Void, String> {
        String result;

        protected void onPreExecute() {
        }

        protected String doInBackground(String... arg0) {

            try {

                URL url = new URL("http://"+ WebServiceConstant.ip+"/beerbuddy/getBarLocation.php?"); // here is your URL path1
                Log.e("url", "" + url);
                JSONObject postDataParams = new JSONObject();
                //postDataParams.put("id", UId);
                Log.e("params", postDataParams.toString());

                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(15000 /* milliseconds */);
                conn.setConnectTimeout(15000 /* milliseconds */);
                conn.setRequestMethod("POST");
                conn.setDoInput(true);
                conn.setDoOutput(true);

                OutputStream os = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(os, "UTF-8"));
                writer.write(getPostDataString(postDataParams));

                writer.flush();
                writer.close();
                os.close();

                int responseCode = conn.getResponseCode();

                if (responseCode == HttpsURLConnection.HTTP_OK) {

                    BufferedReader in = new BufferedReader(new
                            InputStreamReader(
                            conn.getInputStream()));

                    StringBuffer sb = new StringBuffer("");
                    String line = "";

                    while ((line = in.readLine()) != null) {
                        sb.append(line);
                        break;
                    }

                    result = "responce" + sb.toString();
                    String[] res = result.split("\\[");
                    result = "[" + res[1];

                    msg = result;
                    Log.e("Msg", "" + msg);
                }


                array = new JSONArray(msg);
                object = array.getJSONObject(0);
                status = object.getInt("Status");
                int size = array.length();
                barNames = new String[size];
                barIds = new String[size];
                latit = new double[size];
                longit = new double[size];
                barlat = new String[size];
                barlng = new String[size];

                if (status == 1) {
                    for (int j = 0; j < size; j++) {
                        object = array.getJSONObject(j);
                       barNames[j] = object.getString("name");
                        Log.e("BarName:", "" + barNames[j]);
                        barIds[j] = object.getString("id");
                        latit[j] = object.getDouble("lat");
                        longit[j] = object.getDouble("lng");

                        Log.e("Lat:"+j, "" + latit[j]);
                        Log.e("EmpId:"+j, "" + barIds[j]);
                    }
                }
            } catch (ProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }  catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }



        @Override
        protected void onPostExecute (String result){
//            Toast.makeText(getApplicationContext(), result,
//                    Toast.LENGTH_LONG).show();
            // Toast.makeText(getApplicationContext(), "Latitude" + latitude, Toast.LENGTH_SHORT).show();
            //	Toast.makeText(getApplicationContext(), "Longitude" + Ulng, Toast.LENGTH_SHORT).show();
        }
    }

    private String getPostDataString(JSONObject params) throws Exception{

        StringBuilder result = new StringBuilder();
        boolean first = true;

        Iterator<String> itr = params.keys();

        while (itr.hasNext()) {

            String key = itr.next();
            Object value = params.get(key);

            if (first)
                first = false;
            else
                result.append("&");

            result.append(URLEncoder.encode(key, "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(value.toString(), "UTF-8"));

        }
        return result.toString();
    }
}
