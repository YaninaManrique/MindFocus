package com.diacode.mindfocus.data;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface PasoDao {
    @Insert
    void insertar(Paso paso);
    @Insert
    void insertarTodos(List<Paso> pasos);
    @Update
    void actualizar(Paso paso);
    @Delete
    void eliminar(Paso paso);
    @Query("SELECT * FROM pasos WHERE tareaId = :tareaId")
    List<Paso> listarPorTarea(int tareaId);
    @Query("SELECT COUNT(*) FROM pasos WHERE tareaId = :tareaId")
    int contarPasos(int tareaId);
}
