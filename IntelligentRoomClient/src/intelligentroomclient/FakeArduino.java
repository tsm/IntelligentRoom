/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package intelligentroomclient;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Krzychu
 */
public class FakeArduino implements Runnable{

    IntelligentRoomClient client;
    double minLight = 0;
    double sunMuliplier = 1.2;
    
    FakeArduino(IntelligentRoomClient client) {
        this.client = client;
    }

    @Override
    public void run() {
        while (true){
            try {
                Thread.sleep(200);
            } catch (InterruptedException ex) {
                Logger.getLogger(FakeArduino.class.getName()).log(Level.SEVERE, null, ex);
            }
            if (client.isRunning){
                int light = (int) (minLight + client.getLamp0()*sunMuliplier+client.getLamp1());
                client.stepSimulation(light, 26.0);
            }
        }
    }
    
}
