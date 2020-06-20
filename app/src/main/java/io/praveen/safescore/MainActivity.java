package io.praveen.safescore;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.location.Location;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.BatteryManager;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Objects;

public class MainActivity extends Fragment {

    FirebaseAuth mAuth;
    FirebaseUser mUser;
    SharedPreferences preferences;
    double lat1, lon1;
    LocationManager locationManager;
    TextView name;
    TextView location;
    TextView police;
    TextView away;
    TextView safe;
    TextView time;
    TextView threat;
    boolean is_police=true,is_bar=true;
    SwipeRefreshLayout mSwipeRefreshLayout;

    @SuppressLint("InflateParams")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_main, null);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        away = view.findViewById(R.id.main_away);
        time = view.findViewById(R.id.main_time);
        police = view.findViewById(R.id.main_police);
        name = view.findViewById(R.id.main_welcome);
        location = view.findViewById(R.id.main_location);
        threat = view.findViewById(R.id.main_threat);
        safe=view.findViewById(R.id.unsafe_main);
        mSwipeRefreshLayout = view.findViewById(R.id.swipeToRefresh);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.colorAccent);
        locationManager = (LocationManager) Objects.requireNonNull(getActivity()).getSystemService(Context.LOCATION_SERVICE);
    }

    @SuppressLint({"MissingPermission", "SetTextI18n"})
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                FragmentTransaction ftr = Objects.requireNonNull(getFragmentManager()).beginTransaction();
                ftr.detach(MainActivity.this).attach(MainActivity.this).commit();
                mSwipeRefreshLayout.setRefreshing(false);
            }
        });
        float lat = preferences.getFloat("lat", 0);
        float lon = preferences.getFloat("lon", 0);
        int out = preferences.getInt("Out", 0);
        int back = preferences.getInt("Back", 0);
        Location loc = null;
        if (locationManager != null) loc = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        if (loc != null) {
            lat1 = loc.getLatitude();
            lon1 = loc.getLongitude();
        }
        double distance = distance(lat, lat1, lon, lon1);
        mUser = mAuth.getCurrentUser();
        name.setText("Welcome, " + mUser.getDisplayName() + ".");
        location.setText(lat1 + ", " + lon1);
        Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("HH:mm | dd/MM/yyyy", Locale.ENGLISH);
        String formattedDate = df.format(c.getTime());
        SimpleDateFormat df2 = new SimpleDateFormat("HH", Locale.ENGLISH);
        int mTime = Integer.valueOf(df2.format(c.getTime()));
        time.setText(formattedDate+" ✅");
        DecimalFormat _numberFormat = new DecimalFormat("#0.0");
        float mDist = Float.parseFloat(_numberFormat.format((float) distance / 1000));
        new Json().execute("https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=" + lat1 + "," + lon1 + "&radius=2000&type=police&key=AIzaSyDLoccMhKiGN0kkGReCbKQa0eoCKBo1PoY");
        new Json2().execute("https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=" + lat1 + "," + lon1 + "&radius=2000&type=liquor_store&key=AIzaSyDLoccMhKiGN0kkGReCbKQa0eoCKBo1PoY");
        if (mDist > 5){
            if( mTime > out && mTime < back ){
                away.setTextColor(getResources().getColor(R.color.colorGreen));
                away.setText("AWAY FROM HOME BY " + mDist + " KMs\n(PREDICTED TO BE AT WORK/SCHOOL)");
            }
            else away.setText("AWAY FROM HOME BY " + mDist + " KMs");
        }
        else if (mDist > 0.5) {
            away.setText("AWAY FROM HOME BY " + mDist + " KMs");
            away.setTextColor(getResources().getColor(R.color.colorGreen));
        } else {
            away.setText("SWEET, YOU ARE NEAR YOUR HOME!");
            away.setTextColor(getResources().getColor(R.color.colorGreen));
        }

    }

    public static double distance(double lat1, double lat2, double lon1, double lon2) {
        final int R = 6371;
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = R * c * 1000;
        distance = Math.pow(distance, 2) + Math.pow(0, 2);
        distance = Math.sqrt(distance);
        return distance;
    }

    @SuppressLint("StaticFieldLeak")
    private class Json extends AsyncTask<String, String, String> {

        protected void onPreExecute() {
            super.onPreExecute();
        }

        protected String doInBackground(String... params) {
            HttpURLConnection connection = null;
            BufferedReader reader = null;
            try {
                URL url = new URL(params[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();
                InputStream stream = connection.getInputStream();
                reader = new BufferedReader(new InputStreamReader(stream));
                StringBuilder buffer = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) buffer.append(line).append("\n");
                return buffer.toString();
            } catch (IOException ignored) {} finally {
                if (connection != null) connection.disconnect();
                try { if (reader != null) reader.close(); }
                catch (IOException ignored) {}
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            try {
                JSONObject mainObject = new JSONObject(result);
                String policeStatus = mainObject.getString("status");
                if (policeStatus.equals("ZERO_RESULTS")){
                    is_police=false;
                    police.setText("NO STATIONS NEAR 2 KMs");
                    police.setTypeface(null, Typeface.BOLD);
                    police.setTextColor(getResources().getColor(R.color.colorAccent));
                } else{
                    is_police=true;
                    JSONArray res = mainObject.getJSONArray("results");
                    JSONObject obj = res.getJSONObject(0);
                    final JSONObject lo = obj.getJSONObject("geometry");
                    JSONObject geo = lo.getJSONObject("location");
                    final String lat = geo.getString("lat");
                    final String lng = geo.getString("lng");
                    final String loc = obj.getString("name");
                    police.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            String uri = String.format(Locale.ENGLISH, "http://maps.google.com/maps?daddr=%f,%f (%s)", Float.valueOf(lat), Float.valueOf(lng), loc);
                            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                            intent.setPackage("com.google.android.apps.maps");
                            startActivity(intent);
                        }
                    });
                    police.setText("YES, STATION FOUND\n("+loc+")");
                    police.setTextColor(getResources().getColor(R.color.colorGreen));
                }
                if(is_police==false&&is_bar==true)
                {
                    safe.setText("You are in Unsafe Zone");
                }
                else
                {
                    safe.setText("You are in Safe Zone");
                    safe.setTextColor(getResources().getColor(R.color.colorGreen));
                }
            } catch (Exception ignored){}
            super.onPostExecute(result);
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class Json2 extends AsyncTask<String, String, String> {

        protected void onPreExecute() {
            super.onPreExecute();
        }

        protected String doInBackground(String... params) {
            HttpURLConnection connection = null;
            BufferedReader reader = null;
            try {
                URL url = new URL(params[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();
                InputStream stream = connection.getInputStream();
                reader = new BufferedReader(new InputStreamReader(stream));
                StringBuilder buffer = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) buffer.append(line).append("\n");
                return buffer.toString();
            } catch (IOException ignored) {} finally {
                if (connection != null) connection.disconnect();
                try { if (reader != null) reader.close(); }
                catch (IOException ignored) {}
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            try {
                JSONObject mainObject = new JSONObject(result);
                String wineStatus = mainObject.getString("status");
                if (wineStatus.equals("ZERO_RESULTS")){
                    is_bar=false;
                    threat.setText("NONE/MINIMAL");
                    threat.setTextColor(getResources().getColor(R.color.colorGreen));
                } else{
                    is_bar=true;
                    JSONArray res = mainObject.getJSONArray("results");
                    JSONObject obj = res.getJSONObject(0);
                    final JSONObject lo = obj.getJSONObject("geometry");
                    JSONObject geo = lo.getJSONObject("location");
                    final String lat = geo.getString("lat");
                    final String lng = geo.getString("lng");
                    final String loc = obj.getString("name");
                    threat.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            String geoUri = "http://maps.google.com/maps?q=loc:" + lat + "," + lng + " (" + loc + ")";
                            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(geoUri));
                            startActivity(intent);
                        }
                    });
                    threat.setText("LIQUOR SHOP\n("+loc+")");
                    threat.setTypeface(null, Typeface.BOLD);
                    threat.setTextColor(getResources().getColor(R.color.colorAccent));
                }
                if(is_police==false&&is_bar==true)
                {
                    safe.setText("You are in Unsafe Zone");
                }
                else
                {
                    safe.setText("You are in Safe Zone");
                    safe.setTextColor(getResources().getColor(R.color.colorGreen));
                }
            }
            catch (Exception ignored){}
            super.onPostExecute(result);
        }
    }
}
