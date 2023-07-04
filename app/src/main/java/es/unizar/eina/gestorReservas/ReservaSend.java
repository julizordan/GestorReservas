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
        public String descripcion;
        public Double precioNoche;
        public Double porcentaje_recargo;
        public Integer MAX_OCUPANTES;

        public InfoHabitacion(String descripcion, Double precioNoche, Double porcentaje_recargo, Integer MAX_OCUPANTES) {
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
        Cursor reserva = mDbHelper.fetchReserva(mRowId);
        startManagingCursor(reserva);
        String nombre_cliente = reserva.getString(reserva.getColumnIndexOrThrow(ReservasDbAdapter.RESERVA_NOMBREC));
        telefonoCliente = reserva.getString(reserva.getColumnIndexOrThrow(ReservasDbAdapter.RESERVA_TELEFONO_CLIENTE));
        String fecha_entrada = reserva.getString(reserva.getColumnIndexOrThrow(ReservasDbAdapter.RESERVA_FECHAE));
        String fecha_salida = reserva.getString(reserva.getColumnIndexOrThrow(ReservasDbAdapter.RESERVA_FECHAS));



        // Productos
        ArrayList<InfoHabitacion> habitacion = new ArrayList<InfoHabitacion>();
        double pesoTotal = 0.0;
        double precioTotal = 0.0;

        Cursor hab = mDbHelper.obtenerHabitacionesDeReserva(mRowId);
        while (!hab.isAfterLast()){
            String nombre = hab.getString(hab.getColumnIndex(PedidoDbAdapter.KEY_NOMBRE_PRODUCTO));
            Double peso = hab.getDouble(hab.getColumnIndex(PedidoDbAdapter.KEY_PESO_PRODUCTO));
            Double precio = hab.getDouble(hab.getColumnIndex(PedidoDbAdapter.KEY_PRECIO_PRODUCTO));
            Integer cantidad = hab.getInt(hab.getColumnIndex(PedidoDbAdapter.KEY_CANTIDAD));

            habitacion.add(new InfoProd(nombre, peso, precio, cantidad));

            pesoTotal += peso * cantidad;
            precioTotal += precio * cantidad;

            hab.moveToNext();
        }


        mensaje = "Su reserva ya esta confirmada" +
                "\nCliente: " + nombre_cliente +
                "\nTelefono de contacto: " + telefonoCliente +
                "\nFecha de entrega estimada: " + fecha_entrada +
                "\nPeso total: " + Double.toString(pesoTotal) + "kg" +
                "\nPrecio total: " +  Double.toString(precioTotal) + "€" +
                "\nLista de productos:" +
                "\n\tNombre\t-\tPeso(kg)\t-\tPrecio(€)\t-\tCantidad" ;
        for ( InfoHabitacion a: habitacion) {
            mensaje += "\n\t"+ a.nombre +"\t-\t"+ a.peso +"\t-\t"+ a.precio +"\t-\t"+ a.cantidad;
        }

    }
    private void send(String method){
        SendAbstraction sender = new SendAbstractionImpl(this, method);
        sender.send("", mensaje);

    }

    int getInt(String str){
        if(str.equals("")){
            return 0;
        }
        else {
            return Integer.parseInt(str);
        }
    }

}
}
