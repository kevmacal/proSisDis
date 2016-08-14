/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package manager;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.jms.*;
import jdk.nashorn.internal.codegen.CompilerConstants;
/**
 *
 * @author marlon
 */

public class mainManager {
    private static final String CLIENT_QUEUE = "MasterClient.Queue";
    private static final String SLAVE_QUEUE = "MasterSlave.Queue";
    
    
    public static void main(String[] args)  throws JMSException, InterruptedException {
        Runnable r1=new queueThread(CLIENT_QUEUE);
        Runnable r2=new queueThread(SLAVE_QUEUE);
        
        Thread t1=new Thread(r1);
        Thread t2=new Thread(r2);
        
        t1.start();
        t2.start();
        
        t1.join();
        t2.join();
        
    }
}
