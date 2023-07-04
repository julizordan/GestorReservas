package es.unizar.eina.gestorReservas;

import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import es.unizar.eina.gestorReservas.R;

public class HabitacionEdit extends AppCompatActivity {
    public EditText idHabitacion;
    public EditText descripcion;
    public EditText max_ocupantes;
    public EditText precioNoche;
    public EditText porcentaje;

    private Long mRowId;
    private HabitacionDbAdapter mDbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mDbHelper = new HabitacionDbAdapter(this);
        mDbHelper.open();
        setContentView(R.layout.habitacion_edit);
        setTitle(R.string.habitacion_edit);

        idHabitacion = (EditText) findViewById(R.id.id_hab);
        descripcion = (EditText) findViewById(R.id.descripcion_hab);
        max_ocupantes = (EditText) findViewById(R.id.max_ocupantes);
        precioNoche = (EditText) findViewById(R.id.precio_noche);
        porcentaje = (EditText) findViewById(R.id.porcentaje_recargo);

        Button confirmButton = (Button) findViewById(R.id.confirm);

        mRowId = (savedInstanceState == null) ? null :
                (Long) savedInstanceState.getSerializable(HabitacionDbAdapter.HAB_ID);
        if (mRowId == null) {
            Bundle extras = getIntent().getExtras();
            mRowId = (extras != null) ? extras.getLong(HabitacionDbAdapter.HAB_ID) : null;
        }

        confirmButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                Bundle bundle = new Bundle();
                setResult(RESULT_OK);
                finish();
            }

        });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        saveState();
        outState.putSerializable(HabitacionDbAdapter.HAB_ID, mRowId);
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
    }

    private void saveState() {
        String descripcion = this.descripcion.getText().toString();
        String newID = idHabitacion.getText().toString();
        int max_ocupantes;
        double precioNoche;
        double porcentaje;

        try {
            max_ocupantes = Integer.parseInt(this.max_ocupantes.getText().toString());
            precioNoche = Double.parseDouble(this.precioNoche.getText().toString());
            porcentaje = Double.parseDouble(this.porcentaje.getText().toString());
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        if (mRowId == null) {
            long id = mDbHelper.createHabitacion(newID, descripcion, max_ocupantes, precioNoche, porcentaje);
            if (id > 0) {
                mRowId = id;
                Toast.makeText(this, "Habitación guardada con éxito. ID: " + id, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Error al guardar la habitación", Toast.LENGTH_SHORT).show();
            }
        } else if (!newID.equals(String.valueOf(mRowId))) {
            Toast.makeText(this, "No se puede modificar el ID de una habitación existente", Toast.LENGTH_SHORT).show();
            return;
        } else {
            boolean updated = mDbHelper.updateHabitacion(mRowId, descripcion, max_ocupantes, precioNoche, porcentaje);
            if (updated) {
                Toast.makeText(this, "Habitación actualizada con éxito", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Error al guardar la habitación", Toast.LENGTH_SHORT).show();
            }
        }
    }


    private void populateFields() {
        if (mRowId != null) {
            Cursor note = mDbHelper.fetchHabitacion(mRowId);
            startManagingCursor(note);
            idHabitacion.setText(String.valueOf(note.getLong(note.getColumnIndexOrThrow(HabitacionDbAdapter.HAB_ID))));
            descripcion.setText(note.getString(note.getColumnIndexOrThrow(HabitacionDbAdapter.HAB_DESCRIPCION)));
            max_ocupantes.setText(note.getString(note.getColumnIndexOrThrow(HabitacionDbAdapter.HAB_MAX_OCUPANTES)));
            precioNoche.setText(note.getString(note.getColumnIndexOrThrow(HabitacionDbAdapter.HAB_PRECIO_NOCHE)));
            porcentaje.setText(note.getString(note.getColumnIndexOrThrow(HabitacionDbAdapter.HAB_PORCENTAJE)));
        }
    }
}
