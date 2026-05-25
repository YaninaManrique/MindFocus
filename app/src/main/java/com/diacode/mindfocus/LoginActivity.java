package com.diacode.mindfocus;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button btnLogin;
    private TextView tvIrRegistro;
    //para el guardado local
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //inicializamos SharedPreferences
        prefs = getSharedPreferences("MindFocusPrefs", MODE_PRIVATE);

        etEmail     = findViewById(R.id.etEmail);
        etPassword  = findViewById(R.id.etPassword);
        btnLogin    = findViewById(R.id.btnLogin);
        tvIrRegistro = findViewById(R.id.tvIrRegistro);

        btnLogin.setOnClickListener(v -> intentarLogin());

        tvIrRegistro.setOnClickListener(v -> {
            startActivity(new Intent(this, RegistroActivity.class));
        });
    }

    private void intentarLogin() {
        String email    = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        //validar campos vacios
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        //recuperamos datos guardados en el registro
        String emailGuardado    = prefs.getString("email", "");
        String passwordGuardado = prefs.getString("password", "");

        //comparamos
        if (email.equals(emailGuardado) && password.equals(passwordGuardado)) {
            //login correcto — guardamos sesión activa
            prefs.edit().putBoolean("sesionActiva", true).apply();

            Toast.makeText(this, "¡Bienvenido!", Toast.LENGTH_SHORT).show();

            //vamos a al app
            Intent intent = new Intent(this, ActivityPrincipal.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        } else {
            Toast.makeText(this, "Correo o contraseña incorrectos", Toast.LENGTH_SHORT).show();
        }
    }

}
