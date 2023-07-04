package es.unizar.eina.gestorReservas;

import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

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
    TextView infoReservaTextView;
    private String infoReservaActual;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mDbHelper = new ReservasDbAdapter(this);
        mDbHelper.open();
        mDbHelperHabitacion = new HabitacionDbAdapter(this);
        mDbHelperHabitacion.open();
        setContentView(R.layout.reserva_edit);
        infoReservaTextView = findViewById(R.id.infoReservaTextView);
        setTitle(R.string.reserva_edit);
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
        listaHabitaciones = findViewById(R.id.lista_habitaciones);
        nombreCliente = findViewById(R.id.nombre_cliente);
        numeroCliente = findViewById(R.id.numero_cliente);
        fechaEntrada = findViewById(R.id.fecha_entrada);
        fechaSalida = findViewById(R.id.fecha_salida);

        idReserva = findViewById(R.id.id_reserva);
        idReserva.setEnabled(false);

        Button confirmButton = findViewById(R.id.confirm);

        mRowId = (savedInstanceState == null) ? null : (Long) savedInstanceState.getSerializable(ReservasDbAdapter.RESERVA_ID);
        if (mRowId == null) {
            Bundle extras = getIntent().getExtras();
            mRowId = (extras != null) ? extras.getLong(ReservasDbAdapter.RESERVA_ID) : null;
        }

        confirmButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
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

        boolean isReservaCreated = false; // Variable para controlar si la reserva se ha creado

        long idReserva = 0;
        double precioTotal = 0;
        long noches = 0;

        if (mRowId == null) {
            // Crear la reserva y obtener su ID
            idReserva = mDbHelper.createReserva(nombreCliente, numeroCliente, fechaEntrada, fechaSalida);
            Log.d("RESERVAS", "ID reserva" + idReserva);
            if (idReserva > 0) {
                mRowId = idReserva;
                isReservaCreated = true; // La reserva se ha creado
                Toast.makeText(this, "Reserva guardada con éxito", Toast.LENGTH_SHORT).show();
                List<Long> habitacionesSeleccionadas = getHabitacionesSeleccionadas();

                for (Long idHabitacion : habitacionesSeleccionadas) {
                    noches = mDbHelper.contarNoches(fechaEntrada, fechaSalida);
                    Cursor cursor = mDbHelperHabitacion.obtenerPrecioHabitacion(idHabitacion);
                    if (cursor.moveToFirst()) {
                        double precioNoche = cursor.getDouble(cursor.getColumnIndex(HabitacionDbAdapter.HAB_PRECIO_NOCHE));
                        precioTotal = precioNoche * noches;
                        mDbHelper.insertarInfoReserva(idReserva, idHabitacion, (int) noches, precioTotal);
                        Log.d("RESERVAS", "insertar Info reserva");
                    }
                    cursor.close();
                }
            } else {
                Toast.makeText(this, "Error al guardar la reserva", Toast.LENGTH_SHORT).show();
            }
        }

        if (!isReservaCreated) { // Si la reserva no se ha creado, ejecutar la lógica de actualización
            mDbHelper.deleteInfoReserva(mRowId);
            Log.d("RESERVAS", "entro en el else");
            mDbHelper.updateReserva(mRowId, nombreCliente, numeroCliente, fechaEntrada, fechaSalida);
            Toast.makeText(this, "Reserva actualizada con éxito", Toast.LENGTH_SHORT).show();

            List<Long> habitacionesSeleccionadas = getHabitacionesSeleccionadas();
            StringBuilder infoReservaTextoBuilder = new StringBuilder();

            for (Long idHabitacion : habitacionesSeleccionadas) {
                noches = mDbHelper.contarNoches(fechaEntrada, fechaSalida);
                Cursor cursor = mDbHelperHabitacion.obtenerPrecioHabitacion(idHabitacion);
                if (cursor.moveToFirst()) {
                    double precioNoche = cursor.getDouble(cursor.getColumnIndex(HabitacionDbAdapter.HAB_PRECIO_NOCHE));
                    Log.d("RESERVAS", "precioNoche" + precioNoche);
                    precioTotal = precioNoche * noches;
                    Log.d("RESERVAS", "precioTotal" + precioTotal);
                    mDbHelper.updateInfoReserva(mRowId, idHabitacion, (int) noches, precioTotal);

                    // Agregar información de la reserva actual al texto
                    String infoReservaActual = "ID Habitación: " + idHabitacion + "\n" + "ID Reserva: " + mRowId + "\n" + "Precio Compra: " + precioTotal + "\n" + "Numero de noches: " + noches + "\n" + "-------------------------\n";
                    infoReservaTextoBuilder.append(infoReservaActual);
                }
                cursor.close();
            }

            infoReservaActual = infoReservaTextoBuilder.toString();
            infoReservaTextView.setText(infoReservaActual);
        }
    }



    private void populateFields() {
        Log.d("RESERVAS", "populateFields");
        if (mRowId != null) {
            Cursor note = mDbHelper.fetchReserva(mRowId);
            startManagingCursor(note);
            idReserva.setText(mRowId.toString());
            nombreCliente.setText(note.getString(note.getColumnIndexOrThrow(ReservasDbAdapter.RESERVA_NOMBREC)));
            numeroCliente.setText(note.getString(note.getColumnIndexOrThrow(ReservasDbAdapter.RESERVA_TELEFONO_CLIENTE)));
            fechaEntrada.setText(note.getString(note.getColumnIndexOrThrow(ReservasDbAdapter.RESERVA_FECHAE)));
            fechaSalida.setText(note.getString(note.getColumnIndexOrThrow(ReservasDbAdapter.RESERVA_FECHAS)));

            // Obtener las habitaciones asociadas a la reserva actual
            Cursor habitacionesAsociadas = mDbHelper.obtenerHabitacionesDeReserva(mRowId);
            StringBuilder habitacionesTextoBuilder = new StringBuilder();

            while (habitacionesAsociadas.moveToNext()) {
                // Obtener la información de cada habitación asociada
                long idHabitacion = habitacionesAsociadas.getLong(habitacionesAsociadas.getColumnIndexOrThrow(ReservasDbAdapter.RELACION_ID_HAB));
                double precioTotal = habitacionesAsociadas.getDouble(habitacionesAsociadas.getColumnIndexOrThrow(ReservasDbAdapter.RELACION_PRECIO_TOTAL));
                int noches = habitacionesAsociadas.getInt(habitacionesAsociadas.getColumnIndexOrThrow(ReservasDbAdapter.RELACION_NUM_NOCHES));

                // Agregar información de cada habitación asociada al StringBuilder
                String infoHabitacion = "ID Habitación: " + idHabitacion + "\n" + "ID Reserva: " + mRowId + "\n" + "Precio Compra: " + precioTotal + "\n" + "Numero de noches: " + noches + "\n" + "-------------------------\n";
                habitacionesTextoBuilder.append(infoHabitacion);
            }
            String habitacionesTexto = habitacionesTextoBuilder.toString();
            infoReservaTextView.setText(habitacionesTexto);
        }
    }


    private void populateHabitacionList() {
        Cursor cursorHabitacion = mDbHelperHabitacion.fetchAllHabitaciones();
        String[] from = new String[]{HabitacionDbAdapter.HAB_ID, HabitacionDbAdapter.HAB_PRECIO_NOCHE, HabitacionDbAdapter.HAB_MAX_OCUPANTES};
        int[] to = new int[]{R.id.text1, R.id.text2, R.id.text3,};

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
                    } else if (view.getId() == R.id.text3) {
                        view.setVisibility(View.GONE);
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


    private List<Long> getHabitacionesSeleccionadas() {
        List<Long> habitacionesSeleccionadas = new ArrayList<>();
        SparseBooleanArray checkedPositions = listaHabitaciones.getCheckedItemPositions();
        for (int i = 0; i < checkedPositions.size(); i++) {
            int position = checkedPositions.keyAt(i);
            if (checkedPositions.valueAt(i)) {
                Cursor cursor = (Cursor) listaHabitaciones.getItemAtPosition(position);
                if (cursor != null) {
                    long idHabitacion = cursor.getLong(cursor.getColumnIndex(HabitacionDbAdapter.HAB_ID));
                    habitacionesSeleccionadas.add(idHabitacion);
                }
            }
        }
        return habitacionesSeleccionadas;
    }
}
