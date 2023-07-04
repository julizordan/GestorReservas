package es.unizar.eina.gestorReservas;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "debGestorReservas";
    private static final int DATABASE_VERSION = 6;

    private static final String TAG = "DatabaseHelper";

    /**
     * Database creation sql statement
     */
    private static final String DATABASE_CREATE_HABITACION =
            "create table habitacion (_id integer primary key autoincrement, "+
                    "descripcion text not null, MAX_OCUPANTES integer not null,"+
                    "precioNoche double not null,porcentaje_recargo double not null);";
    private static final String DATABASE_CREATE_RESERVA =
            "create table reserva (_id integer primary key autoincrement, "+
                    " nombre_cliente text not null, telefono_cliente text not null,"+
                    " fecha_entrada text not null,fecha_salida text not null);";
    private static final String DATABASE_CREATE_INFOHABITACION =
            "create table infoHabitacion (id_habitacion integer not null, id_reserva integer not null, " +
                    " precioCompra double not null, cantidadNoches integer not null," +
                    " foreign key(id_habitacion) references habitacion(_id), " +
                    " foreign key(id_reserva) references reserva(_id) ON DELETE CASCADE);";

    DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("PRAGMA foreign_keys=ON");
        db.execSQL(DATABASE_CREATE_HABITACION);
        db.execSQL(DATABASE_CREATE_RESERVA);
        db.execSQL(DATABASE_CREATE_INFOHABITACION);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                + newVersion + ", which will destroy all old data");
        db.execSQL("DROP TABLE IF EXISTS habitacion");
        db.execSQL("DROP TABLE IF EXISTS reserva");
        db.execSQL("DROP TABLE IF EXISTS infoHabitacion");
        onCreate(db);
    }
}