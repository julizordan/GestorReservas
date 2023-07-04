package es.unizar.eina.gestorReservas.send;

public interface SendAbstraction {

    /** Definicion del metodo que permite enviar la nota con el asunto (subject) y cuerpo (body) que se reciben como parametros
     * @param subject asunto
     * @param body cuerpo del mensaje
     */
    public void send(String subject, String body);
}
