package com.diacode.mindfocus.data;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "pasos",
        foreignKeys = @ForeignKey(
                entity = Tarea.class,
                parentColumns = "id",
                childColumns = "tareaId",
                onDelete = ForeignKey.CASCADE
        ),
        indices = {@Index("tareaId")}
)
public class Paso {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private int tareaId;
    private String descripcion;
    private boolean completado;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getTareaId() {
        return tareaId;
    }

    public void setTareaId(int tareaId) {
        this.tareaId = tareaId;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public boolean isCompletado() {
        return completado;
    }

    public void setCompletado(boolean completado) {
        this.completado = completado;
    }
}
