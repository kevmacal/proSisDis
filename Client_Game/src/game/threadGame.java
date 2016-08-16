/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.jms.JMSException;

/**
 *
 * @author marlon
 */
public class threadGame implements Runnable{
    private String cliente;
    private int servidor;
    private String topico;
    private int stop;
    private int onTime;

    public void setStop(int stop) {
        this.stop = stop;
    }

    public int getOnTime() {
        return onTime;
    }    
    
    
    public threadGame(String cliente, int servidor, String topico){
        this.cliente=cliente;
        this.servidor=servidor;
        this.topico=topico;
        this.stop=0;
        this.onTime=1;
    }
    @Override
    public void run() {
        int i=0;
        int tiempo;
        int print=0;
        /*Un minuto*/
        while(i<120&&stop==0){
            tiempo=(120-i)/2;
            try {
                Thread.sleep(500);
            } catch (InterruptedException ex) {
                Logger.getLogger(threadGame.class.getName()).log(Level.SEVERE, null, ex);
            }
            if(tiempo%10==0&&print==0){
                System.out.println("Quedan: "+tiempo+" segundos");
                print=1;
            }else{
                print=0;
            }
            i++;
        }
        if(stop==0){
            onTime=0;
            producerTopicQueue ptq=new producerTopicQueue();
            try {
                ptq.sendMessages("endGame;Termino el tiempo", cliente, servidor, 5, topico);
                ptq.sendMessages("endGame;Termino el tiempo", cliente, servidor, 0, topico);
            } catch (JMSException ex) {
                Logger.getLogger(threadGame.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
}
