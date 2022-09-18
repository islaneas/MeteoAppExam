package com.example.meteoexamapp;

import static androidx.constraintlayout.motion.widget.Debug.getLocation;

import android.Manifest;
import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.StrictMode;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity  {
    TextView ville;
    TextView tmp;
    TextView tmpmin;
    TextView tmpmax;
    TextView txtpression;
    TextView txthumidite;
    TextView txtdate;
    double lon, lat;
    Location currentLocation;
    LocationManager locationManager;
    // l'identifiant de l'appel de l'autorisation
    private static final int REQUEST_CODE = 101;

    void getmyLocation() {
        ActivityCompat.requestPermissions( this,
                new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE);
        locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            OnGPS();
        } else {
            getLocation();
        }

    }
    private void OnGPS() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Enable GPS").setCancelable(false).setPositiveButton("Yes", new  DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
            }
        }).setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        final AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
    private void getLocation() {
        if (ActivityCompat.checkSelfPermission(
                MainActivity.this,Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE);
        } else {
            Location locationGPS = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (locationGPS != null) {
                 lat = locationGPS.getLatitude();
                 lon = locationGPS.getLongitude();
               String  latitude = String.valueOf(lat);
                String longitude = String.valueOf(lon);
                ville =findViewById(R.id.txtville);
                ville.setText("Your Location: " + "\n" + "Latitude: " + latitude + "\n" + "Longitude: " + longitude);
                Geocoder geocoder = new Geocoder(this, Locale.getDefault());
                List<Address> addresses = null;
                try {
                    addresses = geocoder.getFromLocation(lat, lon, 1);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                String cityName = addresses.get(0).getLocality();
                getWeatherData(cityName);
            } else {
                Toast.makeText(this, "Unable to find location.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //get my location
        getmyLocation();


        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new
                    StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

    }

    // get weather data
    void getWeatherData(String query) {
        ville.setText(query);
        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        String url = "https://api.openweathermap.org/data/2.5/weather?q="
                + query + "&appid=1562bccb2f9edd4850b88b54a5b4c538";
        //https://api.openweathermap.org/data/2.5/weather?q=London&appid=e457293228d5e1465f30bcbe1aea456b

        // l'ancienne clé : 5bd7e048cf1ef62c79254f75dfe27d19
        // la clé actuelle: e457293228d5e1465f30bcbe1aea456b
        //clé 2022 : e457293228d5e1465f30bcbe1aea456b
        // :y key : 1562bccb2f9edd4850b88b54a5b4c538


        StringRequest stringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                try {
                    Log.i("MyLog", "----------------------------------------------");
                    Log.i("MyLog", response);


                    JSONObject jsonObject = new JSONObject(response);

                    Date date = new Date(jsonObject.getLong("dt") * 1000);
                    SimpleDateFormat simpleDateFormat =
                            new SimpleDateFormat("dd-MMM-yyyy' T 'HH:mm");
                    String dateString = simpleDateFormat.format(date);
                    // map data
                    JSONObject coord = jsonObject.getJSONObject("coord");
                    lon = (double) (coord.getDouble("lon"));
                    lat = (double) (coord.getDouble("lat"));

                    // weather data
                    JSONObject main = jsonObject.getJSONObject("main");
                    int Temp = (int) (main.getDouble("temp") - 273.15);
                    int TempMin = (int) (main.getDouble("temp_min") - 273.15);
                    int TempMax = (int) (main.getDouble("temp_max") - 273.15);
                    int Pression = (int) (main.getDouble("pressure"));
                    int Humidite = (int) (main.getDouble("humidity"));

                    JSONArray weather = jsonObject.getJSONArray("weather");
                    String meteo = weather.getJSONObject(0).getString("main");

                    txtdate.setText(dateString);
                    tmp.setText(String.valueOf(Temp + "°C"));
                    tmpmin.setText(String.valueOf(TempMin) + "°C");
                    tmpmax.setText(String.valueOf(TempMax) + "°C");
                    txtpression.setText(String.valueOf(Pression + " hPa"));
                    txthumidite.setText(String.valueOf(Humidite) + "%");

                    Log.i("Weather", "----------------------------------------------");
                    Log.i("Meteo", meteo);
                    setImage(meteo);
                    Toast.makeText(co, meteo, Toast.LENGTH_LONG).show();


                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.i("MyLog", "-------Connection problem-------------------");
                        Toast.makeText(MainActivity.this,
                                "City not fond", Toast.LENGTH_LONG).show();
                    }
                });

        queue.add(stringRequest);
    }

    Context co;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.search).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        co = this;

        ville = findViewById(R.id.txtville);
        tmp = findViewById(R.id.temp);
        tmpmin = findViewById(R.id.tempmin);
        tmpmax = findViewById(R.id.tempmax);
        txtpression = findViewById(R.id.pression);
        txthumidite = findViewById(R.id.humid);
        txtdate = findViewById(R.id.date);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {

                getWeatherData(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        return true;
    }

    public void setImage(String s) {
        //mainlayout
        View screenView;
        screenView = findViewById(R.id.mainlayout);
        if (s.equals("Rain")) {
            screenView.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.rainy));
        } else if (s.equals("Clear")) {
            screenView.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.hot));
        } else if (s.equals("Thunderstorm")) {
            screenView.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.cold));
        } else if (s.equals("Clouds")) {
            screenView.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.warm));
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void LoadMap(View view) {
        Intent intent = new Intent(this, MapsActivity.class);

        // creating a bundle object
        Bundle bundle = new Bundle();
        // storing the string value in the bundle
        // which is mapped to key
        bundle.putString("city", ville.getText().toString());
        bundle.putString("lon", lon + "");
        bundle.putString("lat", lat + "");

        // passing the bundle into the intent
        intent.putExtras(bundle);
        // starting the intent
        startActivity(intent);
    }



}
