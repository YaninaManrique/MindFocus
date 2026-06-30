package com.diacode.mindfocus.data;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "usuarios")
public class Usuario {
    @PrimaryKey(autoGenerate = true)
    private int id;

    @NonNull
    @ColumnInfo(name = "email")
    private String email;

    // guardamos el HASH de la contrasena, nunca el texto plano
    @NonNull
    @ColumnInfo(name = "password_hash")
    private String passwordHash;

    @ColumnInfo(name = "nombre")
    private String nombre;

    public Usuario(@NonNull String email, @NonNull String passwordHash, String nombre) {
        this.email = email;
        this.passwordHash = passwordHash;
        this.nombre = nombre;
    }

    // getters y setters (Room los necesita)
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    @NonNull
    public String getEmail() { return email; }
    public void setEmail(@NonNull String email) { this.email = email; }

    @NonNull
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(@NonNull String passwordHash) { this.passwordHash = passwordHash; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
}
