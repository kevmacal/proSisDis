/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package slave_data;
import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;
 
import javax.jms.*;

/**
 *
 * @author marlon
 */
public class consumerSlave {
    private static final String URL = "tcp://192.168.0.5:61616"; 
    private static final String USER = ActiveMQConnection.DEFAULT_USER; 
    private static final String PASSWORD = ActiveMQConnection.DEFAULT_PASSWORD; 
    private static final String DESTINATION_QUEUE = "Programacion.Queue"; 
    private static final boolean TRANSACTED_SESSION = false; 
    private int totalConsumedMessages = 0; 
    public void processMessages() throws JMSException {
 
        final ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(USER, PASSWORD, URL);
        final Connection connection = connectionFactory.createConnection();
 
        connection.start();
 
        final Session session = connection.createSession(TRANSACTED_SESSION, Session.AUTO_ACKNOWLEDGE);
        final Destination destination = session.createQueue(DESTINATION_QUEUE);
        final MessageConsumer consumer = session.createConsumer(destination);
 
        processMessagesInQueue(consumer);
 
        consumer.close();
        session.close();
        connection.close();
    }
    private void processMessagesInQueue(MessageConsumer consumer) throws JMSException {
        Message message;
        int i=0;
        while (i<1000) {
            if((message = consumer.receive()) != null){
                proccessMessage(message);
            }
        }
    }
    private void proccessMessage(Message message) throws JMSException {
        if (message instanceof TextMessage) {
            final TextMessage textMessage = (TextMessage) message;
            final String text = textMessage.getText();
            totalConsumedMessages++;
            System.out.println("text: "+text+" - total: "+totalConsumedMessages);
        }
    }
}
