/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package manager;

import javax.jms.Connection;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;
import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;

/**
 *
 * @author marlon
 */
public class producerManager {
    private static final String URL = "tcp://192.168.1.3:61616"; 
    private static final String USER = ActiveMQConnection.DEFAULT_USER; 
    private static final String PASSWORD = ActiveMQConnection.DEFAULT_PASSWORD; 
    private static final boolean TRANSACTED_SESSION = true;
    public void sendMessages(String queueDest, String mensaje, String cliente, int servidor, int peticion) throws JMSException {
        final ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(USER, PASSWORD, URL);
        Connection connection = connectionFactory.createConnection();
        connection.start();
 
        final Session session = connection.createSession(TRANSACTED_SESSION, Session.AUTO_ACKNOWLEDGE);
        final Destination destination = session.createQueue(queueDest);
 
        final MessageProducer producer = session.createProducer(destination);
        producer.setDeliveryMode(DeliveryMode.PERSISTENT);
 
        sendMessage(mensaje, session, producer, cliente, servidor, peticion);
        session.commit();
 
        session.close();
        connection.close();
    }
    private void sendMessage(String message, Session session, MessageProducer producer, String cliente, int server, int peticion) throws JMSException {
        final TextMessage textMessage = session.createTextMessage(message);
        textMessage.setStringProperty("cliente",cliente);
        textMessage.setIntProperty("servidor",server);
        textMessage.setIntProperty("tipoPeticion", peticion); //1 Topicos que tienes, 0 es respuesta
        producer.send(textMessage);
    }
}
