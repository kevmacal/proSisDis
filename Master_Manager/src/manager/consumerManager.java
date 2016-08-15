/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package manager;

import java.util.ArrayList;
import java.util.Enumeration;
import javax.jms.*;
import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;

/**
 *
 * @author marlon
 */
public class consumerManager {
    private static final String USER = ActiveMQConnection.DEFAULT_USER; 
    private static final String PASSWORD = ActiveMQConnection.DEFAULT_PASSWORD;
    private static final String URL = "tcp://192.168.1.3:61616";
    private static final boolean TRANSACTED_SESSION = false;
    private final ArrayList<Integer> servers;
    private String colaDest;
    //private int totalConsumedMessages = 0; 
    private static final int NUMSERVERS = 1; //Numero manualmente, cada servidor tiene su numero
    private static final String SLAVE_QUEUE = "MasterSlave.Queue";
    private static final String CLIENT_QUEUE = "MasterClient.Queue";
    public consumerManager(){
        servers=new ArrayList<>();
        int i=1;
        while(i<=NUMSERVERS){
            servers.add(i);
            i++;
        }
    }
    public void processMessages(String queueDest) throws JMSException, InterruptedException {
 
        final ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(USER, PASSWORD, URL);
        final javax.jms.Connection connection = connectionFactory.createConnection();
 
        connection.start();
        this.colaDest=queueDest;
        final Session session = connection.createSession(TRANSACTED_SESSION, Session.AUTO_ACKNOWLEDGE);
        
                
        if(queueDest.equals(CLIENT_QUEUE)){
            processMessagesInQueueClient(session);
        }else{
            processMessagesInQueueSlave(session);
        }
        session.close();
        connection.close();
    }
    private void processMessagesInQueueClient(Session session) throws JMSException, InterruptedException {
        Message message;
        int i=0;
        QueueBrowser queuePeek;
        MessageConsumer consumer;
        while (i<1000) {
            Thread.sleep(70);
            //System.out.println(i);
            queuePeek = session.createBrowser(session.createQueue(colaDest));
            Enumeration<?> messagesInQueue = queuePeek.getEnumeration();
            Message peek = (Message) messagesInQueue.nextElement();
            if (peek!=null){
                if (confirmMessageClient(peek)==1){
                    consumer = session.createConsumer(session.createQueue(colaDest));
                    if((message = consumer.receive()) != null){
                        proccessMessage(message);
                        consumer.close();
                    }
                }
            }
            queuePeek.close();
            i++;
        }
    }
    private int confirmMessageClient(Message message) throws JMSException{
        int valorVerdad = 0;
        if (message instanceof TextMessage) {
            final TextMessage textMessage = (TextMessage) message;
            int tipoPeticion = textMessage.getIntProperty("tipoPeticion");
            //final String text = textMessage.getText();
            //int servidor = textMessage.getIntProperty("servidor");
            //String[] tokens = text.split(",");
            //int servidor = Integer.parseInt(tokens[0]);            
            if(tipoPeticion >= 1){
                valorVerdad = 1;
            }
            //System.out.println("Confirmando text: "+text+" - total: "+totalConsumedMessages);
        }
        return valorVerdad;
    }
    private void proccessMessage(Message message) throws JMSException {
        if (message instanceof TextMessage) {
            final TextMessage textMessage = (TextMessage) message;
            String propietario = textMessage.getStringProperty("cliente");
            int peticion=textMessage.getIntProperty("tipoPeticion");
            int servidor=textMessage.getIntProperty("servidor");
            String cliente=textMessage.getStringProperty("cliente");
            final String texto = textMessage.getText();
            if(servidor==0){
                switch(peticion){
                    case 1:
                        peticionTopicos(cliente);
                        break;
                    case 2:
                        aceptarTopico(cliente, texto);
                        break;
                    default:
                        System.err.println("Error en la peticion");
                        break;
                }
            }else{
                if(texto.split(";")[0].equals("queue")){
                    //System.out.println("Ya se genera la cola para comunicacion directa");
                    respuestaColaCreada(cliente, texto, servidor);
                }else{
                    respuestaTopicos(cliente, texto, servidor);
                }
            }
            //totalConsumedMessages++;
            System.out.println(propietario+" = Procesa text: "+texto);
        }
    }
    private void processMessagesInQueueSlave(Session session) throws JMSException, InterruptedException {
        Message message;
        int i=0;
        QueueBrowser queuePeek;
        MessageConsumer consumer;
        while (i<1000) {
            Thread.sleep(70);
            //System.out.println(i);
            queuePeek = session.createBrowser(session.createQueue(colaDest));
            Enumeration<?> messagesInQueue = queuePeek.getEnumeration();
            Message peek = (Message) messagesInQueue.nextElement();
            if (peek!=null){
                if (confirmMessageSlave(peek)==1){
                    consumer = session.createConsumer(session.createQueue(colaDest));
                    if((message = consumer.receive()) != null){                        
                        proccessMessage(message);
                        consumer.close();
                    }
                }
            }
            queuePeek.close();
            i++;
        }
    }
    private int confirmMessageSlave(Message message) throws JMSException{
        int valorVerdad = 0;
        if (message instanceof TextMessage) {
            final TextMessage textMessage = (TextMessage) message;
            int tipoPeticion = textMessage.getIntProperty("tipoPeticion");
            //final String text = textMessage.getText();
            //int servidor = textMessage.getIntProperty("servidor");
            //String[] tokens = text.split(",");
            //int servidor = Integer.parseInt(tokens[0]);            
            if(tipoPeticion == 0){
                valorVerdad = 1;
            }
            //System.out.println("Confirmando text: "+text+" - total: "+totalConsumedMessages);
        }
        return valorVerdad;
    }
    /*Procesar peticiones*/
    /*Peticion de topicos a sus servidores*/
    private void peticionTopicos(String cliente) throws JMSException{
        int i;
        for(i=0;i<servers.size();i++){
            producerManager pm=new producerManager();
            pm.sendMessages(SLAVE_QUEUE, "Peticion Topico", cliente, servers.get(i), 1);
        }
    }
    private void aceptarTopico(String cliente, String Mensaje) throws JMSException{
        producerManager pm = new producerManager();
        String[] serv_topi=Mensaje.split(";");
        pm.sendMessages(SLAVE_QUEUE, "queue;"+cliente+"."+serv_topi[2], cliente, Integer.parseInt(serv_topi[1]), 2);
    }
    
    private void respuestaTopicos(String cliente, String Mensaje,int servidor) throws JMSException{
        producerManager pm = new producerManager();
        pm.sendMessages(CLIENT_QUEUE, "topicsAnswer;" + Mensaje, cliente, servidor, 0);
    }
    private void respuestaColaCreada(String cliente, String Mensaje,int servidor) throws JMSException{
        producerManager pm = new producerManager();
        pm.sendMessages(CLIENT_QUEUE,Mensaje, cliente, servidor, 0);
    }
}
