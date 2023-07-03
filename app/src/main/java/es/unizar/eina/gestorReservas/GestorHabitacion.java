package es.unizar.eina.gestorReservas;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

public class GestorHabitacion extends AppCompatActivity {

    private static final int ACTIVITY_CREATE=0;
    private static final int ACTIVITY_EDIT=1;
    private static final int ACTIVITY_RESERVAS=2;

    private static final int INSERT_ID = Menu.FIRST;
    private static final int DELETE_ID = Menu.FIRST + 1;
    private static final int EDIT_ID = Menu.FIRST + 2;
    public static final int HAB_ID = Menu.FIRST + 3;
    public static final int VER_RESERVAS = Menu.FIRST + 4;
    public static final int HAB_MAX_OCUPANTES = Menu.FIRST + 5;
    public static final int HAB_PRECIO_NOCHE = Menu.FIRST + 6;
    public static final int HAB_PORCENTAJE = Menu.FIRST + 7;

    private HabitacionDbAdapter mDbHelper;
    private Cursor mNotesCursor;
    private ListView mList;
    private String order;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.gestor_habitacion);

        mDbHelper = new HabitacionDbAdapter(this);
        mDbHelper.open();
        mList = (ListView) findViewById(R.id.list);
        setTitle("Gestor habitacion");
        order = HabitacionDbAdapter.HAB_ID;
        fillData();
        registerForContextMenu(mList);
    }

    private void fillData() {
        // Get all of the notes from the database and create the item list
        mNotesCursor = mDbHelper.fetchAllHabitaciones(order);
        startManagingCursor(mNotesCursor);

        // Create an array to specify the fields we want to display in the list (only TITLE)
        String[] from = new String[]{HabitacionDbAdapter.HAB_ID};

        // and an array of the fields we want to bind those fields to (in this case just text1)
        int[] to = new int[]{R.id.text1};

        // Now create a custom adapter and set it to display using our row
        SimpleCursorAdapter habitacionesAdapter = new SimpleCursorAdapter(
                this, R.layout.habitacion_row, mNotesCursor, from, to);
        habitacionesAdapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {
            @Override
            public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
                if (view.getId() == R.id.text1) {
                    TextView textView = (TextView) view;
                    String habitacionId = "Habitación: " + cursor.getString(columnIndex);
                    textView.setText(habitacionId);
                    textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 21); // Tamaño de texto personalizado (21sp)
                    return true;
                }
                return false;
            }
        });
        mList.setAdapter(habitacionesAdapter);
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        boolean result = super.onCreateOptionsMenu(menu);
        menu.add(Menu.NONE, VER_RESERVAS, Menu.NONE, R.string.verReservas);
        menu.add(Menu.NONE, INSERT_ID, Menu.NONE, R.string.habInsert);
        menu.add(Menu.NONE, HAB_MAX_OCUPANTES, Menu.NONE, R.string.orderOcupantes);
        menu.add(Menu.NONE, HAB_PRECIO_NOCHE, Menu.NONE, R.string.orderPrecio);
        menu.add(Menu.NONE, HAB_ID, Menu.NONE, R.string.orderID);
        return result;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case INSERT_ID:
                createHabitacion();
                return true;
            case HAB_MAX_OCUPANTES:
                    order = HabitacionDbAdapter.HAB_MAX_OCUPANTES;
                    fillData();
                    return true;
            case HAB_PRECIO_NOCHE:
                order = HabitacionDbAdapter.HAB_PRECIO_NOCHE;
                fillData();
                return true;
            case HAB_ID:
                order = HabitacionDbAdapter.HAB_ID;
                fillData();
                return true;
            case VER_RESERVAS:
                verReservas();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.add(Menu.NONE, DELETE_ID, Menu.NONE, R.string.deleteHab);
        menu.add(Menu.NONE, EDIT_ID, Menu.NONE, R.string.editHab);
    }

    /*Esto es cuando se le da y se borra, también seleccionando se puede editar*/
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case DELETE_ID:
                AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
                mDbHelper.deleteHabitacion(info.id);
                return true;
            case EDIT_ID:
                info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
                editHabitacion(info.id);
                return true;
        }
        return super.onContextItemSelected(item);
    }

    private void createHabitacion() {
        Intent i = new Intent(this, HabitacionEdit.class);
        startActivityForResult(i, ACTIVITY_CREATE);
    }

    private void editHabitacion(long id) {
        Intent i = new Intent(this, HabitacionEdit.class);
        i.putExtra(HabitacionDbAdapter.HAB_ID,id);
        startActivityForResult(i, ACTIVITY_EDIT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        fillData();
    }

    private void verReservas(){
        Intent i = new Intent(this, GestorReservas.class);
        startActivityForResult(i, ACTIVITY_RESERVAS);
    }
}
