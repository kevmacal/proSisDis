/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package slave_data;
 
import javax.jms.*;

/**
 *
 * @author marlon
 */
public class mainSlave {
    
    public static final String SERVER_IP="tcp://192.168.1.3:61616";  //Se caambia manualmente para cada servidor
    
    public static void main(String[] args)  throws JMSException, InterruptedException {
        final consumerSlave waitPlayer = new consumerSlave();
        waitPlayer.processMessages();
    }
}
