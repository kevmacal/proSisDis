package manager;


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
            final consumerManager wait = new consumerManager();
            try {
                wait.processMessages(cola);
            } catch (InterruptedException | JMSException ex) {
                Logger.getLogger(mainManager.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
    }
