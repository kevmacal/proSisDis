/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package slave_data;

import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Enumeration;
import javax.jms.*;
import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;

/**
 *
 * @author marlon
 */
public class consumerSlaveGame {
    private static final String URL = "tcp://192.168.1.3:61616"; //Corresponde al servidor, se asigna manualmente
    private final int numServer = 1; //Este numero se asigna manualmente
    private static final String USER = ActiveMQConnection.DEFAULT_USER; 
    private static final String PASSWORD = ActiveMQConnection.DEFAULT_PASSWORD;
    private static final boolean TRANSACTED_SESSION = false; 
    private static final String USERNAME="root";
    private static final String PASSWORDBD="0984287897";
    private static final String CONN_STRING="jdbc:mysql://localhost:3306/prueba"; //Ambiente de pruebas
    private String queue;
    public void processMessages(String queue) throws JMSException, InterruptedException {
 
        final ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(USER, PASSWORD, URL);
        final javax.jms.Connection connection = connectionFactory.createConnection();
 
        connection.start();
        this.queue=queue;
        final Session session = connection.createSession(TRANSACTED_SESSION, Session.AUTO_ACKNOWLEDGE);
        //final Destination destination = session.createQueue(DESTINATION_QUEUE);
        
 
        processMessagesInQueue(session);
        
        session.close();
        connection.close();
    }
    private void processMessagesInQueue(Session session) throws JMSException, InterruptedException {
        Message message;
        int i=0;
        QueueBrowser queuePeek;
        MessageConsumer consumer;
        while (i<1000) {
            Thread.sleep(70);
            //System.out.println(i);
            queuePeek = session.createBrowser(session.createQueue(queue));
            Enumeration<?> messagesInQueue = queuePeek.getEnumeration();
            Message peek = (Message) messagesInQueue.nextElement();
            if (peek!=null){
                if (confirmMessage(peek)==1){
                    consumer= session.createConsumer(session.createQueue(queue));
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
    private int confirmMessage(Message message) throws JMSException{
        int valorVerdad = 0;
        if (message instanceof TextMessage) {
            final TextMessage textMessage = (TextMessage) message;
            int servidor = textMessage.getIntProperty("servidor");
            int tipoPeticion = textMessage.getIntProperty("tipoPeticion");
            final String text = textMessage.getText();
            //String[] tokens = text.split(",");
            //int servidor = Integer.parseInt(tokens[0]);            
            if(servidor == numServer && tipoPeticion >= 1){
                valorVerdad = 1;
            }
            //System.out.println("Confirmando text: "+text+" - total: "+totalConsumedMessages);
        }
        return valorVerdad;
    }
    private void proccessMessage(Message message) throws JMSException {
        producerTopicQueue ptq=new producerTopicQueue();
        int i;
        String concat="";
        if (message instanceof TextMessage) {
            final TextMessage textMessage = (TextMessage) message;
            String propietario = textMessage.getStringProperty("cliente");
            int peticion=textMessage.getIntProperty("tipoPeticion");
            int servidor=textMessage.getIntProperty("servidor");
            String topic = textMessage.getStringProperty("topic");
            final String text = textMessage.getText();
            switch(peticion){
                case 1:
                    concat=concat+"hs;";
                    for(i=0;i<mainSlave.hScores.get(topic).size();i++){
                        concat=concat+mainSlave.hScores.get(topic).get(i)+";";
                    }
                    ptq.sendMessages(queue, concat, propietario, servidor, 0, topic);
                    break;
                case 2:                    
                    System.out.println("Juego");
                     break;
                default:
                    System.out.println("No es peticion, es respuesta");
                    break;
            }
            System.out.println(propietario+" = Procesa text: "+text);
        }
    }
}
