package com.diacode.mindfocus.data;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

@Dao
public interface UsuarioDao {
    @Insert
    long insertar(Usuario usuario);

    // para login: buscamos por email
    @Query("SELECT * FROM usuarios WHERE email = :email LIMIT 1")
    Usuario buscarPorEmail(String email);

    // para validar que no exista ya en el registro
    @Query("SELECT COUNT(*) FROM usuarios WHERE email = :email")
    int existeEmail(String email);
}
