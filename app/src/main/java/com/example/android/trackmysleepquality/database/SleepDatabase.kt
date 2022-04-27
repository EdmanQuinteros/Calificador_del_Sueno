package com.example.android.trackmysleepquality.database

import android.content.Context
import android.provider.CalendarContract
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/*
* Proporcione el SleepNightcomo el único elemento con la lista de entities.
* Establecer version como 1. Cada vez que cambie el esquema, deberá aumentar el número de versión.
* Establézcalo exportSchemaen false, para no mantener copias de seguridad del historial de versiones del esquema.
 */
@Database(entities = [SleepNight::class], version = 1, exportSchema = false)
abstract class SleepDatabase : RoomDatabase(){
    abstract val sleepDatabaseDao: SleepDatabaseDao

    companion object {
        //INSTANCE mantendrá una referencia a la db, cuando se haya creado una. Esto le ayuda a evitar abrir repetidamente conexiones a la base de datos
        @Volatile
        private var INSTANCE: SleepDatabase? = null

        fun getInstance(context: Context): SleepDatabase {
            //synchronizedsignifica que solo un hilo de ejecución a la vez puede ingresar a este bloque de código
            synchronized(this){
                var instance = INSTANCE

                if (instance == null){
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        SleepDatabase::class.java,
                        "sleep_history_database").fallbackToDestructiveMigration().build()  //destruir y reconstruir la base de datos

                    INSTANCE = instance
                }
                return instance
            }
        }
    }
}