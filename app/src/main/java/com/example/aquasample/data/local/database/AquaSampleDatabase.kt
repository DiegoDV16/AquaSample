package com.example.aquasample.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.aquasample.data.local.converter.Converters
import com.example.aquasample.data.local.dao.CentroCultivoDao
import com.example.aquasample.data.local.dao.MuestraDao
import com.example.aquasample.data.local.dao.RoleDao
import com.example.aquasample.data.local.dao.TramoDao
import com.example.aquasample.data.local.dao.UsuarioDao
import com.example.aquasample.data.local.entity.CentroCultivoEntity
import com.example.aquasample.data.local.entity.MuestraEntity
import com.example.aquasample.data.local.entity.RoleEntity
import com.example.aquasample.data.local.entity.TramoEntity
import com.example.aquasample.data.local.entity.UsuarioEntity
import java.util.concurrent.Executors

@Database(
    entities = [
        RoleEntity::class,
        CentroCultivoEntity::class,
        UsuarioEntity::class,
        MuestraEntity::class,
        TramoEntity::class
    ],
    version = 3,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AquaSampleDatabase : RoomDatabase() {

    abstract fun roleDao(): RoleDao
    abstract fun centroCultivoDao(): CentroCultivoDao
    abstract fun usuarioDao(): UsuarioDao
    abstract fun muestraDao(): MuestraDao
    abstract fun tramoDao(): TramoDao

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
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                    .addCallback(DatabaseCallback(context.applicationContext))
                    .build()
                INSTANCE = instance
                instance
            }
        }

        /**
         * Agrega las marcas de conteo asistido sin borrar los datos existentes.
         * Sustituye al fallback destructivo que tenía la versión 1.
         */
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE muestras ADD COLUMN marcasConteo TEXT NOT NULL DEFAULT '[]'"
                )
                db.execSQL(
                    "ALTER TABLE muestras ADD COLUMN conteoAsistido INTEGER NOT NULL DEFAULT 0"
                )
            }
        }

        /**
         * Crea el catálogo de tramos y enlaza cada muestra con su tramo. Los
         * tramos iniciales replican las combinaciones tren/línea que ya usaban
         * las muestras de demostración.
         */
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS tramos (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        centroId INTEGER NOT NULL,
                        tren TEXT NOT NULL,
                        linea TEXT NOT NULL,
                        longitudCm INTEGER NOT NULL,
                        estado TEXT NOT NULL
                    )
                    """.trimIndent()
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS index_tramos_centroId ON tramos (centroId)")
                db.execSQL("ALTER TABLE muestras ADD COLUMN tramoId INTEGER")

                val tramos = listOf(
                    listOf(1, 1, "Tren 02", "Línea L-03", 145),
                    listOf(2, 1, "Tren 03", "Línea L-01", 150),
                    listOf(3, 2, "Tren 02", "Línea L-02", 160),
                    listOf(4, 2, "Tren 01", "Línea L-03", 155),
                    listOf(5, 3, "Tren 01", "Línea L-01", 140),
                    listOf(6, 3, "Tren 04", "Línea L-04", 165),
                    listOf(7, 1, "Tren 01", "Línea L-02", 150),
                    listOf(8, 2, "Tren 03", "Línea L-04", 170)
                )
                tramos.forEach { (id, centroId, tren, linea, longitud) ->
                    db.execSQL(
                        "INSERT INTO tramos (id, centroId, tren, linea, longitudCm, estado) " +
                            "VALUES ($id, $centroId, '$tren', '$linea', $longitud, 'ACTIVO')"
                    )
                }
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

                // 3. Tramos de demostración (tren, línea y largo por centro)
                val tramos = listOf(
                    TramoEntity(1, 1, "Tren 02", "Línea L-03", 145),
                    TramoEntity(2, 1, "Tren 03", "Línea L-01", 150),
                    TramoEntity(3, 2, "Tren 02", "Línea L-02", 160),
                    TramoEntity(4, 2, "Tren 01", "Línea L-03", 155),
                    TramoEntity(5, 3, "Tren 01", "Línea L-01", 140),
                    TramoEntity(6, 3, "Tren 04", "Línea L-04", 165),
                    TramoEntity(7, 1, "Tren 01", "Línea L-02", 150),
                    TramoEntity(8, 2, "Tren 03", "Línea L-04", 170)
                )

                // 4. Cuentas de demostración (una por rol)
                val usuarios = listOf(
                    UsuarioEntity(
                        id = "USR-001",
                        email = "operador@aquasample.cl",
                        passwordHash = "operador123",
                        nombres = "Francisca",
                        apellidoPaterno = "Rojas",
                        apellidoMaterno = "Soto",
                        rolId = 1,
                        estado = "ACTIVO"
                    ),
                    UsuarioEntity(
                        id = "USR-002",
                        email = "analista@aquasample.cl",
                        passwordHash = "analista123",
                        nombres = "Mateo",
                        apellidoPaterno = "Cárdenas",
                        apellidoMaterno = "Muñoz",
                        rolId = 2,
                        estado = "ACTIVO"
                    ),
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
                        db.tramoDao().insertTramos(tramos)
                        db.usuarioDao().insertUsers(usuarios)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }
}
