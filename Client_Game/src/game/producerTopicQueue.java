/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game;

import javax.jms.*;
import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;

/**
 *
 * @author marlon
 */
public class producerTopicQueue {   
    private static final String USER = ActiveMQConnection.DEFAULT_USER; 
    private static final String PASSWORD = ActiveMQConnection.DEFAULT_PASSWORD; 
    //private static final String DESTINATION_QUEUE = "MasterSlave.Queue"; 
    private static final boolean TRANSACTED_SESSION = true;
    public void sendMessages(String mensaje, String cliente, int servidor, int peticion, String topic) throws JMSException {
 
        final ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(USER, PASSWORD, mainGame.topicQueueIP);
        Connection connection = connectionFactory.createConnection();
        connection.start();
 
        final Session session = connection.createSession(TRANSACTED_SESSION, Session.AUTO_ACKNOWLEDGE);
        final Destination destination = session.createQueue(mainGame.topicQueueName);
 
        final MessageProducer producer = session.createProducer(destination);
        producer.setDeliveryMode(DeliveryMode.PERSISTENT);
 
        sendMessage(mensaje, session, producer, cliente, servidor, peticion, topic);
        session.commit();
 
        session.close();
        connection.close();
    }
 
    private void sendMessage(String message, Session session, MessageProducer producer, String cliente, int server, int peticion, String topic) throws JMSException {
        final TextMessage textMessage = session.createTextMessage(message);
        textMessage.setStringProperty("topic",topic);
        textMessage.setStringProperty("cliente",cliente);
        textMessage.setIntProperty("servidor",server);
        textMessage.setIntProperty("tipoPeticion", peticion); //1 Topicos que tienes, 0 es respuesta
        producer.setTimeToLive(60000); //Un minuto de vida, es comunicacion directa, el supuesto es que no se caiga
        producer.send(textMessage);
    }
}
