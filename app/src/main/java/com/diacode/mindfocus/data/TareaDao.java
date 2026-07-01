package com.diacode.mindfocus.data;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface TareaDao {
    @Insert
    long insertar(Tarea tarea);
    @Update
    void actualizar(Tarea tarea);
    @Delete
    void eliminar(Tarea tarea);
    @Query("SELECT * FROM tareas WHERE usuarioId=:usuarioId ORDER BY id DESC")
    List<Tarea> listarTodas(int usuarioId);
    @Query("SELECT * FROM tareas WHERE usuarioId=:usuarioId AND completada=0")
    List<Tarea> listarPendientes(int usuarioId);
    @Query("SELECT * FROM tareas WHERE usuarioId=:usuarioId AND completada=1")
    List<Tarea> listarCompletadas(int usuarioId);
    @Query("SELECT * FROM tareas WHERE id=:id")
    Tarea buscarPorId(int id);
}
