package com.hsd.contest.spain.clover.huawei;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TimePicker;

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

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class SetTimeActivity extends AppCompatActivity implements AdapterHoras.OnCardListener {

    private ImageView topLayout, bottomLayout;

    private ArrayList<String> horas;
    private AdapterHoras adapterHoras;
    private boolean[] dias;
    private String nombre;
    private int day, month, year, int_dias, int_tiempo;
    private ImageButton next;
    private HiAnalyticsInstance instance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_time);

        // Enable Analytics Kit Log
        HiAnalyticsTools.enableLog();
        // Generate the Analytics Instance
        instance = HiAnalytics.getInstance(this);

        topLayout = findViewById(R.id.top_layout);
        bottomLayout = findViewById(R.id.bottom_layout);

        topLayout.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.day_sky_top));
        bottomLayout.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.day_sky));
        setWeather();

        nombre = getIntent().getStringExtra("nombre");

        // Fecha de inicio.
        day = getIntent().getIntExtra("day", 0);
        month = getIntent().getIntExtra("month", 0);
        year = getIntent().getIntExtra("year", 0);

        //  Array de días de la semana seleccionados por el usuario.
        dias = getIntent().getBooleanArrayExtra("freq");

        // Para el intervalo de tiempo (Opción 3).
        int_dias = getIntent().getIntExtra("int_dias", 0);
        int_tiempo = getIntent().getIntExtra("int_tiempo", 0);

        horas = new ArrayList<>();
        adapterHoras = new AdapterHoras(horas, this);
        RecyclerView recycler = findViewById(R.id.recycler_horas);
        recycler.setLayoutManager(new LinearLayoutManager(SetTimeActivity.this));
        recycler.setAdapter(adapterHoras);

        // Si no hay horas asignadas, no se permite continuar.
        next = findViewById(R.id.set_times_btn);
        next.setVisibility(View.INVISIBLE);

        Button add_time = findViewById(R.id.add_time);
        add_time.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar mcurrentTime = Calendar.getInstance();
                int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
                int minute = mcurrentTime.get(Calendar.MINUTE);
                TimePickerDialog mTimePicker;
                mTimePicker = new TimePickerDialog(SetTimeActivity.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                        if (selectedMinute < 10)
                            horas.add(horas.size(), selectedHour + ":0" + selectedMinute);
                        else
                            horas.add(horas.size(), selectedHour + ":" + selectedMinute);
                        adapterHoras.notifyItemInserted(horas.size());
                        next.setVisibility(View.VISIBLE);
                    }
                }, hour, minute, true); //Yes 24 hour time
                mTimePicker.setTitle("Select Time");
                mTimePicker.show();
            }
        });

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setAlarm();
                // TODO: Pedir el número de pastillas por blíster y el número de pastillas por dosis.
                Intent i = new Intent(getApplicationContext(), MainActivity.class);
                i.putExtra("from" , "SetTimeActivity");
                i.putExtra("dosis", new Dosis(nombre, 0, horas, 0, int_dias, int_tiempo, dias));
                startActivity(i);
            }
        });
    }

    @Override
    public void onPopupMenuClick(View view, int position) {
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case DialogInterface.BUTTON_POSITIVE:
                        horas.remove(position);
                        adapterHoras.notifyItemRemoved(position);
                        if (horas.isEmpty())
                            next.setVisibility(View.GONE);
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

    private void setAlarm() {
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        Intent intent = new Intent(getApplicationContext(), AlarmBroadcast.class);
        intent.putExtra("nombre", "FOO");
        
        for (int i = 0; i < horas.size(); i++) {
            PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), i, intent, PendingIntent.FLAG_ONE_SHOT);
            String dateTime = day + "-" + month + "-" + year + " " + horas.get(i);
            DateFormat formatter = new SimpleDateFormat("d-M-yyyy hh:mm", Locale.getDefault());
            try {
                Date date = formatter.parse(dateTime);

                // Cada día.
                if (areAllTrue(dias)) {
                    alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, date.getTime(), AlarmManager.INTERVAL_DAY, pendingIntent);
                }
                // Intervalo de días, semanas o meses.
                else if (areAllFalse(dias)) {
                    // Días
                    if (int_tiempo == 0) {
                        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, date.getTime(), AlarmManager.INTERVAL_DAY * int_dias, pendingIntent);
                    // Semanas
                    } else if (int_tiempo == 1) {
                        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, date.getTime(), AlarmManager.INTERVAL_DAY * 7 * int_dias, pendingIntent);
                    }
                }
                // Cada semana, distintos días.
                else {
                    // Creamos un calendario para iterar sobre la semana.
                    Calendar calendar = Calendar.getInstance();
                    Date date1 = Calendar.getInstance().getTime();

                    calendar.add(Calendar.DATE, 8);
                    Date date2 = calendar.getTime();

                    calendar.add(Calendar.DATE, -8);
                    while (date1.before(date2)) {
                        // Asignamos la fecha sobre la que estamos iterando al calendario auxiliar.
                        int day = calendar.get(Calendar.DAY_OF_WEEK);
                        if (dias[day - 1]) {
                            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, date1.getTime(), AlarmManager.INTERVAL_DAY * 7 * int_dias, pendingIntent);
                        }
                        // Actualizamos la fecha.
                        calendar.add(Calendar.DATE, 1);
                        date1 = calendar.getTime();
                    }
                }
                alarmManager.set(AlarmManager.RTC_WAKEUP, date.getTime(), pendingIntent);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
    }

    public static boolean areAllTrue(boolean[] array)
    {
        for(boolean b : array) if(!b) return false;
        return true;
    }

    public static boolean areAllFalse(boolean[] array)
    {
        for(boolean b : array) if(b) return false;
        return true;
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