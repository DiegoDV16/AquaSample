package com.example.aquasample.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.aquasample.data.local.converter.Converters
import com.example.aquasample.data.local.dao.CentroCultivoDao
import com.example.aquasample.data.local.dao.MuestraDao
import com.example.aquasample.data.local.dao.RoleDao
import com.example.aquasample.data.local.dao.SampleDao
import com.example.aquasample.data.local.dao.UsuarioDao
import com.example.aquasample.data.local.entity.CentroCultivoEntity
import com.example.aquasample.data.local.entity.MuestraEntity
import com.example.aquasample.data.local.entity.RoleEntity
import com.example.aquasample.data.local.entity.UsuarioEntity
import java.util.concurrent.Executors

@Database(
    entities = [
        RoleEntity::class,
        CentroCultivoEntity::class,
        UsuarioEntity::class,
        MuestraEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AquaSampleDatabase : RoomDatabase() {

    abstract fun roleDao(): RoleDao
    abstract fun centroCultivoDao(): CentroCultivoDao
    abstract fun usuarioDao(): UsuarioDao
    abstract fun muestraDao(): MuestraDao

    fun sampleDao(): SampleDao = muestraDao()

    companion object {
        @Volatile
        private var INSTANCE: AquaSampleDatabase? = null

        fun getInstance(context: Context): AquaSampleDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AquaSampleDatabase::class.java,
                    "aquasample_db"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(DatabaseCallback(context.applicationContext))
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(private val context: Context) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                Executors.newSingleThreadExecutor().execute {
                    try {
                        val database = getInstance(context)
                        populateInitialData(database)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }

            private fun populateInitialData(db: AquaSampleDatabase) {
                // 1. Roles iniciales
                val roles = listOf(
                    RoleEntity(
                        id = 1,
                        nombre = "OPERADOR",
                        descripcion = "Operador de Terreno para captura de muestras"
                    ),
                    RoleEntity(
                        id = 2,
                        nombre = "ANALISTA",
                        descripcion = "Analista de laboratorio e inspección técnica"
                    ),
                    RoleEntity(
                        id = 3,
                        nombre = "ADMINISTRADOR",
                        descripcion = "Administrador general del sistema y gestión de usuarios"
                    )
                )

                // 2. Centros de cultivo ficticios
                val centros = listOf(
                    CentroCultivoEntity(
                        id = 1,
                        codigo = "CONC-104",
                        nombre = "Concesión Dalcahue #104",
                        comuna = "Dalcahue",
                        estado = "ACTIVO"
                    ),
                    CentroCultivoEntity(
                        id = 2,
                        codigo = "CONC-208",
                        nombre = "Centro Canal Lemuy #208",
                        comuna = "Puqueldón",
                        estado = "ACTIVO"
                    ),
                    CentroCultivoEntity(
                        id = 3,
                        codigo = "CONC-305",
                        nombre = "Concesión Golfo Ancud #305",
                        comuna = "Castro",
                        estado = "ACTIVO"
                    )
                )

                // 3. Único usuario administrador del sistema por defecto
                val usuarios = listOf(
                    UsuarioEntity(
                        id = "USR-ADMIN",
                        email = "admin@aquasample.cl",
                        passwordHash = "admin123",
                        nombres = "Administrador",
                        apellidoPaterno = "Sistema",
                        apellidoMaterno = "",
                        rolId = 3,
                        estado = "ACTIVO"
                    )
                )

                kotlinx.coroutines.runBlocking {
                    try {
                        db.roleDao().insertRoles(roles)
                        db.centroCultivoDao().insertCentros(centros)
                        db.usuarioDao().insertUsers(usuarios)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }
}
