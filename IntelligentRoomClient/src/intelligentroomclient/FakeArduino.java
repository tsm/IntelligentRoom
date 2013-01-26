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
    
    FakeArduino(IntelligentRoomClient client) {
        this.client = client;
    }

    @Override
    public void run() {
        while (true){
            if (client.isRunning){
                if (client.sunrise.timeEquals(client.time)){
                    
                }
            }
        }
    }
    
}
