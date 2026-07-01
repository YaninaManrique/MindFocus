package com.diacode.mindfocus.data;

import androidx.room.TypeConverter;

public class PrioridadConverter {
    @TypeConverter
    public static Prioridad toPrioridad(String value){
        return value == null ? null : Prioridad.valueOf(value);
    }
    @TypeConverter
    public static String fromPrioridad(Prioridad prioridad){
        return prioridad == null ? null : prioridad.name();
    }
}
