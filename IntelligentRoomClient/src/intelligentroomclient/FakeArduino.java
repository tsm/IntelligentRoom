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
    double lightMin = 100;
    double lightMax = 500;
    
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
                double actual = client.time.getTime() % SimulationTime.DAY_SECS;
                double sunRise = client.sunrise.getTime();
                double sunSet = client.sunset.getTime();
                double dayCenter = (sunSet+sunRise)/2;
                double halfDay = dayCenter-sunRise;
                double light;
                System.out.println("Actual: "+actual+", sunRise: "+sunRise+", dayCenter: "+dayCenter+", sunSet: "+sunSet);
                if ( Math.abs(dayCenter - actual)>=Math.abs(halfDay)){
                    // Isn't between sunrise and sunset
                    light = 100;
                }else{
                    //Is between then use proportion to linearize light value
                    light = lightMin + (halfDay - Math.abs(dayCenter-actual))*(lightMax-lightMin)/halfDay;
                }
                client.stepSimulation((int)light, 26.0);
            }
        }
    }
    
}
