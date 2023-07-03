package es.unizar.eina.gestorReservas;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

/**
 * Simple notes database access helper class. Defines the basic CRUD operations
 * for the notepad example, and gives the ability to list all notes as well as
 * retrieve or modify a specific note.
 *
 * This has been improved from the first version of this tutorial through the
 * addition of better error handling and also using returning a Cursor instead
 * of using a collection of inner classes (which is less scalable and not
 * recommended).
 */
//TODO: PONER TODOS LOS PARÄMETROS D TODAS LAS CLASES
public class ReservasDbAdapter {

    public static final String RESERVA_ID = "_id";
    public static final String RESERVA_NOMBREC= "nombre_cliente";
    public static final String RESERVA_TELEFONO_CLIENTE = "telefono_cliente";
    public static final String RESERVA_FECHAE = "fecha_entrada";
    public static final String RESERVA_FECHAS = "fecha_salida";

    //Tabla infoReservas
    public static final String RELACION_ID_HAB = "relacion_id_hab";
    public static final String RELACION_ID_RESERVA = "relacion_id_reserva";
    public static final String RELACION_MAX_OCUPANTES = "max_ocupantes_relacion";
    public static final String RELACION_CANTIDAD_HAB= "cantidad_habitaciones_relacion";

    private DatabaseHelper mDbHelper;
    private SQLiteDatabase mDb;

    private static final String DATABASE_TABLE = "reserva";
    public static final String DATABASE_INFO_RESERVA = "info_reserva";


    private final Context mCtx;

    /**
     * Constructor - takes the context to allow the database to be
     * opened/created
     *
     * @param ctx the Context within which to work
     */
    public ReservasDbAdapter(Context ctx) {
        this.mCtx = ctx;
    }

    /**
     * Open the notes database. If it cannot be opened, try to create a new
     * instance of the database. If it cannot be created, throw an exception to
     * signal the failure
     *
     * @return this (self reference, allowing this to be chained in an
     *         initialization call)
     * @throws SQLException if the database could be neither opened or created
     */
    public ReservasDbAdapter open() throws SQLException {
        mDbHelper = new DatabaseHelper(mCtx);
        mDb = mDbHelper.getWritableDatabase();
        return this;
    }

    public void close() {
        mDbHelper.close();
    }


    /**
     * Create a new note using the title and body provided. If the note is
     * successfully created return the new rowId for that note, otherwise return
     * a -1 to indicate failure.
     *
     */
    public long createReserva(String nombre_cliente, String numero_cliente,
                              String fecha_entrada, String fecha_salida ) {
        if(nombre_cliente.isEmpty() || nombre_cliente == null || numero_cliente == null || numero_cliente.isEmpty()
                || fecha_entrada == null || fecha_entrada.isEmpty() || fecha_salida == null || fecha_salida.isEmpty()){
            return -1;
        }
        ContentValues initialValues = new ContentValues();
        initialValues.put(RESERVA_NOMBREC, nombre_cliente);
        initialValues.put(RESERVA_TELEFONO_CLIENTE, numero_cliente);
        initialValues.put(RESERVA_FECHAE, fecha_entrada);
        initialValues.put(RESERVA_FECHAS, fecha_salida);

        return mDb.insert(DATABASE_TABLE, null, initialValues);
    }

    /**
     * Delete the note with the given rowId
     *
     * @param rowId id of note to delete
     * @return true if deleted, false otherwise
     */
    public boolean deleteHabitacion(long rowId) {
        if (rowId < 1){
            return  false;
        }
        return mDb.delete(DATABASE_TABLE, RESERVA_ID + "=" + rowId, null) > 0;
    }

    /**
     * Return a Cursor over the list of all notes in the database
     * @param orderBy criterio segun el que se ordenan las habitaciones
     * @return Cursor over all notes
     */
    public Cursor fetchAllReservas(String orderBy) {
        switch (orderBy) {
            case RESERVA_NOMBREC:
                return mDb.query(DATABASE_TABLE, new String[]{
                        RESERVA_ID, RESERVA_NOMBREC, RESERVA_TELEFONO_CLIENTE, RESERVA_FECHAE,
                        RESERVA_FECHAS}, null, null, null, null, RESERVA_NOMBREC);
            case RESERVA_TELEFONO_CLIENTE:
                return mDb.query(DATABASE_TABLE, new String[]{
                        RESERVA_ID, RESERVA_NOMBREC, RESERVA_TELEFONO_CLIENTE, RESERVA_FECHAE,
                        RESERVA_FECHAS}, null, null, null, null, RESERVA_TELEFONO_CLIENTE);
            case RESERVA_FECHAE:
                return mDb.query(DATABASE_TABLE, new String[]{
                        RESERVA_ID, RESERVA_NOMBREC, RESERVA_TELEFONO_CLIENTE, RESERVA_FECHAE,
                        RESERVA_FECHAS}, null, null, null, null, RESERVA_FECHAE);
            default:
                return mDb.query(DATABASE_TABLE, new String[]{
                        RESERVA_ID, RESERVA_NOMBREC, RESERVA_TELEFONO_CLIENTE, RESERVA_FECHAE,
                        RESERVA_FECHAS}, null, null, null, null, RESERVA_ID);
        }
    }

    /**
     * Return a Cursor positioned at the note that matches the given rowId
     *
     * @param rowId id of note to retrieve
     * @return Cursor positioned to matching note, if found
     * @throws SQLException if note could not be found/retrieved
     */
    public Cursor fetchReserva(long rowId) throws SQLException {

        Cursor mCursor =
                mDb.query(DATABASE_TABLE, new String[]{
                                RESERVA_ID, RESERVA_NOMBREC, RESERVA_TELEFONO_CLIENTE, RESERVA_FECHAE,
                                RESERVA_FECHAS}, RESERVA_ID + "=" + rowId, null, null,
                        null,null, null);

        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;

    }

    /**
     * Update the note using the details provided. The note to be updated is
     * specified using the rowId, and it is altered to use the title and body
     * values passed in
     */
    public boolean updateReserva(long rowId, String nombre_cliente, String numero_cliente,
                                 String fecha_entrada, String fecha_salida) {
        if(nombre_cliente.isEmpty() || nombre_cliente == null || numero_cliente == null || numero_cliente.isEmpty()
                || fecha_entrada == null || fecha_entrada.isEmpty() || fecha_salida == null || fecha_salida.isEmpty()){
            return false;
        }
        ContentValues args = new ContentValues();
        args.put(RESERVA_NOMBREC, nombre_cliente);
        args.put(RESERVA_TELEFONO_CLIENTE, numero_cliente);
        args.put(RESERVA_FECHAE, fecha_entrada);
        args.put(RESERVA_FECHAS, fecha_salida);

        return mDb.update(DATABASE_TABLE, args, RESERVA_ID + "=" + rowId, null) > 0;
    }

    public long aumentarHabitacion(long id_reserva, long id_hab, int cantidadHabitaciones ) {
        ContentValues args = new ContentValues();
        args.put(RELACION_ID_HAB, id_hab);
        args.put(RELACION_ID_RESERVA, id_reserva);
        args.put(RELACION_CANTIDAD_HAB, cantidadHabitaciones);
        return mDb.insert(DATABASE_INFO_RESERVA, null, args);
    }

    public boolean eliminarHabitacion(long id_reserva, long id_hab){
        return mDb.delete(DATABASE_INFO_RESERVA, RELACION_ID_RESERVA + "=? and " + RELACION_ID_HAB + "=?",
                new String[]{Long.toString(id_reserva), Long.toString(id_hab)}) > 0;

    }

    public boolean actualizarHabitacion(long id_reserva, long id_hab, int cantidadHabitaciones ){
        ContentValues args = new ContentValues();
        args.put(RELACION_CANTIDAD_HAB, cantidadHabitaciones);

        return mDb.update(DATABASE_INFO_RESERVA, args,
                RELACION_ID_RESERVA + "=? and " + RELACION_ID_HAB + "=?",
                new String[]{Long.toString(id_reserva), Long.toString(id_hab)}) > 0;
    }

    public boolean deleteReserva(long rowId) {
        return mDb.delete(DATABASE_INFO_RESERVA, RESERVA_ID + "=" + rowId, null) > 0;
    }

    public boolean deleteReserva(String nombre_cliente) {
        return mDb.delete(DATABASE_INFO_RESERVA, RESERVA_NOMBREC + "='" + nombre_cliente + "'", null) > 0;
    }

}