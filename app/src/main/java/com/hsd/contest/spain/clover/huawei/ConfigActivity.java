package com.hsd.contest.spain.clover.huawei;

import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

public class ConfigActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config);

        // Carga el sprite.
        ImageView img = findViewById(R.id.imageView);
        img.setBackgroundResource(R.drawable.anim_walk);

        // Carga el cuadro de diálogo.
        String[] welcome_dialog = getResources().getStringArray(R.array.welcome_dialog);
        TypeWriter tw = findViewById(R.id.textView);
        tw.setText("");
        tw.setCharacterDelay((long) 35);
        final int[] i = {0}; // Itera sobre las frases.
        tw.animateText(welcome_dialog[i[0]]);
        i[0]++;

        final String[] nombre = {""};

        EditText et_nombre = findViewById(R.id.editTextTextPersonName);
        et_nombre.setVisibility(View.INVISIBLE);

        ImageButton btn = findViewById(R.id.next_btn);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btn.setVisibility(View.INVISIBLE);
                // Si aún no hemos recogido el nombre.
                if (et_nombre.getText().toString().isEmpty()) {
                    tw.setText("");
                    tw.animateText(welcome_dialog[i[0]]);
                    i[0]++;

                    if (i[0] == welcome_dialog.length - 2) {
                        Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            public void run() {
                                tw.setVisibility(View.INVISIBLE);
                                et_nombre.setVisibility(View.VISIBLE);
                            }
                        }, 5000);
                    }
                    else {
                        btn.setVisibility(View.VISIBLE);
                    }
                }
                else {
                    // Iniciamos la secuencia para salir de la actividad y enseñar al usuario el menú principal en la siguiente.
                    nombre[0] = et_nombre.getText().toString();
                    et_nombre.setVisibility(View.INVISIBLE);
                    tw.setVisibility(View.VISIBLE);
                    tw.animateText(welcome_dialog[i[0]] + nombre[0] + welcome_dialog[i[0] + 1]);

                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        public void run() {
                            // TODO: Transición entre Activities más fluidas. Usar animaciones.
                            // TODO: Antes de llamar al Intent, hacer que Clover corra fuera de la pantalla.
                            Intent i = new Intent(getApplicationContext(), MainActivity.class);
                            i.putExtra("nombre", nombre[0]);
                            startActivityForResult(i, 1);
                            finish();
                        }
                    }, 5500);
                }
            }
        });

        et_nombre.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(s.toString().trim().length()==0){
                    btn.setVisibility(View.INVISIBLE);
                } else {
                    btn.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // TODO Auto-generated method stub
            }
        });

        // Get the background, which has been compiled to an AnimationDrawable object.
        AnimationDrawable frameAnimation = (AnimationDrawable) img.getBackground();

        // Start the animation (looped playback by default).
        frameAnimation.start();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }
}