package com.hsd.contest.spain.clover.huawei;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.text.HtmlCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.matteobattilana.weather.PrecipType;
import com.github.matteobattilana.weather.WeatherView;
import com.huawei.hms.analytics.HiAnalytics;
import com.huawei.hms.analytics.HiAnalyticsInstance;
import com.huawei.hms.analytics.HiAnalyticsTools;
import com.huawei.hms.kit.awareness.Awareness;
import com.huawei.hms.kit.awareness.barrier.AwarenessBarrier;
import com.huawei.hms.kit.awareness.barrier.BarrierStatus;
import com.huawei.hms.kit.awareness.barrier.BarrierUpdateRequest;
import com.huawei.hms.kit.awareness.barrier.TimeBarrier;
import com.huawei.hms.kit.awareness.status.WeatherStatus;
import com.huawei.hms.kit.awareness.status.weather.Situation;
import com.huawei.hms.kit.awareness.status.weather.WeatherSituation;

import java.util.ArrayList;
import java.util.TimeZone;

import static android.text.Html.fromHtml;

public class DosisManagerActivity extends AppCompatActivity implements AdapterTratamientos.OnCardListener {

    private ImageView topLayout, bottomLayout;

    private Button searchBtn;

    private ArrayList<Dosis> dosisArrayList;
    private AdapterTratamientos adapterTratamientos;

    private HiAnalyticsInstance instance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dosis_manager);

        // Enable Analytics Kit Log
        HiAnalyticsTools.enableLog();
        // Generate the Analytics Instance
        instance = HiAnalytics.getInstance(this);
        
        topLayout = findViewById(R.id.top_layout);
        bottomLayout = findViewById(R.id.bottom_layout);

        topLayout.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.day_sky_top));
        bottomLayout.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.day_sky));
        setWeather();

        searchBtn = findViewById(R.id.search_btn);
        searchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent (getApplicationContext(), MapActivity.class);
                startActivity(i);
            }
        });

        TypeWriter dialog_empty = findViewById(R.id.dialog_empty);
        dialog_empty.setVisibility(View.INVISIBLE);

        ImageView clover_empty = findViewById(R.id.clover_empty);
        clover_empty.setVisibility(View.INVISIBLE);

        dosisArrayList = getIntent().getParcelableArrayListExtra("dosisList");
        if(!dosisArrayList.isEmpty()) {
            adapterTratamientos = new AdapterTratamientos(dosisArrayList, this);
            RecyclerView recycler = findViewById(R.id.recycler_tratamientos);
            recycler.setLayoutManager(new LinearLayoutManager(DosisManagerActivity.this));
            recycler.setAdapter(adapterTratamientos);
        }
        else {
            clover_empty.setVisibility(View.VISIBLE);
            clover_empty.setBackgroundResource(R.drawable.anim_idle);
            AnimationDrawable frameAnimation = (AnimationDrawable) clover_empty.getBackground();
            frameAnimation.start();

            dialog_empty.setVisibility(View.VISIBLE);
            dialog_empty.setText("");
            dialog_empty.setCharacterDelay((long) 35);

            String styledText = getString(R.string.empty_1) + "<font color='#1CD585'> Añadir</font>!";
            dialog_empty.animateText(HtmlCompat.fromHtml(styledText, HtmlCompat.FROM_HTML_MODE_LEGACY));
        }
    }

    // FIXME: Si borramos todo, explota la app.
    @Override
    public void onPopupMenuClick(View view, int position) {
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case DialogInterface.BUTTON_POSITIVE:
                        dosisArrayList.remove(position);
                        adapterTratamientos.notifyItemRemoved(position);
                        break;
                    case DialogInterface.BUTTON_NEGATIVE:
                        break;
                }
            }
        };
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.borrar_seguro).setPositiveButton(R.string.eliminar, dialogClickListener)
                .setNegativeButton(R.string.no, dialogClickListener).show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setWeather();
    }

    // TODO: Adaptar a método estático en MainActivity para que solo se repita el código una vez (¿= sacar fuera la petición de permisos?).
    private int getWeatherId() {
        // Checks permissions.
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 44);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 45);

        final int[] weather = new int[1];
        Awareness.getCaptureClient(this).getWeatherByDevice()
                .addOnSuccessListener(weatherStatusResponse -> {
                    WeatherStatus weatherStatus = weatherStatusResponse.getWeatherStatus();
                    WeatherSituation weatherSituation = weatherStatus.getWeatherSituation();
                    try {
                        Situation situation = weatherSituation.getSituation();
                        weather[0] = situation.getWeatherId();
                    } catch (NullPointerException ex) {
                        weather[0] = -1;
                    }
                })
                .addOnFailureListener(e -> {
                    weather[0] = -1;
                });

        Bundle bundle = new Bundle();
        bundle.putInt("weatherId", weather[0]);
        instance.onEvent("GET_WEATHER_ID", bundle);
        return weather[0];
    }

    public void setWeather() {
        WeatherView weatherView = findViewById(R.id.weather_view);
        int weatherId = getWeatherId();
        Bundle bundle = new Bundle();
        class TimeBarrierReceiver extends BroadcastReceiver {
            @Override
            public void onReceive(Context context, Intent intent) {
                BarrierStatus barrierStatus = BarrierStatus.extract(intent);
                String label = barrierStatus.getBarrierLabel();
                // DUSK
                if (label.equals("Dusk barrier")) {
                    if (barrierStatus.getPresentStatus() == BarrierStatus.TRUE) {
                        topLayout.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.red_sky));
                        bottomLayout.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.red_sky_top));
                        bundle.putString("TIME_BARRIER", "dusk");
                    }
                }
                // NIGHT
                else if (label.equals("Night barrier 1") || label.equals("Night barrier 2")) {
                    if (barrierStatus.getPresentStatus() == BarrierStatus.TRUE) {
                        topLayout.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.night_sky_top));
                        bottomLayout.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.night_sky));
                        bundle.putString("TIME_BARRIER", "night");
                    }
                }
                // NOON
                else {
                    if (barrierStatus.getPresentStatus() == BarrierStatus.TRUE) {
                        // CLEAR
                        if (weatherId == 1 || weatherId == 2 || weatherId == 3 || weatherId == 14 || weatherId == 17 || weatherId == 21 || weatherId == 30 || weatherId == 32 || weatherId == 33 || weatherId == 34) {
                            topLayout.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.day_sky_top));
                            bottomLayout.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.day_sky));
                            bundle.putString("TIME_BARRIER", "noon_sunny");
                        }
                        // CLOUDY
                        else if (weatherId == 4 || weatherId == 5 || weatherId == 6 || weatherId == 7 || weatherId == 8 || weatherId == 11 || weatherId == 13 || weatherId == 16 || weatherId == 20 || weatherId == 23 || weatherId == 24 || weatherId == 25 || weatherId == 26 || weatherId == 29 || weatherId == 31 || weatherId == 35 || weatherId == 36 || weatherId == 38 || weatherId == 39 || weatherId == 40 || weatherId == 41 || weatherId == 42 || weatherId == 43 || weatherId == 44) {
                            topLayout.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.day_sky_cloudy_top));
                            bottomLayout.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.day_sky_cloudy ));
                            bundle.putString("TIME_BARRIER", "noon_cloudy");
                        }
                    }
                }
            }
        }

        // Checks permissions.
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 44);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 45);

        // Defines TimeBarrier objects so as to detect the current time and change the background accordingly.
        long oneHourMilliSecond = 60 * 60 * 1000L;
        AwarenessBarrier noon = TimeBarrier.duringPeriodOfDay(TimeZone.getDefault(),
                8 * oneHourMilliSecond, 19 * oneHourMilliSecond);

        AwarenessBarrier dusk = TimeBarrier.duringPeriodOfDay(TimeZone.getDefault(),
                19 * oneHourMilliSecond, 21 * oneHourMilliSecond);

        AwarenessBarrier night_1 = TimeBarrier.duringPeriodOfDay(TimeZone.getDefault(),
                21 * oneHourMilliSecond, 24 * oneHourMilliSecond);

        AwarenessBarrier night_2 = TimeBarrier.duringPeriodOfDay(TimeZone.getDefault(),
                0, 8 * oneHourMilliSecond);

        final String BARRIER_RECEIVER_ACTION = getApplication().getPackageName() + "TIME_BARRIER_RECEIVER_ACTION";
        Intent intent = new Intent(BARRIER_RECEIVER_ACTION);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        TimeBarrierReceiver barrierReceiver = new TimeBarrierReceiver();
        registerReceiver(barrierReceiver, new IntentFilter(BARRIER_RECEIVER_ACTION));

        String timeBarrierLabel = "Noon barrier";
        BarrierUpdateRequest.Builder builder = new BarrierUpdateRequest.Builder();
        BarrierUpdateRequest request = builder.addBarrier(timeBarrierLabel, noon, pendingIntent).build();
        Awareness.getBarrierClient(getApplicationContext()).updateBarriers(request);

        String timeBarrierLabel2 = "Dusk barrier";
        BarrierUpdateRequest.Builder builder2 = new BarrierUpdateRequest.Builder();
        BarrierUpdateRequest request2 = builder2.addBarrier(timeBarrierLabel2, dusk, pendingIntent).build();
        Awareness.getBarrierClient(getApplicationContext()).updateBarriers(request2);

        String timeBarrierLabel3 = "Night barrier 1";
        BarrierUpdateRequest.Builder builder3 = new BarrierUpdateRequest.Builder();
        BarrierUpdateRequest request3 = builder3.addBarrier(timeBarrierLabel3, night_1, pendingIntent).build();
        Awareness.getBarrierClient(getApplicationContext()).updateBarriers(request3);

        String timeBarrierLabel4 = "Night barrier 2";
        BarrierUpdateRequest.Builder builder4 = new BarrierUpdateRequest.Builder();
        BarrierUpdateRequest request4 = builder4.addBarrier(timeBarrierLabel4, night_2, pendingIntent).build();
        Awareness.getBarrierClient(getApplicationContext()).updateBarriers(request4);

        // Toggles precipitation.
        if (weatherId == 14 || weatherId == 17 || weatherId == 21 || weatherId == 18 || weatherId == 19 || weatherId == 20 || weatherId == 26 || weatherId == 39 || weatherId == 40 || weatherId == 41 || weatherId == 42 || weatherId == 43 || weatherId == 44) {
            weatherView.setWeatherData(PrecipType.RAIN);
            bundle.putString("PRECIPITATION_TYPE", "rain");
        }
        else if (weatherId == 22 || weatherId == 23 || weatherId == 24 || weatherId == 25) {
            weatherView.setWeatherData(PrecipType.SNOW);
            bundle.putString("PRECIPITATION_TYPE", "snow");
        }
        else {
            weatherView.setWeatherData(PrecipType.CLEAR);
            bundle.putString("PRECIPITATION_TYPE", "clear");
        }
        instance.onEvent("SET_WEATHER", bundle);
    }
}