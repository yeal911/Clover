package com.hsd.contest.spain.clover.huawei;

import android.Manifest;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.RadioButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.github.amlcurran.showcaseview.ShowcaseView;
import com.github.amlcurran.showcaseview.targets.ViewTarget;
import com.github.matteobattilana.weather.PrecipType;
import com.github.matteobattilana.weather.WeatherView;
import com.huawei.hmf.tasks.OnFailureListener;
import com.huawei.hmf.tasks.OnSuccessListener;
import com.huawei.hmf.tasks.Task;
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
import com.huawei.hms.mlsdk.MLAnalyzerFactory;
import com.huawei.hms.mlsdk.common.MLFrame;
import com.huawei.hms.mlsdk.text.MLText;
import com.huawei.hms.mlsdk.text.MLTextAnalyzer;

import java.io.IOException;
import java.util.TimeZone;

public class ActivityAddFreq  extends AppCompatActivity {

    private ImageView topLayout = findViewById(R.id.top_layout), bottomLayout = findViewById(R.id.bottom_layout);

    private String nombre;

    private EditText et_nombre;

    private LinearLayout dias_layout, layout_interval;

    private DatePicker dp;

    private NumberPicker ipicker_num, ipicker_dwm;

    private Button btn;

    private HiAnalyticsInstance instance;
    
    private static final int PICK_IMAGE = 100;
    Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_freq);

        btn = findViewById(R.id.btn_confirm);

        if (getIntent().getStringExtra("from") != null) {
            if (getIntent().getStringExtra("from").equals("Tutorial")) {

                // No queremos pasar de pantalla accidentalmente en mitad del tutorial.
                btn.setEnabled(false);

                ShowcaseView scv = new ShowcaseView.Builder(this)
                        .setTarget(new ViewTarget(findViewById(R.id.layout_nombre)))
                        .setContentTitle("Insertar nombre")
                        .setContentText("Aquí me tienes que decir cómo quieres que llame al tratamiento. Si es un nombre muy complicado y no te acuerdas de cómo se escribe, aprieta el botón de la derecha para enseñarme una foto de la caja del medicamento.")
                        .setStyle(R.style.CustomShowcaseTheme)
                        .build();
                scv.overrideButtonClick(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        scv.setTarget(new ViewTarget(findViewById(R.id.layout_fecha)));
                        scv.setContentTitle("Especificar fecha de inicio");
                        scv.setContentText("Tanto si empiezas el tratamiento hoy, hace un mes o incluso el año que viene; ¡dímelo para que lo apunte en mi agenda!");
                        scv.setStyle(R.style.CustomShowcaseTheme);
                        scv.overrideButtonClick(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                scv.setTarget(new ViewTarget(findViewById(R.id.layout_fecha)));
                                scv.setContentTitle("Especificar frecuencia");
                                scv.setContentText("¿Cada cuánto tienes tu dosis? Sé que las posibilidades son infinitas, pero las tengo todas presentes.");
                                scv.setStyle(R.style.CustomShowcaseTheme);
                                scv.overrideButtonClick(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        scv.setTarget(new ViewTarget(findViewById(R.id.btn_confirm)));
                                        scv.setContentTitle("Continuar para especificar horas");
                                        scv.setContentText("Te toque a las 6, a las 7 o incluso a las 9:41, nunca me pillarás dormida. ¡Dime a qué hora te tengo que avisar y no llegaré tarde ni un solo día!");
                                        scv.setStyle(R.style.CustomShowcaseTheme);
                                        scv.overrideButtonClick(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {
                                                Intent i = new Intent(getApplicationContext(), MainActivity.class);
                                                i.putExtra("from", "Tutorial2");
                                                startActivity(i);
                                            }
                                        });
                                    }
                                });
                            }
                        });
                    }
                });
            }
        }
        else {
            // Enable Analytics Kit Log
            HiAnalyticsTools.enableLog();
            // Generate the Analytics Instance
            instance = HiAnalytics.getInstance(this);

            et_nombre = findViewById(R.id.et_nombre);
            topLayout = findViewById(R.id.top_layout);
            bottomLayout = findViewById(R.id.bottom_layout);

            nombre = et_nombre.getText().toString();

            ImageButton btn_camera = findViewById(R.id.btn_camera);

            dias_layout = findViewById(R.id.dias_layout);
            dias_layout.setVisibility(View.GONE);

            topLayout.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.day_sky_top));
            bottomLayout.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.day_sky));
            setWeather();

            layout_interval = findViewById(R.id.interval_picker);
            layout_interval.setVisibility(View.GONE);

            dp = findViewById(R.id.date);

            ipicker_num = findViewById(R.id.ipicker_num);
            ipicker_num.setMinValue(1);
            ipicker_num.setMaxValue(30);

            ipicker_dwm = findViewById(R.id.ipicker_dwm);
            ipicker_dwm.setMinValue(0);
            ipicker_dwm.setMaxValue(1);
            ipicker_dwm.setDisplayedValues(new String[] {"Días", "Semanas"});

            CheckBox lunes, martes, miercoles, jueves, viernes, sabado, domingo;
            lunes = findViewById(R.id.cb_lunes);
            martes = findViewById(R.id.cb_martes);
            miercoles = findViewById(R.id.cb_miercoles);
            jueves = findViewById(R.id.cb_jueves);
            viernes = findViewById(R.id.cb_viernes);
            sabado = findViewById(R.id.cb_sabado);
            domingo = findViewById(R.id.cb_domingo);

            // TODO: Si se insertan datos nulos, ocultar el botón.
            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    boolean[] dias = new boolean[] {};
                    RadioButton a = findViewById(R.id.optionA);
                    RadioButton b = findViewById(R.id.optionB);
                    RadioButton c = findViewById(R.id.optionC);

                    // Cada día
                    if (a.isChecked()) {
                        // El array de días es siempre true.
                        dias = new boolean[] {true, true, true, true, true, true, true};
                    }
                    // Días específicos
                    // TODO: Si está seleccionada esta opción y no hay ningún día seleccionado, no se puede avanzar.
                    else if (b.isChecked()) {
                        // Cogemos el array personalizado del usuario.
                        dias = new boolean[] {domingo.isChecked(), lunes.isChecked(), martes.isChecked(), miercoles.isChecked(), jueves.isChecked(), viernes.isChecked(), sabado.isChecked()};
                    }
                    // Intervalo
                    else if (c.isChecked()) {
                        dias = new boolean[] {false, false, false, false, false, false, false};
                    }

                    nombre = et_nombre.getText().toString();
                    // TODO: ¿Y si el nombre que ha puesto el usuario está vacío? Poner uno por defecto.
                    Intent i = new Intent(getApplicationContext(), SetTimeActivity.class);
                    i.putExtra("nombre", nombre);
                    i.putExtra("day", dp.getDayOfMonth());
                    i.putExtra("month", dp.getMonth() + 1);
                    i.putExtra("year", dp.getYear());
                    i.putExtra("freq", dias);

                    i.putExtra("int_dias", ipicker_num.getValue());
                    i.putExtra("int_tiempo", ipicker_dwm.getValue());
                    startActivity(i);
                    finish();
                }
            });

            btn_camera.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    openGallery();
                }
            });
        }
    }

    public void onRadioButtonClicked(View view) {
        // Is the button now checked?
        boolean checked = ((RadioButton) view).isChecked();

        // Check which radio button was clicked
        switch(view.getId()) {
            case R.id.optionA:
                if (checked) {
                    dias_layout.setVisibility(View.GONE);
                    layout_interval.setVisibility(View.GONE);
                }
                break;
            case R.id.optionB:
                if (checked) {
                    // Días específicos.
                    dias_layout.setVisibility(View.VISIBLE);
                    layout_interval.setVisibility(View.GONE);
                }
                break;
            case R.id.optionC:
                if (checked) {
                    dias_layout.setVisibility(View.GONE);
                    layout_interval.setVisibility(View.VISIBLE);
                }
                break;
        }
    }

    private void openGallery() {
        Intent gallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        startActivityForResult(gallery, PICK_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == PICK_IMAGE){
            imageUri = data.getData();
            MLTextAnalyzer analyzer = MLAnalyzerFactory.getInstance().getLocalTextAnalyzer();
            MLFrame frame = null;
            try {
                frame = MLFrame.fromFilePath(getApplicationContext(), imageUri);
            } catch (IOException e) {
                e.printStackTrace();
            }
            Task<MLText> task = analyzer.asyncAnalyseFrame(frame);
            task.addOnSuccessListener(new OnSuccessListener<MLText>() {
                @Override
                public void onSuccess(MLText text) {
                    // Recognition success.
                    et_nombre.setText(text.getStringValue());
                    nombre = text.getStringValue();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(Exception e) {
                    // Recognition failure.
                    Toast.makeText(ActivityAddFreq.this, R.string.error_reconocimiento, Toast.LENGTH_SHORT).show();
                }
            });
            try {
                if (analyzer != null) {
                    analyzer.stop();
                }
            } catch (IOException e) {
                // Exception handling.
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Solo queremos que ejecute este código si no estamos en el tutorial.
        if (getIntent().getStringExtra("from") == null)
            setWeather();
        else {
            topLayout = findViewById(R.id.top_layout);
            bottomLayout = findViewById(R.id.bottom_layout);
            topLayout.setBackgroundColor(getColor(R.color.colorPrimary));
            bottomLayout.setBackgroundColor(getColor(R.color.colorPrimary));
        }
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
