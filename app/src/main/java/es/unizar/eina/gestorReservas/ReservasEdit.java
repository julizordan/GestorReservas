package es.unizar.eina.gestorReservas;

import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class ReservasEdit extends AppCompatActivity {
    private EditText idReserva;
    private EditText nombreCliente;
    private EditText numeroCliente;
    private EditText fechaEntrada;
    private EditText fechaSalida;
    private EditText precio;

    private Long mRowId;
    private HabitacionDbAdapter mDbHelperHabitacion;
    private ReservasDbAdapter mDbHelper;
    private ListView listaHabitaciones;
    private SimpleCursorAdapter habitacionAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mDbHelper = new ReservasDbAdapter(this);
        mDbHelper.open();
        mDbHelperHabitacion = new HabitacionDbAdapter(this);
        mDbHelperHabitacion.open();
        setContentView(R.layout.reserva_edit);
        ListView habitacionesList = findViewById(R.id.lista_habitaciones);
        habitacionesList.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        habitacionesList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Obtener el estado de selección del elemento actual
                boolean isSelected = habitacionesList.isItemChecked(position);
                // Cambiar el color de fondo del elemento según su estado de selección
                view.setBackgroundColor(isSelected ? getResources().getColor(R.color.colorAccent) : Color.TRANSPARENT);
            }
        });

        setTitle(R.string.reserva_edit);

        listaHabitaciones = findViewById(R.id.lista_habitaciones);
        nombreCliente = findViewById(R.id.nombre_cliente);
        numeroCliente = findViewById(R.id.numero_cliente);
        fechaEntrada = findViewById(R.id.fecha_entrada);
        fechaSalida = findViewById(R.id.fecha_salida);

        idReserva = findViewById(R.id.id_reserva);
        idReserva.setEnabled(false);

        precio = findViewById(R.id.precio_total);
        precio.setEnabled(false);

        Button confirmButton = findViewById(R.id.confirm);

        mRowId = (savedInstanceState == null) ? null :
                (Long) savedInstanceState.getSerializable(ReservasDbAdapter.RESERVA_ID);
        if (mRowId == null) {
            Bundle extras = getIntent().getExtras();
            mRowId = (extras != null) ? extras.getLong(ReservasDbAdapter.RESERVA_ID) : null;
        }

        confirmButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                saveState();
                setResult(RESULT_OK);
                finish();
            }
        });

        registerForContextMenu(listaHabitaciones);

        if (mRowId != null) {
            populateFields();
        } else {
            idReserva.setText("Nueva Reserva");
        }
        populateHabitacionList();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        saveState();
        outState.putSerializable(ReservasDbAdapter.RESERVA_ID, mRowId);
    }

    @Override
    protected void onPause() {
        super.onPause();
        saveState();
    }

    @Override
    protected void onResume() {
        super.onResume();
        populateFields();
        populateHabitacionList();
    }

    private void saveState() {
        String numeroCliente = this.numeroCliente.getText().toString();
        String nombreCliente = this.nombreCliente.getText().toString();
        String fechaEntrada = this.fechaEntrada.getText().toString();
        String fechaSalida = this.fechaSalida.getText().toString();

        if (numeroCliente.isEmpty() || nombreCliente.isEmpty() || fechaEntrada.isEmpty() || fechaSalida.isEmpty()) {
            // Validar que se hayan ingresado todos los campos requeridos
            Toast.makeText(this, "Por favor, complete todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        if (mRowId == null) { // Se estaba creando una reserva
            long id = mDbHelper.createReserva(nombreCliente, numeroCliente, fechaEntrada, fechaSalida);
            if (id > 0) {
                mRowId = id;
                Toast.makeText(this, "Reserva guardada con éxito", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Error al guardar la reserva", Toast.LENGTH_SHORT).show();
            }
        } else {
            mDbHelper.updateReserva(mRowId, nombreCliente, numeroCliente, fechaEntrada, fechaSalida);
            Toast.makeText(this, "Reserva actualizada con éxito", Toast.LENGTH_SHORT).show();
        }
    }


    private void populateFields() {
        if (mRowId != null) {
            Cursor note = mDbHelper.fetchReserva(mRowId);
            startManagingCursor(note);
            idReserva.setText(mRowId.toString());
            nombreCliente.setText(note.getString(note.getColumnIndexOrThrow(ReservasDbAdapter.RESERVA_NOMBREC)));
            numeroCliente.setText(note.getString(note.getColumnIndexOrThrow(ReservasDbAdapter.RESERVA_TELEFONO_CLIENTE)));
            fechaEntrada.setText(note.getString(note.getColumnIndexOrThrow(ReservasDbAdapter.RESERVA_FECHAE)));
            fechaSalida.setText(note.getString(note.getColumnIndexOrThrow(ReservasDbAdapter.RESERVA_FECHAS)));
        }
    }

    private void populateHabitacionList() {
        Cursor cursorHabitacion = mDbHelperHabitacion.fetchAllHabitaciones();
        String[] from = new String[]{
                HabitacionDbAdapter.HAB_ID,
                HabitacionDbAdapter.HAB_PRECIO_NOCHE,
                HabitacionDbAdapter.HAB_MAX_OCUPANTES
        };
        int[] to = new int[]{
                R.id.text1,
                R.id.text2,
                R.id.text3,
        };

        if (habitacionAdapter == null) {
            habitacionAdapter = new SimpleCursorAdapter(this, R.layout.habitacion_row, cursorHabitacion, from, to, 0);
            habitacionAdapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {
                @Override
                public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
                    if (view.getId() == R.id.text1) {
                        String idHab = cursor.getString(columnIndex);
                        String precioNoche = cursor.getString(cursor.getColumnIndex(HabitacionDbAdapter.HAB_PRECIO_NOCHE));
                        String maxOcupantes = cursor.getString(cursor.getColumnIndex(HabitacionDbAdapter.HAB_MAX_OCUPANTES));

                        String formattedText = "ID: " + idHab + ", Precio noche: $" + precioNoche + ", Max ocupantes: " + maxOcupantes;
                        ((TextView) view).setText(formattedText);
                        return true;
                    }
                    return false;
                }
            });
            listaHabitaciones.setAdapter(habitacionAdapter);
        } else {
            habitacionAdapter.changeCursor(cursorHabitacion);
        }

        listaHabitaciones.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Cursor cursor = (Cursor) parent.getItemAtPosition(position);
                if (cursor != null) {
                    String idHab = cursor.getString(cursor.getColumnIndex(HabitacionDbAdapter.HAB_ID));
                    Log.d("ReservasEdit", "Habitación seleccionada: " + idHab);
                }
            }
        });
    }

}
