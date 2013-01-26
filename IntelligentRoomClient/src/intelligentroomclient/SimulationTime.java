/*
 * Helper clas to maintain time.
 */
package intelligentroomclient;

/**
 *
 * @author tsm
 */
public class SimulationTime {
    
    final public static int MIN_SECS = 60;
    final public static int HOUR_SECS = 3600;
    final public static int DAY_SECS = 86400;
    
    private int time=0;
    
    public SimulationTime(int t){
        time=0;
    }
    
    public SimulationTime(int h, int m){
        time=m*60+h*3600;
    }

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }
    
    public void addSecs(int s){
        time+=s;
    }
    
    public int getMin(){
        return time/MIN_SECS%60;
    }
    
    public int getHour(){
        return time/HOUR_SECS%24;
    }
    
    public int getDay(){
        return time/DAY_SECS;
    }
    public void setMin(int m){
        time = time-getMin()*MIN_SECS+m*MIN_SECS;
    }
    public void setHour(int h){
        time = time-getHour()*HOUR_SECS+h*HOUR_SECS;
    }
    public void setDay(int d){
        time = time-getDay()*DAY_SECS+d*DAY_SECS;
    }
    
    public boolean timeEquals(SimulationTime st){        
        return (st.getHour()==this.getHour())&&(st.getMin()==this.getMin());
    }
}
