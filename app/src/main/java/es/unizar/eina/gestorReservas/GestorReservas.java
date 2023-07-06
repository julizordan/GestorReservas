package es.unizar.eina.gestorReservas;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import androidx.appcompat.app.AppCompatActivity;

public class GestorReservas extends AppCompatActivity {

    private static final int ACTIVITY_CREATE_RESERVA = 0;
    private static final int ACTIVITY_EDIT_RESERVA = 1;
    private static final int ACTIVITY_HABITACION = 3;
    private static final int ACTIVITY_SEND_RESERVA = 4;

    private static final int INSERT_ID = Menu.FIRST;
    private static final int DELETE_ID = Menu.FIRST + 1;
    private static final int EDIT_ID = Menu.FIRST + 2;
    public static final int SEND_ID = Menu.FIRST + 3;
    public static final int ACTIVITY_VER_HABITACION = Menu.FIRST + 4;
    public static final int NOMBRE_CLIENTE = Menu.FIRST + 5;
    public static final int TELEFONO = Menu.FIRST + 6;
    public static final int FECHA_ENTRADA = Menu.FIRST + 7;
    private static final int TEST_SOBRECARGA = Menu.FIRST + 8;
    private static final int TEST_CAJA_NEGRA = Menu.FIRST + 9;
    private static final int TEST_NIVEL = Menu.FIRST + 10;
    private static final int AJUSTES_PREDETERMINADOS = Menu.FIRST + 11;

    private ReservasDbAdapter mDbHelper;
    private Cursor mNotesCursor;
    private ListView mList;
    private String order;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gestor_reservas);

        mDbHelper = new ReservasDbAdapter(this);
        mDbHelper.open();
        mList = findViewById(R.id.list);
        setTitle("Gestor Reservas");
        order = ReservasDbAdapter.RESERVA_NOMBREC;
        fillData();
        registerForContextMenu(mList);

        // Establecer el modo de selección de la lista como persistente
        mList.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
    }

    private void fillData() {
        // Get all of the notes from the database and create the item list
        mNotesCursor = mDbHelper.fetchAllReservas(order);
        startManagingCursor(mNotesCursor);

        // Create an array to specify the fields we want to display in the list (only TITLE)
        String[] from = new String[]{
                ReservasDbAdapter.RESERVA_NOMBREC,
                ReservasDbAdapter.RESERVA_TELEFONO_CLIENTE,
                ReservasDbAdapter.RESERVA_FECHAE,
                ReservasDbAdapter.RESERVA_FECHAS
        };

        // and an array of the fields we want to bind those fields to (in this case just text1)
        int[] to = new int[]{R.id.text1, R.id.text2, R.id.text3, R.id.text4};

        // Now create an array adapter and set it to display using our row
        SimpleCursorAdapter notes = new SimpleCursorAdapter(this, R.layout.reserva_row, mNotesCursor, from, to);
        notes.setViewBinder(new SimpleCursorAdapter.ViewBinder() {
            @Override
            public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
                // Verificar si la vista es el elemento deseado para cambiar el color
                if (view.getId() == R.id.text1) { // Cambia el ID por el correspondiente al elemento de la lista que deseas cambiar de color
                    // Obtener el estado activado del elemento actual
                    boolean isActivated = mList.isItemChecked(cursor.getPosition());
                    // Cambiar el color de fondo según el estado activado
                    if (isActivated) {
                        view.setBackgroundColor(getResources().getColor(R.color.colorAccent));
                    } else {
                        view.setBackgroundColor(getResources().getColor(android.R.color.transparent));
                    }
                    return true;
                }
                return false;
            }
        });
        mList.setAdapter(notes);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        boolean result = super.onCreateOptionsMenu(menu);
        menu.add(Menu.NONE, ACTIVITY_VER_HABITACION, Menu.NONE, R.string.verHabitacion);
        menu.add(Menu.NONE, INSERT_ID, Menu.NONE, R.string.reservasInsert);
        menu.add(Menu.NONE, NOMBRE_CLIENTE, Menu.NONE, R.string.ordernombreCliente);
        menu.add(Menu.NONE, TELEFONO, Menu.NONE, R.string.ordertelefonoCLiente);
        menu.add(Menu.NONE, FECHA_ENTRADA, Menu.NONE, R.string.orderfechaECLiente);
        menu.add(Menu.NONE, TEST_SOBRECARGA, Menu.NONE, R.string.testSobrecarga);
        menu.add(Menu.NONE, TEST_CAJA_NEGRA, Menu.NONE, R.string.testCajaNegra);
        menu.add(Menu.NONE, TEST_NIVEL, Menu.NONE, R.string.testPruebaVolumen);
        menu.add(Menu.NONE, AJUSTES_PREDETERMINADOS, Menu.NONE, R.string.ajustesPredeterminados);
        return result;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case INSERT_ID:
                createReserva();
                return true;
            case ACTIVITY_VER_HABITACION:
                irHabitacion();
                return true;
            case NOMBRE_CLIENTE:
                order = ReservasDbAdapter.RESERVA_NOMBREC;
                fillData();
                return true;
            case TELEFONO:
                order = ReservasDbAdapter.RESERVA_TELEFONO_CLIENTE;
                fillData();
                return true;
            case FECHA_ENTRADA:
                order = ReservasDbAdapter.RESERVA_FECHAE;
                fillData();
                return true;
            case TEST_SOBRECARGA:
                Test.testSobrecarga(this);
                return true;
            case TEST_CAJA_NEGRA:
                Test.testCajaNegraHabitaciones(this);
                return true;
            case TEST_NIVEL:
                Test.pruebaVolumen(this);
                return true;
            case AJUSTES_PREDETERMINADOS:
                Test.ajustesPredeterminados(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.add(Menu.NONE, DELETE_ID, Menu.NONE, R.string.deleteRes);
        menu.add(Menu.NONE, EDIT_ID, Menu.NONE, R.string.editRes);
        menu.add(Menu.NONE, SEND_ID, Menu.NONE, R.string.sendRes);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case DELETE_ID:
                AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
                mDbHelper.deleteReserva(info.id);
                fillData();
                return true;
            case EDIT_ID:
                info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
                editReserva(info.position, info.id);
                return true;
            case SEND_ID:
                info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
                sendReserva(info.position, info.id);
                return true;
        }
        return super.onContextItemSelected(item);
    }

    private void createReserva() {
        Intent i = new Intent(this, ReservasEdit.class);
        startActivityForResult(i, ACTIVITY_CREATE_RESERVA);
    }

    protected void editReserva(int position, long id) {
        Intent i = new Intent(this, ReservasEdit.class);
        i.putExtra(ReservasDbAdapter.RESERVA_ID, id);
        startActivityForResult(i, ACTIVITY_EDIT_RESERVA);
    }

    private void irHabitacion() {
        Intent i = new Intent(this, GestorHabitacion.class);
        startActivityForResult(i, ACTIVITY_HABITACION);
    }

    private void sendReserva(int position, long id) {
        Intent i = new Intent(this, ReservaSend.class);
        i.putExtra(ReservasDbAdapter.RESERVA_ID, id);
        startActivityForResult(i, ACTIVITY_SEND_RESERVA);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        fillData();
    }
}
