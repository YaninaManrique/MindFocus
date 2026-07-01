package com.diacode.mindfocus;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.diacode.mindfocus.data.AppDatabase;
import com.diacode.mindfocus.data.Usuario;
import com.diacode.mindfocus.utils.PasswordUtils;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button btnLogin;
    private TextView tvIrRegistro;
    //para el guardado local
    private SharedPreferences prefs;
    private AppDatabase db;
    private final Executor executor = Executors.newSingleThreadExecutor();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        db = AppDatabase.getInstance(this);
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

        // La consulta a Room va en otro hilo
        executor.execute(() -> {
            Usuario usuario = db.usuarioDao().buscarPorEmail(email);

            // Volvemos al hilo principal para actualizar UI / navegar
            runOnUiThread(() -> {
                if (usuario == null) {
                    Toast.makeText(this, "Correo o contraseña incorrectos", Toast.LENGTH_SHORT).show();
                    return;
                }

                boolean passwordCorrecta = PasswordUtils.verificarPassword(
                        password, usuario.getPasswordHash()
                );

                if (passwordCorrecta) {
                    Log.d("LOGIN", "Email usuario: " + usuario.getEmail());
                    // Login correcto
                    prefs.edit()
                            .putBoolean("sesionActiva", true)
                            .putInt("usuarioId", usuario.getId())
                            .putString("nombre", usuario.getNombre())
                            .putString("email", usuario.getEmail())
                            .apply();

                    Toast.makeText(this, "¡Bienvenido!", Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(this, ActivityPrincipal.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                } else {
                    Toast.makeText(this, "Correo o contraseña incorrectos", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

}
