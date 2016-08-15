package slave_data;


import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.jms.JMSException;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author marlon
 */
public class queueThread implements Runnable{
        private String cola;
        
        public queueThread(String cola){
            this.cola=cola;
        }
        
        @Override
        public void run() {
            final consumerSlaveGame wait = new consumerSlaveGame();
            try {
                wait.processMessages(cola);
            } catch (JMSException ex) {
                Logger.getLogger(queueThread.class.getName()).log(Level.SEVERE, null, ex);
            } catch (InterruptedException ex) {
                Logger.getLogger(queueThread.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
    }
