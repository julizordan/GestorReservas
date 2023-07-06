package es.unizar.eina.gestorReservas;

import android.content.Context;

import java.util.Random;

public class Test {

    public static void ajustesPredeterminados(Context ctx) {
        Random rand = new Random();
        HabitacionDbAdapter habitacionDbAdapter = new HabitacionDbAdapter(ctx);
        ReservasDbAdapter reservasDbAdapter = new ReservasDbAdapter(ctx);
        habitacionDbAdapter.open();
        reservasDbAdapter.open();

        int maxOcupantes = 1;
        double precioNoche = 100.0;
        double porcentajeRecargo = 0.1;

        String nombreCliente = "Juliana";
        String telefonoCliente = "653456787";
        String fechaEntrada = "24/05/2023";
        String fechaSalida = "29/05/2023";

        reservasDbAdapter.deleteAllInfoReserva();
        reservasDbAdapter.deleteAllReservas();
        habitacionDbAdapter.deleteAllHabitaciones();

        for (int i = 1; i <= 4; i++) {
            String id = String.valueOf(i);
            String descripcion = "Habitación número " + i;

            long resultado = habitacionDbAdapter.createHabitacion(id, descripcion, maxOcupantes+1,
                    precioNoche+100, porcentajeRecargo);

            if (resultado != -1) {
                android.util.Log.d("AjustesPred", "Habitación creada exitosamente con ID: " + i);
                long idReserva = reservasDbAdapter.createReserva(nombreCliente,telefonoCliente,fechaEntrada,fechaSalida);
                if (idReserva != -1) {
                    android.util.Log.d("AjustesPred", "Reserva creada exitosamente " + i);
                    long result = reservasDbAdapter.insertarInfoReserva(idReserva, rand.nextInt(4) + 1, 6, 300);
                    android.util.Log.d("AjustesPred", "Habitacion reserva creada exitosamente con ID: " + result);
                } else {
                    android.util.Log.d("AjustesPred", "Error al crear la Reserva: " + idReserva);
                    break;
                }
            } else {
                android.util.Log.d("AjustesPred", "Error al crear la habitación: " + i);
            }
        }

        habitacionDbAdapter.close();
        reservasDbAdapter.close();
    }
    public static void testSobrecarga(Context ctx) {
        HabitacionDbAdapter habitacionDbAdapter = new HabitacionDbAdapter(ctx);
        habitacionDbAdapter.open();

        int numHabitaciones = 100000; // Número de habitaciones a crear
        int maxOcupantes = 1;
        double precioNoche = 100.0;
        double porcentajeRecargo = 0.1;
        long tiempoInicio = System.currentTimeMillis();

        for (int i = 1; i <= numHabitaciones; i++) {
            String id = String.valueOf(i);
            String descripcion = "Habitación número " + i;

            // Aumentar la longitud de la descripción en cada iteración
            int longitudDescripcion = (int) Math.pow(2, i);
            for (int j = 0; j < longitudDescripcion; j++) {
                descripcion += "a";
            }

            long resultado = habitacionDbAdapter.createHabitacion(id, descripcion, maxOcupantes,
                    precioNoche, porcentajeRecargo);

            long tiempoTranscurrido = System.currentTimeMillis() - tiempoInicio;

            if (resultado != -1) {
                android.util.Log.d("TestSobrecarga", "Habitación creada exitosamente con ID: " + i + "tamaño descripcion " + descripcion.length());
                if (tiempoTranscurrido > 150000) {
                    android.util.Log.d("TestSobrecarga", "Se ha alcanzado el tiempo máximo permitido." + descripcion.length());
                    break; // Detener el bucle
                }
            } else {
                android.util.Log.d("<TestSobrecarga>", "Error al crear la habitación: " + i);
                break;
            }
        }

        habitacionDbAdapter.close();
    }

    public static void testCajaNegraHabitaciones(Context ctx) {
        HabitacionDbAdapter habitacionDbAdapter = new HabitacionDbAdapter(ctx);
        habitacionDbAdapter.open();

        habitacionDbAdapter.deleteAllHabitaciones();

        //CASOS DE CREAR HABITACIÓN

        // Caso válido
        long resultado1 = habitacionDbAdapter.createHabitacion("1", "Habitación individual", 1, 100.0, 0.1);
        System.out.println("Resultado 1: " + resultado1); // Se espera un valor de inserción válido (> 0)

        // ID vacío
        long resultado2 = habitacionDbAdapter.createHabitacion("", "Habitación doble", 2, 150.0, 0.2);
        System.out.println("Resultado 2: " + resultado2); // Se espera -1 debido a que el ID está vacío

        // ID con caracteres no numéricos
        long resultado3 = habitacionDbAdapter.createHabitacion("A1", "Habitación triple", 3, 200.0, 0.3);
        System.out.println("Resultado 3: " + resultado3); // Se espera -1 debido a que el ID contiene caracteres no numéricos

        // Descripción nula
        long resultado4 = habitacionDbAdapter.createHabitacion("2", null, 1, 100.0, 0.1);
        System.out.println("Resultado 4: " + resultado4); // Se espera -1 debido a que la descripción es nula

        // Descripción vacía
        long resultado5 = habitacionDbAdapter.createHabitacion("3", "", 2, 150.0, 0.2);
        System.out.println("Resultado 5: " + resultado5); // Se espera -1 debido a que la descripción está vacía

        // MAX_OCUPANTES fuera de rango
        long resultado6 = habitacionDbAdapter.createHabitacion("4", "Habitación familiar", 12, 200.0, 0.15);
        System.out.println("Resultado 6: " + resultado6); // Se espera -1 debido a que MAX_OCUPANTES está fuera del rango permitido

        // precioNoche negativo
        long resultado7 = habitacionDbAdapter.createHabitacion("5", "Habitación estándar", 2, -50.0, 0.1);
        System.out.println("Resultado 7: " + resultado7); // Se espera -1 debido a que el precioNoche es negativo

        // porcentaje_recargo nulo
        long resultado8 = habitacionDbAdapter.createHabitacion("6", "Habitación de lujo", 2, 300.0, null);
        System.out.println("Resultado 8: " + resultado8); // Se espera -1 debido a que porcentaje_recargo es nulo

        // Todos los campos inválidos
        long resultado9 = habitacionDbAdapter.createHabitacion(null, "", null, null, null);
        System.out.println("Resultado 9: " + resultado9); // Se espera -1 debido a que todos los campos son inválidos

        //CASOS DE ACTUALIZACIÓN

        long rowId = habitacionDbAdapter.createHabitacion("2", "Habitación individual", 1, 100.0, 0.1);

        // Caso válido de actualización
        boolean resultado10 = habitacionDbAdapter.updateHabitacion(rowId, "Habitación doble", 2, 150.0, 0.2);
        System.out.println("Resultado 10: " + resultado1); // Se espera true

        // rowId inválido
        boolean resultado11 = habitacionDbAdapter.updateHabitacion(0, "Habitación triple", 3, 200.0, 0.3);
        System.out.println("Resultado 11: " + resultado11); // Se espera false debido a que rowId es inválido

        // Descripción nula
        boolean resultado12 = habitacionDbAdapter.updateHabitacion(rowId, null, 1, 100.0, 0.1);
        System.out.println("Resultado 12: " + resultado12); // Se espera false debido a que la descripción es nula

        // Descripción vacía
        boolean resultado13 = habitacionDbAdapter.updateHabitacion(rowId, "", 2, 150.0, 0.2);
        System.out.println("Resultado 13: " + resultado13); // Se espera false debido a que la descripción está vacía

        // MAX_OCUPANTES fuera de rango
        boolean resultado14 = habitacionDbAdapter.updateHabitacion(rowId, "Habitación familiar", 12, 200.0, 0.15);
        System.out.println("Resultado 14: " + resultado14); // Se espera false debido a que MAX_OCUPANTES está fuera del rango permitido

        // precioNoche negativo
        boolean resultado15 = habitacionDbAdapter.updateHabitacion(rowId, "Habitación estándar", 2, -50.0, 0.1);
        System.out.println("Resultado 15: " + resultado15); // Se espera false debido a que el precioNoche es negativo

        // porcentaje_recargo nulo
        boolean resultado16 = habitacionDbAdapter.updateHabitacion(rowId, "Habitación de lujo", 2, 300.0, null);
        System.out.println("Resultado 16: " + resultado16); // Se espera false debido a que porcentaje_recargo es nulo

        // Todos los campos inválidos
        boolean resultado17 = habitacionDbAdapter.updateHabitacion(rowId, null, null, null, null);
        System.out.println("Resultado 17: " + resultado17); // Se espera false debido a que todos los campos son inválidos

        //CASO DE BORRAR HABITACIÓN
        long rowId2 = habitacionDbAdapter.createHabitacion("3", "Habitación individual", 1, 100.0, 0.1);

        // Caso válido de actualización
        boolean resultado18 = habitacionDbAdapter.deleteHabitacion(rowId2);
        System.out.println("Resultado 18: " + resultado18); // Se espera true

        // rowId inválido
        boolean resultado19 = habitacionDbAdapter.deleteHabitacion(0);
        System.out.println("Resultado 19: " + resultado19); // Se espera false debido a que rowId es inválido

        habitacionDbAdapter.close();
    }

    public static void pruebaVolumen(Context ctx) {
        Random rand = new Random();

        HabitacionDbAdapter habitacionDbAdapter = new HabitacionDbAdapter(ctx);
        ReservasDbAdapter reservasDbAdapter = new ReservasDbAdapter(ctx);
        habitacionDbAdapter.open();
        reservasDbAdapter.open();

        int numHabitaciones = 300;
        int numReservas = 3000;
        int maxOcupantes = 1;
        double precioNoche = 100.0;
        double porcentajeRecargo = 0.1;

        String nombreCliente = "Juliana";
        String telefonoCliente = "653456787";
        String fechaEntrada = "24/05/2023";
        String fechaSalida = "29/05/2023";

        reservasDbAdapter.deleteAllInfoReserva();
        reservasDbAdapter.deleteAllReservas();
        habitacionDbAdapter.deleteAllHabitaciones();

        for (int i = 1; i <= numHabitaciones; i++) {
            String id = String.valueOf(i);
            String descripcion = "Habitación número " + i;

            long resultado = habitacionDbAdapter.createHabitacion(id, descripcion, maxOcupantes,
                    precioNoche, porcentajeRecargo);

            if (resultado != -1) {
                android.util.Log.d("PruebaVolumen", "Habitación creada exitosamente con ID: " + i);
            } else {
                android.util.Log.d("PruebaVolumen>", "Error al crear la habitación: " + i);
                break;
            }
        }

        for (int i = 1; i <= numReservas; i++) {
            long idReserva = reservasDbAdapter.createReserva(nombreCliente,telefonoCliente,fechaEntrada,fechaSalida);
            if (idReserva != -1) {
                android.util.Log.d("PruebaVolumen", "Reserva creada exitosamente " + i);
                long result = reservasDbAdapter.insertarInfoReserva(idReserva,rand.nextInt(300) + 1, 6, 300);
                android.util.Log.d("PruebaVolumen", "Habitacion reserva creada exitosamente con ID: " + result);

            } else {
                android.util.Log.d("PruebaVolumen>", "Error al crear la Reserva: " + idReserva);
                break;
            }
        }

        habitacionDbAdapter.close();
        reservasDbAdapter.close();
    }

}






