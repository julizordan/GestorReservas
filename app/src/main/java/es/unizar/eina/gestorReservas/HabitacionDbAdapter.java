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
public class HabitacionDbAdapter {

    public static final String HAB_ID = "_id";
    public static final String HAB_DESCRIPCION = "descripcion";
    public static final String HAB_MAX_OCUPANTES = "MAX_OCUPANTES";
    public static final String HAB_PRECIO_NOCHE = "precioNoche";
    public static final String HAB_PORCENTAJE = "porcentaje_recargo";

    private DatabaseHelper mDbHelper;
    private SQLiteDatabase mDb;

    private static final String DATABASE_TABLE = "habitacion";

    private final Context mCtx;

    /**
     * Constructor - takes the context to allow the database to be
     * opened/created
     *
     * @param ctx the Context within which to work
     */
    public HabitacionDbAdapter(Context ctx) {
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
    public HabitacionDbAdapter open() throws SQLException {
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
    public long createHabitacion(String id, String descripcion, Integer MAX_OCUPANTES,
                                 Double precioNoche, Double porcentaje_recargo) {
        if (id == null || id.isEmpty() || !id.matches("\\d+") || descripcion == null || descripcion.isEmpty() || MAX_OCUPANTES == null
                || precioNoche == null || porcentaje_recargo == null || precioNoche < 0 || MAX_OCUPANTES <= 0 || MAX_OCUPANTES > 10 ) {
            return -1;
        }
        ContentValues initialValues = new ContentValues();
        initialValues.put(HAB_ID, id);
        initialValues.put(HAB_DESCRIPCION, descripcion);
        initialValues.put(HAB_MAX_OCUPANTES, MAX_OCUPANTES);
        initialValues.put(HAB_PRECIO_NOCHE, precioNoche);
        initialValues.put(HAB_PORCENTAJE, porcentaje_recargo);

        return mDb.insert(DATABASE_TABLE, null, initialValues);
    }

    public long deleteAllHabitaciones(){

        return mDb.delete(DATABASE_TABLE, null, null);
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
        return mDb.delete(DATABASE_TABLE, HAB_ID + "=" + rowId, null) > 0;
    }

    /**
     * Return a Cursor over the list of all notes in the database
     * @param orderBy criterio segun el que se ordenan las habitaciones
     * @return Cursor over all notes
     */
    public Cursor fetchAllHabitaciones(String orderBy) {
        String orderByClause;
        switch (orderBy) {
            case HAB_MAX_OCUPANTES:
                orderByClause = HAB_MAX_OCUPANTES + " DESC";
                break;
            case HAB_PRECIO_NOCHE:
                orderByClause = HAB_PRECIO_NOCHE + " DESC";
                break;
            default:
                orderByClause = HAB_ID + " ASC";
                break;
        }

        return mDb.query(DATABASE_TABLE, new String[]{
                HAB_ID, HAB_DESCRIPCION, HAB_MAX_OCUPANTES, HAB_PRECIO_NOCHE,
                HAB_PORCENTAJE}, null, null, null, null, orderByClause);
    }

    public Cursor fetchAllHabitaciones() {
        return mDb.query(DATABASE_TABLE, new String[] {HAB_ID, HAB_DESCRIPCION, HAB_MAX_OCUPANTES, HAB_PRECIO_NOCHE, HAB_PORCENTAJE},
                null, null, null, null, null);
    }


    public Cursor obtenerPrecioHabitacion(long idHabitacion) {
        return mDb.query(DATABASE_TABLE, new String[] {HAB_PRECIO_NOCHE},
                HAB_ID + " = ?", new String[] {String.valueOf(idHabitacion)}, null, null, null);
    }


    /**
     * Return a Cursor positioned at the note that matches the given rowId
     *
     * @param rowId id of note to retrieve
     * @return Cursor positioned to matching note, if found
     * @throws SQLException if note could not be found/retrieved
     */
    public Cursor fetchHabitacion(long rowId) throws SQLException {

        Cursor mCursor =

             mDb.query(DATABASE_TABLE, new String[]{
                HAB_ID, HAB_DESCRIPCION, HAB_MAX_OCUPANTES, HAB_PRECIO_NOCHE,
                HAB_PORCENTAJE}, HAB_ID + "=" + rowId, null, null,
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
    public boolean updateHabitacion(long rowId, String descripcion, Integer MAX_OCUPANTES, Double precioNoche, Double porcentaje_recargo) {
        if (rowId < 1 || descripcion == null || descripcion.isEmpty() || MAX_OCUPANTES == null
                || precioNoche == null || porcentaje_recargo == null || precioNoche < 0 || MAX_OCUPANTES <= 0 || MAX_OCUPANTES > 10 ) {
            return false;
        }

        ContentValues initialValues = new ContentValues();
        initialValues.put(HAB_DESCRIPCION, descripcion);
        initialValues.put(HAB_MAX_OCUPANTES, MAX_OCUPANTES);
        initialValues.put(HAB_PRECIO_NOCHE, precioNoche);
        initialValues.put(HAB_PORCENTAJE, porcentaje_recargo);

        return mDb.update(DATABASE_TABLE, initialValues, HAB_ID + "=" + rowId, null) > 0;
    }
}