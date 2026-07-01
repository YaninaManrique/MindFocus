package com.diacode.mindfocus.data;

import androidx.room.TypeConverter;

public class TipoTareaConverter {
    @TypeConverter
    public static TipoTarea toTipo(String value){
        return value == null ? null : TipoTarea.valueOf(value);
    }
    @TypeConverter
    public static String fromTipo(TipoTarea tipo){
        return tipo == null ? null : tipo.name();
    }
}
