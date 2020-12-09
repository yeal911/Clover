package com.hsd.contest.spain.clover.huawei;

import android.Manifest;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RemoteViews;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.amlcurran.showcaseview.ShowcaseView;
import com.github.amlcurran.showcaseview.targets.ActionViewTarget;
import com.github.amlcurran.showcaseview.targets.ViewTarget;
import com.github.matteobattilana.weather.PrecipType;
import com.github.matteobattilana.weather.WeatherView;
import com.huawei.hms.ads.AdParam;
import com.huawei.hms.ads.BannerAdSize;
import com.huawei.hms.ads.HwAds;
import com.huawei.hms.ads.banner.BannerView;
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
import com.huawei.hms.maps.model.LatLng;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.TimeZone;


public class MainActivity extends AppCompatActivity  {

    private ConstraintLayout mainLayout, cloverLayout;
    private TypeWriter clover_dialog;
    private String[] dialogs_noon, dialogs_dusk, dialogs_night;
    private Usuario usuario;
    private String nombre;
    private HiAnalyticsInstance instance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // ADS KIT.
        HwAds.init(this);
        BannerView bannerView = new BannerView(this);
        bannerView.setAdId("testw6vs28auh3");
        bannerView.setBannerAdSize(BannerAdSize.BANNER_SIZE_320_50);
        FrameLayout adFrameLayout = findViewById(R.id.hw_banner_view);
        adFrameLayout.addView(bannerView);
        AdParam adParam = new AdParam.Builder().build();
        bannerView.loadAd(adParam);

        // ANALYTICS KIT.
        HiAnalyticsTools.enableLog();
        instance = HiAnalytics.getInstance(this);

        mainLayout = findViewById(R.id.top_layout);
        cloverLayout = findViewById(R.id.clover_layout);

        // Carga el cielo predeterminado y los paquetes de diálogos.
        mainLayout.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.day_sky_top));
        cloverLayout.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.day_sky));

        clover_dialog = findViewById(R.id.dialog);
        dialogs_noon = getResources().getStringArray(R.array.main_dialogs_noon);
        dialogs_dusk = getResources().getStringArray(R.array.main_dialogs_dusk);
        dialogs_night = getResources().getStringArray(R.array.main_dialogs_night);

        setWeather();

        Button add_btn, check_btn;
        add_btn = findViewById(R.id.btn_add);
        add_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(), ActivityAddFreq.class);
                startActivity(i);
            }
        });

        check_btn = findViewById(R.id.btn_check);
        check_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(), DosisManagerActivity.class);
                i.putExtra("dosisList", usuario.getDosis());
                startActivity(i);
            }
        });

        // Carag y lanza la animación de Clover.
        ImageView img = findViewById(R.id.clover_main);
        img.setBackgroundResource(R.drawable.anim_idle);
        AnimationDrawable frameAnimation = (AnimationDrawable) img.getBackground();
        frameAnimation.start();

        // Conseguimos el nombre del usuario.
        nombre = getIntent().getStringExtra("nombre");

        ObjectMapper objectMapper = new ObjectMapper(); // Nos permite crear y restaurar el JSON de la configuración.
        // Restauramos el archivo de configuración. Si no existe o está dañado, continuamos el tutorial.
        try {
            usuario = objectMapper.readValue(new File(getApplicationContext().getFilesDir() + "/config.json"), Usuario.class);
        } catch (IOException e) {
            // TODO: Que Clover venga corriendo desde la pantalla anterior.
            e.printStackTrace();
            usuario = new Usuario(nombre);
            crearJSON(objectMapper, usuario);

            // ¡No queremos que se interrumpa el tutorial tocando en los botones!
            add_btn.setEnabled(false);
            check_btn.setEnabled(false);

            ShowcaseView scv = new ShowcaseView.Builder(this)
                    .setTarget(new ViewTarget(add_btn))
                    .setContentTitle("Añadir tratamiento")
                    .setContentText("Cuando quieras pedirme que me acuerde de un tratamiento, pulsa este botón. ¡Mira, lo voy a apretar yo por ti!")
                    .setStyle(R.style.CustomShowcaseTheme)
                    .build();
            scv.overrideButtonClick(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent i = new Intent(getApplicationContext(), ActivityAddFreq.class);
                    i.putExtra("from", "Tutorial");
                    startActivity(i);
                }
            });
        }

        if (getIntent().getStringExtra("from") != null) {
            if (getIntent().getStringExtra("from").equals("Tutorial2")) {
                // ¡No queremos que se interrumpa el tutorial tocando en los botones!
                add_btn.setEnabled(false);
                check_btn.setEnabled(false);
                ShowcaseView scv2 = new ShowcaseView.Builder(this)
                        .setTarget(new ViewTarget(check_btn))
                        .setContentTitle("Consultar tratamientos y buscar farmacias")
                        .setContentText("Aquí aparecerán los tratamientos que me hayas dicho que recuerde. Si te quedas sin medicinas, también te diré dónde encontrar una farmacia para encontrar más.")
                        .setStyle(R.style.CustomShowcaseTheme2)
                        .build();
                scv2.overrideButtonClick(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        scv2.hide();
                        clover_dialog.setText("");
                        clover_dialog.setCharacterDelay((long) 35);
                        clover_dialog.animateText("¡Y hasta aquí todo lo que necesitas que saber! ¿Por qué no añades tu primer tratamiento? ¡Seguro que lo haces genial!");
                        add_btn.setEnabled(true);
                        check_btn.setEnabled(true);
                    }
                });
            }

        }
    }

    // Método auxiliar que escribe los datos del usuario en el archivo JSON.
    private void crearJSON(ObjectMapper objectMapper, Usuario usuario) {
        try {
            objectMapper.writeValue(new File(getApplicationContext().getFilesDir(),"config.json"), usuario);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

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

    /*
    Utiliza el Awareness Kit para obtener el tiempo meteorológico actual y la hora del día.
     */
    public void setWeather() {
        WeatherView weatherView = findViewById(R.id.weather_view);
        int weatherId = getWeatherId();
        int dialog_number = (int) (Math.random() * 5);
        Bundle bundle = new Bundle();
        class TimeBarrierReceiver extends BroadcastReceiver {
            @Override
            public void onReceive(Context context, Intent intent) {
                BarrierStatus barrierStatus = BarrierStatus.extract(intent);
                String label = barrierStatus.getBarrierLabel();
                // DUSK
                if (label.equals("Dusk barrier")) {
                    if (barrierStatus.getPresentStatus() == BarrierStatus.TRUE) {
                        mainLayout.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.red_sky));
                        cloverLayout.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.red_sky_top));
                        bundle.putString("TIME_BARRIER", "dusk");

                        clover_dialog.setText("");
                        clover_dialog.setCharacterDelay((long) 35);
                        clover_dialog.animateText(dialogs_dusk[dialog_number]);
                    }
                }
                // NIGHT
                // Se usan 2 barreras: una para antes de las 00:00 y otra para después.
                else if (label.equals("Night barrier 1") || label.equals("Night barrier 2")) {
                    if (barrierStatus.getPresentStatus() == BarrierStatus.TRUE) {
                        mainLayout.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.night_sky_top));
                        cloverLayout.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.night_sky));
                        bundle.putString("TIME_BARRIER", "night");

                        clover_dialog.setText("");
                        clover_dialog.setCharacterDelay((long) 35);
                        clover_dialog.animateText(dialogs_night[dialog_number]);
                    }
                }
                // NOON
                else {
                    if (barrierStatus.getPresentStatus() == BarrierStatus.TRUE) {
                        // CLEAR
                        if (weatherId == 1 || weatherId == 2 || weatherId == 3 || weatherId == 14 || weatherId == 17 || weatherId == 21 || weatherId == 30 || weatherId == 32 || weatherId == 33 || weatherId == 34) {
                            mainLayout.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.day_sky_top));
                            cloverLayout.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.day_sky));
                            bundle.putString("TIME_BARRIER", "noon_sunny");
                        }
                        // CLOUDY
                        else if (weatherId == 4 || weatherId == 5 || weatherId == 6 || weatherId == 7 || weatherId == 8 || weatherId == 11 || weatherId == 13 || weatherId == 16 || weatherId == 20 || weatherId == 23 || weatherId == 24 || weatherId == 25 || weatherId == 26 || weatherId == 29 || weatherId == 31 || weatherId == 35 || weatherId == 36 || weatherId == 38 || weatherId == 39 || weatherId == 40 || weatherId == 41 || weatherId == 42 || weatherId == 43 || weatherId == 44) {
                            mainLayout.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.day_sky_cloudy_top));
                            cloverLayout.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.day_sky_cloudy ));
                            bundle.putString("TIME_BARRIER", "noon_cloudy");
                        }

                        clover_dialog.setText("");
                        clover_dialog.setCharacterDelay((long) 35);
                        clover_dialog.animateText(dialogs_noon[dialog_number]);
                    }
                }
            }
        }

        // Pedimos permisos.
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 44);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 45);

        // Definimos las barreras de tiempo.
        long oneHourMilliSecond = 60 * 60 * 1000L;
        AwarenessBarrier noon = TimeBarrier.duringPeriodOfDay(TimeZone.getDefault(),
                8 * oneHourMilliSecond, 19 * oneHourMilliSecond);

        AwarenessBarrier dusk = TimeBarrier.duringPeriodOfDay(TimeZone.getDefault(),
                19 * oneHourMilliSecond, 21 * oneHourMilliSecond);

        AwarenessBarrier night_1 = TimeBarrier.duringPeriodOfDay(TimeZone.getDefault(),
                21 * oneHourMilliSecond, 24 * oneHourMilliSecond);

        AwarenessBarrier night_2 = TimeBarrier.duringPeriodOfDay(TimeZone.getDefault(),
                0, 8 * oneHourMilliSecond);

        // Y las cargamos.
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

        // En función del tiempo meteorológico, activamos o no la animación de precipitaciones.
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

        // Para fines estadísticos, creamos un evento en el Analytics Kit.
        instance.onEvent("SET_WEATHER", bundle);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Si se finaliza el proceso de guardado de un tratamiento (por ejemplo) y cambia el tiempo o la hora, necesitamos actualizar.
        setWeather();

        // Cuando vengamos de la actividad SetTimeActivity, informamos al usuario de que su tratamiento ha sido procesado y guardado con éxito.
        if (getIntent().getStringExtra("from") != null) {
            if (getIntent().getStringExtra("from").equals("SetTimeActivity")) {
                ObjectMapper objectMapper = new ObjectMapper();
                try {
                    if (usuario == null)
                        usuario = objectMapper.readValue(new File(getApplicationContext().getFilesDir() + "/config.json"), Usuario.class);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                usuario.addDosis(getIntent().getParcelableExtra("dosis"));
                crearJSON(objectMapper, usuario);
                Toast.makeText(this, R.string.guardado_exito, Toast.LENGTH_SHORT).show();
            }
        }
    }
}
