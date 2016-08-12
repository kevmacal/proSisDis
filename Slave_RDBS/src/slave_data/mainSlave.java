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
    
    public static void main(String[] args)  throws JMSException {
        final consumerSlave waitPlayer = new consumerSlave();
        waitPlayer.processMessages();
    }
}
