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
    @Query("SELECT * FROM tareas WHERE usuarioId=:usuarioId AND completada=0 ORDER BY id DESC LIMIT 1")
    Tarea obtenerTareaActual(int usuarioId);
    //consultas para el progreso
    @Query("SELECT COUNT(*) FROM tareas WHERE usuarioId=:usuarioId")
    int contarTodas(int usuarioId);
    @Query("SELECT COUNT(*) FROM tareas WHERE usuarioId=:usuarioId AND completada=1")
    int contarCompletadas(int usuarioId);
    //para el reporte
    @Query("SELECT COUNT(*) FROM tareas WHERE usuarioId=:usuarioId AND tipo='ESTUDIO'")
    int contarEstudio(int usuarioId);
    @Query("SELECT COUNT(*) FROM tareas WHERE usuarioId=:usuarioId AND tipo='TRABAJO'")
    int contarTrabajo(int usuarioId);
    @Query("SELECT COUNT(*) FROM tareas WHERE usuarioId=:usuarioId AND tipo='HOGAR'")
    int contarHogar(int usuarioId);
    @Query("SELECT COUNT(*) FROM tareas WHERE usuarioId=:usuarioId AND tipo='EJERCICIO'")
    int contarEjercicio(int usuarioId);
    @Query("SELECT COUNT(*) FROM tareas WHERE usuarioId=:usuarioId AND tipo='CREATIVO'")
    int contarCreativo(int usuarioId);
    @Query("SELECT COUNT(*) FROM tareas WHERE usuarioId=:usuarioId AND prioridad='ALTA'")
    int contarAlta(int usuarioId);
    @Query("SELECT COUNT(*) FROM tareas WHERE usuarioId=:usuarioId AND prioridad='MEDIA'")
    int contarMedia(int usuarioId);
    @Query("SELECT COUNT(*) FROM tareas WHERE usuarioId=:usuarioId AND prioridad='BAJA'")
    int contarBaja(int usuarioId);
    //para las barras de progreso
    @Query("SELECT COUNT(*) FROM tareas WHERE usuarioId=:usuarioId AND tipo='ESTUDIO'")
    int totalEstudio(int usuarioId);
    @Query("SELECT COUNT(*) FROM tareas WHERE usuarioId=:usuarioId AND tipo='ESTUDIO' AND completada=1")
    int estudioCompletadas(int usuarioId);
    @Query("SELECT COUNT(*) FROM tareas WHERE usuarioId=:usuarioId AND tipo='EJERCICIO'")
    int totalEjercicio(int usuarioId);
    @Query("SELECT COUNT(*) FROM tareas WHERE usuarioId=:usuarioId AND tipo='EJERCICIO' AND completada=1")
    int ejercicioCompletadas(int usuarioId);
    @Query("SELECT COUNT(*) FROM tareas WHERE usuarioId=:usuarioId AND tipo='HOGAR'")
    int totalHogar(int usuarioId);
    @Query("SELECT COUNT(*) FROM tareas WHERE usuarioId=:usuarioId AND tipo='HOGAR' AND completada=1")
    int hogarCompletadas(int usuarioId);
    @Query("SELECT COUNT(*) FROM tareas WHERE usuarioId=:usuarioId AND tipo='TRABAJO'")
    int totalTrabajo(int usuarioId);
    @Query("SELECT COUNT(*) FROM tareas WHERE usuarioId=:usuarioId AND tipo='TRABAJO' AND completada=1")
    int trabajoCompletadas(int usuarioId);
    @Query("SELECT COUNT(*) FROM tareas WHERE usuarioId=:usuarioId AND tipo='CREATIVO'")
    int totalCreativo(int usuarioId);
    @Query("SELECT COUNT(*) FROM tareas WHERE usuarioId=:usuarioId AND tipo='CREATIVO' AND completada=1")
    int creativoCompletadas(int usuarioId);
}
