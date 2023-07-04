package es.unizar.eina.gestorReservas;

import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

import es.unizar.eina.gestorReservas.send.SendAbstraction;
import es.unizar.eina.gestorReservas.send.SendAbstractionImpl;

public class ReservaSend extends AppCompatActivity {
    private TextView mTelefono;
    private TextView mBody;

    private Long mRowId;

    private ReservasDbAdapter mDbHelper;

    private String telefonoCliente;
    private String mensaje;

    private class InfoHabitacion {
        public String id_hab;
        public String descripcion;
        public Double precioNoche;
        public Double porcentaje_recargo;
        public Integer MAX_OCUPANTES;

        public InfoHabitacion(String id_hab, String descripcion, Double precioNoche, Double porcentaje_recargo, Integer MAX_OCUPANTES) {
            this.id_hab = id_hab;
            this.descripcion = descripcion;
            this.precioNoche = precioNoche;
            this.porcentaje_recargo = porcentaje_recargo;
            this.MAX_OCUPANTES = MAX_OCUPANTES;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mDbHelper = new ReservasDbAdapter(this);
        mDbHelper.open();

        setContentView(R.layout.reserva_send);
        setTitle(R.string.sendRes);

        mTelefono = (TextView) findViewById(R.id.numero_cliente);
        mBody = (TextView) findViewById(R.id.message);

        Button confirmButton = (Button) findViewById(R.id.confirm);

        mRowId = (savedInstanceState == null) ? null : (Long) savedInstanceState.getSerializable(HabitacionDbAdapter.HAB_ID);

        if (mRowId == null){
            Bundle extras = getIntent().getExtras();
            mRowId = (extras != null) ? extras.getLong(ReservasDbAdapter.RESERVA_ID) : null;

        }

        confirmButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                send("whatsapp");
                setResult(RESULT_OK);
                finish();
            }

        });
    }

    protected void onSaveInstanceState(Bundle outState){
        super.onSaveInstanceState(outState);
        outState.putSerializable(HabitacionDbAdapter.HAB_ID, mRowId);
    }

    protected void onPause(){
        super.onPause();
    }

    protected void onResume(){
        super.onResume();
        populateFields();
    }


    private void populateFields(){
        setUpMessage();
        Log.d("", "telefonoCliente = " + telefonoCliente);
        Log.d("", "mensaje = " + mensaje);

        mTelefono.setText(telefonoCliente);
        mBody.setText(mensaje);
    }

    private void setUpMessage(){
        Integer noches = 0;
        Cursor reserva = mDbHelper.fetchReserva(mRowId);
        startManagingCursor(reserva);
        long reserva_id = reserva.getLong(reserva.getColumnIndexOrThrow(ReservasDbAdapter.RESERVA_ID));
        String nombre_cliente = reserva.getString(reserva.getColumnIndexOrThrow(ReservasDbAdapter.RESERVA_NOMBREC));
        telefonoCliente = reserva.getString(reserva.getColumnIndexOrThrow(ReservasDbAdapter.RESERVA_TELEFONO_CLIENTE));
        String fecha_entrada = reserva.getString(reserva.getColumnIndexOrThrow(ReservasDbAdapter.RESERVA_FECHAE));
        String fecha_salida = reserva.getString(reserva.getColumnIndexOrThrow(ReservasDbAdapter.RESERVA_FECHAS));

        // Reservas
        ArrayList<InfoHabitacion> habitacion = new ArrayList<>();
        double precioTotal = 0.0;

        Cursor hab = mDbHelper.obtenerHabitacionesDeReserva(reserva_id);
        while (hab.moveToNext()){
            String id = hab.getString(hab.getColumnIndex(ReservasDbAdapter.RELACION_ID_HAB));
            Double precioReserva = hab.getDouble(hab.getColumnIndex(ReservasDbAdapter.RELACION_PRECIO_TOTAL));
            noches = hab.getInt(hab.getColumnIndex(ReservasDbAdapter.RELACION_NUM_NOCHES));

            Integer ocupantes = hab.getInt(hab.getColumnIndex(HabitacionDbAdapter.HAB_MAX_OCUPANTES));
            Double porcentaje_recargo = hab.getDouble(hab.getColumnIndex(HabitacionDbAdapter.HAB_PORCENTAJE));
            Double precio = hab.getDouble(hab.getColumnIndex(HabitacionDbAdapter.HAB_PRECIO_NOCHE));
            String descripcion = hab.getString(hab.getColumnIndex(HabitacionDbAdapter.HAB_DESCRIPCION));

            habitacion.add(new InfoHabitacion(id, descripcion, precio, porcentaje_recargo, ocupantes));

            precioTotal += precioReserva;
        }

        StringBuilder mensaje = new StringBuilder();
        mensaje.append("Su reserva ya está confirmada\n");
        mensaje.append("Cliente: ").append(nombre_cliente).append("\n");
        mensaje.append("Teléfono de contacto: ").append(telefonoCliente).append("\n");
        mensaje.append("Fecha de entrada: ").append(fecha_entrada).append("\n");
        mensaje.append("Fecha de salida: ").append(fecha_salida).append("\n");
        mensaje.append("Numero de noches: ").append(noches).append("\n");
        mensaje.append("Precio total: ").append(String.format("%.2f", precioTotal)).append("€\n");
        mensaje.append("Lista de habitaciones:\n");
        for (InfoHabitacion a : habitacion) {
            mensaje.append("\t").append("ID: "+ a.id_hab).append("\t-\t").append("Precio noche: "+ a.precioNoche).append("\t-\t").append("Recargo: "+ a.porcentaje_recargo).append("\t-\n").append("Max Ocupantes: "+a.MAX_OCUPANTES).append("\t-\n").append("Descripcion: "+a.descripcion).append("\n\n");
        }
        this.mensaje = mensaje.toString();
    }
    private void send(String method){
        SendAbstraction sender = new SendAbstractionImpl(this, method);
        sender.send("", mensaje);

    }

}
