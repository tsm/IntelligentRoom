package intelligentroomclient;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JSpinner;
import javax.swing.SpinnerDateModel;
import jssc.SerialPort;
import jssc.SerialPortEvent;
import jssc.SerialPortEventListener;
import jssc.SerialPortException;

/**
 * Intelligent room: bright control by Aruino
 * 
 * @author tsm & Smilasek
 * tomszom.com
 */
public class IntelligentRoomClient extends javax.swing.JFrame {
    
    SerialPort serialPort;
    
    int photo=0;
    int optimalIllumination=350;
    double temp=0.0;
    
    boolean isRunning=false;
    
    SimulationTime time; //in sec
    
    int lamp0=0;
    int lamp1=0;
    
    SimulationTime sunrise=new SimulationTime(4,30);
    SimulationTime sunset=new SimulationTime(18,0);
    SimulationTime fromTime=new SimulationTime(15,0);
    SimulationTime toTime=new SimulationTime(22,0);
    
    URLConnectionReader ucr;
    
    public void stepSimulation(int photo, double temp){
        setPhoto(photo);
        setTemp(temp);
        
        //get lamp value from serwer
        if(ucr!=null){
           ucr= new URLConnectionReader();
           String params="setLight?client="+client_form.getText()+"&value="+getPhoto()+"&t_h="+time.getHour()+"&t_m="+time.getMin();
           String resp = ucr.sendGetRequest(address_from.getText(), port_form.getText(),params);
           if(resp.startsWith("Sterowanie oswietleniem jest nieaktywne")){
              status_lbl.setText("Light control inactive");
              if(!lamp1_slider.isEnabled()) setLamp1(0); //moment wyłączenia trybu "AUTO"
              lamp1_slider.setEnabled(true);
              lamp1_form.setEnabled(true);
              
           }
           else{
               if(resp.startsWith("Nie znaleziono klienta")){
                   status_lbl.setText("Error: client "+client_form.getText()+" not found");
               }else{
                   lamp1_slider.setEnabled(false);
                   lamp1_form.setEnabled(false);
                   try{
                        resp=resp.substring(1);
                        int newvalue=Integer.valueOf(resp);
                        setLamp1(newvalue);
                        status_lbl.setText("OK");
                   }catch (Exception ex) {
                        status_lbl.setText("Error: unexpected server response >"+resp+"<");
                    }                  
               }
           }
           
          
        }
        
        //Write to arduino
        try {
            if (!cb_COM.getSelectedItem().toString().equals("FAKE")){
                serialPort.writeString(getLamp0()+","+getLamp1()+"\n");
            }
        } catch (SerialPortException ex) {
            Logger.getLogger(IntelligentRoomClient.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        if(isRunning && console!=null){                                

            //RAPORT:
            int daytime= new SimulationTime(time.getHour(), time.getMin()).getTime();
            int stepTime=((Integer) simulationStepSpinner.getValue())*SimulationTime.MIN_SECS;
            //if(time.getHour()==0&&time.getMin()==0){
            if(daytime>=0 && daytime<stepTime){
                console.writeMsg("==== Day "+time.getDay()+" ====");
            }
            
            //if(time.getHour()>=sunrise.getHour()&&time.getMin()==sunrise.getMin()){
            if(sunrise.getTime()>daytime-stepTime && sunrise.getTime()<=daytime){
                console.writeMsg("==== Sun rised ====");
            }
            //if(time.getHour()==sunset.getHour()&&time.getMin()==sunset.getMin()){
            if(sunset.getTime()>daytime-stepTime && sunset.getTime()<=daytime){
                console.writeMsg("==== Sunset ====");
            }
            console.writeMsg(String.format("%2d",time.getHour())+":"+
                    String.format("%02d",time.getMin())+" :: Photoresistor = "+photo+", sun= "+getLamp0()+", lamp= "+getLamp1()+", temp= "+getTemp()+" °C"); 


            time.addSecs(stepTime); // TIME incrementation
            showTime();
        }
        
        //setting the SUN
        int half_sun_time=(sunset.getTime()-sunrise.getTime())/2;
        setLamp0(255-(Math.abs(time.getTime()%SimulationTime.DAY_SECS - sunrise.getTime() - half_sun_time)/(half_sun_time/255))); // MAGIC LINE xD
    }
    
    public int getPhoto() {
        return photo;
    }

    public void setPhoto(int photo) {
        this.photo = photo;
        photo_lbl.setText(String.valueOf(photo));
       /* if (photo < 300){
            setLamp1(getLamp1()+5);
        }
        if (photo > 350){
            setLamp1(getLamp1()-5);
        }*/
    }

    public double getTemp() {
        return temp;
    }

    public void setTemp(double temp) {
        this.temp = temp;
        temp_lbl.setText(temp+" °C");
    }
    
    public int getLamp1() {
        return lamp1;
    }

    public void setLamp1(int lamp1) {
        this.lamp1 = lamp1;        
        if(this.lamp1<0){
            this.lamp1=0;
        }
        if(this.lamp1>255) {
            this.lamp1=255;
        }
        lamp1_slider.setValue(this.lamp1);
    }

    public int getLamp0() {
        return lamp0;
    }

    public void setLamp0(int lamp0) {
        this.lamp0 = lamp0;        
        if(this.lamp0<0){
            this.lamp0=0;
        }
        if(this.lamp0>255) {
            this.lamp0=255;
        }
    }

    public int getOptimalIllumination() {
        return optimalIllumination;
    }

    public void setOptimalIllumination(int optimalIllumination) {
        this.optimalIllumination = optimalIllumination;
        optimal_illumination_lbl.setText(""+optimalIllumination);
    }
    
    
    
    public void showTime(){
        min_lbl.setText(String.format("%02d",time.getMin()));
        hour_lbl.setText(String.format("%2d",time.getHour()));
        day_lbl.setText(""+(time.getDay()));
    }
 
    class SerialPortReader implements SerialPortEventListener {
        String catlines="";
        
        @Override
        public void serialEvent(SerialPortEvent event) {
            if (event.isRXCHAR()) {//If data is available
                //System.out.println(event.getEventValue());
                if (event.getEventValue() > 0) {//Check bytes count in the input buffer

                    
                    try {
                        String line = serialPort.readString();
                        catlines+=line;
                        int newline=catlines.indexOf("\n");
                        while(newline>-1){
                            //System.out.print(catlines.substring(0, newline));
                            String [] measures = catlines.substring(0, newline-1).split(";");
                            if(measures.length==2&&(!measures[0].equals(""))&&(!measures[1].equals(""))){ //zapobiega wczytaniu niepełnych danych
                                stepSimulation(Integer.valueOf(measures[0]), Double.valueOf(measures[1]));                              
                            }
                            catlines=catlines.substring(newline+1);
                            newline=catlines.indexOf("\n");
                        }
                        //serialPort.writeString("50\n");
                        //byte buffer[] = serialPort.readBytes(4);
                        //System.out.println(buffer[0] + "  " + buffer[1] + "  " + buffer[2] + "  " + buffer[3]);
                    } catch (SerialPortException ex) {
                        status_lbl.setText("Error: problem with serial port");
                    } catch (Exception ex){
                        status_lbl.setText("Error: problem with reading from serial port");
                    }
                }
            } else if (event.isCTS()) {//If CTS line has changed state
                if (event.getEventValue() == 1) {//If line is ON
                    System.out.println("CTS - ON");
                } else {
                    System.out.println("CTS - OFF");
                }
            } else if (event.isDSR()) {///If DSR line has changed state
                if (event.getEventValue() == 1) {//If line is ON
                    System.out.println("DSR - ON");
                } else {
                    System.out.println("DSR - OFF");
                }
            }
        }
    }
    
    private void setSpinner(SimulationTime time, JSpinner spinner){
        SpinnerDateModel model = new SpinnerDateModel();
        model.setCalendarField(Calendar.MINUTE);        
        Date def= new Date();
        SimpleDateFormat formatterDefault = new SimpleDateFormat("H:mm"); 
        
        //sunrise time
        try {  
            def = formatterDefault.parse(time.getHour()+":"+time.getMin());  
        } catch (ParseException ex) {
            status_lbl.setText("Time parse problem");
        }  
        spinner.setModel(model);
        spinner.setEditor(new JSpinner.DateEditor(spinner, "H:mm"));
        spinner.setValue(def);
    }
    
    /**
     * Creates new form IntelligentRoomClient
     */
    public IntelligentRoomClient() {
        time=new SimulationTime(0);
        initComponents();
        
        //Set up time spinners:   
        setSpinner(sunrise, sunriseSpinner);
        setSpinner(sunset, sunsetSpinner);
        setSpinner(fromTime, fromTimeSpinner);
        setSpinner(toTime, toTimeSpinner);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jTextField2 = new javax.swing.JTextField();
        jPanel1 = new javax.swing.JPanel();
        temp_lbl = new javax.swing.JLabel();
        photo_lbl1 = new javax.swing.JLabel();
        photo_lbl = new javax.swing.JLabel();
        temp_lbl2 = new javax.swing.JLabel();
        optimal_illumination_btn = new javax.swing.JButton();
        optimal_illumination_lbl = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        set_lamp1_btn = new javax.swing.JButton();
        lamp1_form = new javax.swing.JTextField();
        lamp1_slider = new javax.swing.JSlider();
        jLabel1 = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        hour_lbl = new javax.swing.JLabel();
        start_pause_btn = new javax.swing.JButton();
        reset_btn = new javax.swing.JButton();
        day_lbl = new javax.swing.JLabel();
        day_lbl_const = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        min_lbl = new javax.swing.JLabel();
        sunriseSpinner = new javax.swing.JSpinner();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        sunsetSpinner = new javax.swing.JSpinner();
        jLabel10 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        toTimeSpinner = new javax.swing.JSpinner();
        fromTimeSpinner = new javax.swing.JSpinner();
        jLabel12 = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        simulationStepSpinner = new javax.swing.JSpinner();
        jPanel4 = new javax.swing.JPanel();
        cb_COM = new javax.swing.JComboBox();
        jLabel2 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        address_from = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        client_form = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        port_form = new javax.swing.JTextField();
        connect_arduino_btn = new javax.swing.JButton();
        connect2server_btn = new javax.swing.JButton();
        status_lbl = new javax.swing.JLabel();

        jTextField2.setText("jTextField2");

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("IntelligentRoom by Smilasek and Tsm");
        setIconImage(java.awt.Toolkit.getDefaultToolkit().getImage(IntelligentRoomClient.class.getResource("../img/icon.png")));
        setResizable(false);

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("Real-time measurements"));

        temp_lbl.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        temp_lbl.setText("N/A");

        photo_lbl1.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        photo_lbl1.setText("Photoresistor:");

        photo_lbl.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        photo_lbl.setText("N/A");

        temp_lbl2.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        temp_lbl2.setText("Temperature:");

        optimal_illumination_btn.setText("Set optimal illumination");
        optimal_illumination_btn.setEnabled(false);
        optimal_illumination_btn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                optimal_illumination_btnActionPerformed(evt);
            }
        });

        optimal_illumination_lbl.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        optimal_illumination_lbl.setText(""+getOptimalIllumination());

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(2, 2, 2)
                        .addComponent(temp_lbl2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(temp_lbl)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(photo_lbl1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(photo_lbl)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(optimal_illumination_btn, javax.swing.GroupLayout.PREFERRED_SIZE, 173, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(optimal_illumination_lbl)))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap(19, Short.MAX_VALUE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(photo_lbl1)
                    .addComponent(photo_lbl)
                    .addComponent(optimal_illumination_btn, javax.swing.GroupLayout.DEFAULT_SIZE, 28, Short.MAX_VALUE)
                    .addComponent(optimal_illumination_lbl, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(10, 10, 10)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(temp_lbl2)
                    .addComponent(temp_lbl))
                .addContainerGap())
        );

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder("Manual set"));

        set_lamp1_btn.setText("SetLamp");
        set_lamp1_btn.setActionCommand("Set Lamp");
        set_lamp1_btn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                set_lamp1_btnActionPerformed(evt);
            }
        });

        lamp1_form.setText("0");
        lamp1_form.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                lamp1_formActionPerformed(evt);
            }
        });

        lamp1_slider.setMaximum(255);
        lamp1_slider.setValue(0);
        lamp1_slider.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                lamp1_sliderStateChanged(evt);
            }
        });

        jLabel1.setText("Lamp");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(lamp1_slider, javax.swing.GroupLayout.PREFERRED_SIZE, 262, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lamp1_form, javax.swing.GroupLayout.PREFERRED_SIZE, 59, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(set_lamp1_btn)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(lamp1_slider, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(set_lamp1_btn, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lamp1_form, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder("Simulation control"));
        jPanel3.setEnabled(false);

        hour_lbl.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        hour_lbl.setForeground(new java.awt.Color(0, 0, 102));
        hour_lbl.setText("--");

        start_pause_btn.setText("Start");
        start_pause_btn.setEnabled(false);
        start_pause_btn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                start_pause_btnActionPerformed(evt);
            }
        });

        reset_btn.setText("Reset");
        reset_btn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                reset_btnActionPerformed(evt);
            }
        });

        day_lbl.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        day_lbl.setForeground(new java.awt.Color(0, 0, 102));
        day_lbl.setText("-");

        day_lbl_const.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        day_lbl_const.setForeground(new java.awt.Color(0, 0, 102));
        day_lbl_const.setText("Day");

        jLabel3.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        jLabel3.setForeground(new java.awt.Color(0, 0, 102));
        jLabel3.setText(":");

        min_lbl.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        min_lbl.setForeground(new java.awt.Color(0, 0, 102));
        min_lbl.setText("--");

        sunriseSpinner.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                sunriseSpinnerStateChanged(evt);
            }
        });

        jLabel7.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel7.setText("Sunrise");
        jLabel7.setFocusable(false);
        jLabel7.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);

        jLabel8.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/sun.png"))); // NOI18N

        jLabel9.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel9.setText("Sunset");
        jLabel9.setFocusable(false);
        jLabel9.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);

        sunsetSpinner.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                sunsetSpinnerStateChanged(evt);
            }
        });

        jLabel10.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel10.setText("Time OFF");
        jLabel10.setFocusable(false);
        jLabel10.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);

        jLabel11.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/shutdown.png"))); // NOI18N

        toTimeSpinner.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                toTimeSpinnerStateChanged(evt);
            }
        });

        fromTimeSpinner.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                fromTimeSpinnerStateChanged(evt);
            }
        });

        jLabel12.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel12.setText("Time ON");
        jLabel12.setFocusable(false);
        jLabel12.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);

        jLabel13.setText("Simulation time step in minutes:");

        simulationStepSpinner.setModel(new javax.swing.SpinnerNumberModel(15, 1, 60, 5));
        simulationStepSpinner.setEditor(new javax.swing.JSpinner.NumberEditor(simulationStepSpinner, ""));

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(start_pause_btn, javax.swing.GroupLayout.PREFERRED_SIZE, 89, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(8, 8, 8)
                        .addComponent(reset_btn))
                    .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addGroup(jPanel3Layout.createSequentialGroup()
                            .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addComponent(sunriseSpinner)
                                .addComponent(jLabel7, javax.swing.GroupLayout.DEFAULT_SIZE, 87, Short.MAX_VALUE))
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                            .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addComponent(sunsetSpinner)
                                .addComponent(jLabel9, javax.swing.GroupLayout.PREFERRED_SIZE, 87, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGroup(jPanel3Layout.createSequentialGroup()
                            .addComponent(jLabel13)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                            .addComponent(simulationStepSpinner, javax.swing.GroupLayout.DEFAULT_SIZE, 64, Short.MAX_VALUE))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(day_lbl_const, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(day_lbl, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(58, 58, 58)
                        .addComponent(hour_lbl)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(min_lbl))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(fromTimeSpinner)
                            .addComponent(jLabel12, javax.swing.GroupLayout.PREFERRED_SIZE, 87, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabel11, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(toTimeSpinner)
                            .addComponent(jLabel10, javax.swing.GroupLayout.PREFERRED_SIZE, 87, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(jPanel3Layout.createSequentialGroup()
                                .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, 19, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(sunriseSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(jPanel3Layout.createSequentialGroup()
                            .addComponent(jLabel9, javax.swing.GroupLayout.PREFERRED_SIZE, 19, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(sunsetSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(jPanel3Layout.createSequentialGroup()
                                .addComponent(jLabel12, javax.swing.GroupLayout.PREFERRED_SIZE, 19, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(fromTimeSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(jLabel11, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(jPanel3Layout.createSequentialGroup()
                            .addComponent(jLabel10, javax.swing.GroupLayout.PREFERRED_SIZE, 19, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(toTimeSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 20, Short.MAX_VALUE)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel13)
                            .addComponent(simulationStepSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(hour_lbl)
                            .addComponent(jLabel3)
                            .addComponent(min_lbl)
                            .addComponent(day_lbl)
                            .addComponent(day_lbl_const))
                        .addGap(2, 2, 2)))
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(start_pause_btn)
                    .addComponent(reset_btn))
                .addContainerGap())
        );

        jPanel4.setBorder(javax.swing.BorderFactory.createTitledBorder("Connection"));

        cb_COM.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "COM1", "COM2", "COM3", "COM4", "COM5", "COM6", "COM7", "COM8", "COM9", "COM10", "COM11", "COM12", "FAKE" }));
        cb_COM.setSelectedIndex(2);

        jLabel2.setText("Arduino COM port:");

        jLabel4.setText("Server address: ");

        address_from.setText("localhost");

        jLabel5.setText("Client name:");

        client_form.setText("Client");

        jLabel6.setText("Port:");

        port_form.setText("8181");

        connect_arduino_btn.setText("Connect Arduino");
        connect_arduino_btn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                connect_arduino_btnActionPerformed(evt);
            }
        });

        connect2server_btn.setText("Connect to server");
        connect2server_btn.setEnabled(false);
        connect2server_btn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                connect2server_btnActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 125, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(cb_COM, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addComponent(jLabel4)
                        .addGap(18, 18, 18)
                        .addComponent(address_from, javax.swing.GroupLayout.PREFERRED_SIZE, 116, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel5)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addGap(1, 1, 1)
                        .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, 59, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addComponent(port_form, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(48, 48, 48)
                        .addComponent(connect2server_btn, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addComponent(client_form, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(connect_arduino_btn, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cb_COM, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel5)
                    .addComponent(client_form, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(connect_arduino_btn))
                .addGap(18, 18, 18)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(address_from)
                    .addComponent(jLabel6)
                    .addComponent(port_form, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(connect2server_btn))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        status_lbl.setText("Status: OK");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(status_lbl)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(21, 21, 21)
                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 9, Short.MAX_VALUE)
                .addComponent(status_lbl)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void set_lamp1_btnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_set_lamp1_btnActionPerformed
        setLamp1(Integer.valueOf(lamp1_form.getText()));
    }//GEN-LAST:event_set_lamp1_btnActionPerformed

    private void lamp1_formActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_lamp1_formActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_lamp1_formActionPerformed

    private void lamp1_sliderStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_lamp1_sliderStateChanged
        setLamp1(lamp1_slider.getValue());
        lamp1_form.setText(lamp1_slider.getValue()+"");
        setLamp1(Integer.valueOf(lamp1_form.getText()));
        
    }//GEN-LAST:event_lamp1_sliderStateChanged

    private void start_pause_btnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_start_pause_btnActionPerformed
        if(isRunning){
            isRunning=false;
            //sunriseSpinner.setEnabled(true);
            //sunsetSpinner.setEnabled(true);
            start_pause_btn.setText("Resume");
        } else {
            isRunning=true;
            sunriseSpinner.setEnabled(false);
            sunsetSpinner.setEnabled(false);
            start_pause_btn.setText("Pause");
            if (console == null){
                this.getWidth();
                console = new Console(this.getWidth());
            }
            console.setVisible(true);
        }
    }//GEN-LAST:event_start_pause_btnActionPerformed

    private void reset_btnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_reset_btnActionPerformed
        time.setTime(0);
        showTime();
        sunriseSpinner.setEnabled(true);
        sunsetSpinner.setEnabled(true);
        if (console!=null){
            console.clearConsole();
        }
    }//GEN-LAST:event_reset_btnActionPerformed

    private void connect_arduino_btnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_connect_arduino_btnActionPerformed
        String port= cb_COM.getSelectedItem().toString();
        try {
            if(!port.equals("FAKE")){
                serialPort = new SerialPort(cb_COM.getSelectedItem().toString());
                //Open port
                serialPort.openPort();
                //We expose the settings. You can also use this line - serialPort.setParams(9600, 8, 1, 0);
                serialPort.setParams(SerialPort.BAUDRATE_57600, 
                                     SerialPort.DATABITS_8,
                                     SerialPort.STOPBITS_1,
                                     SerialPort.PARITY_NONE);

                int mask = SerialPort.MASK_RXCHAR + SerialPort.MASK_CTS + SerialPort.MASK_DSR;//Prepare mask
                serialPort.setEventsMask(mask);//Set mask
                serialPort.addEventListener(new SerialPortReader());//Add SerialPortEventListener
                status_lbl.setText("Connected to "+port);
            }else{
                new Thread(new FakeArduino(this)).start();
                status_lbl.setText("Connected to "+port);
            }
        }
        catch (SerialPortException ex) {
            status_lbl.setText("Error: Can't connect to port "+port);
        }
        connect_arduino_btn.setEnabled(false);
        connect2server_btn.setEnabled(true);        
        optimal_illumination_btn.setEnabled(true);
    }//GEN-LAST:event_connect_arduino_btnActionPerformed

    private void optimal_illumination_btnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_optimal_illumination_btnActionPerformed
        setOptimalIllumination(getPhoto());
    }//GEN-LAST:event_optimal_illumination_btnActionPerformed

    private void connect2server_btnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_connect2server_btnActionPerformed
        ucr= new URLConnectionReader();
        String params="config?client="+client_form.getText()+"&a_from_h="+fromTime.getHour()+"&a_from_m="+fromTime.getMin()
                +"&a_to_h="+toTime.getHour()+"&a_to_m="+toTime.getMin()+"&def_value="+getOptimalIllumination();
        String resp = ucr.sendGetRequest(address_from.getText(), port_form.getText(),params);
        if(resp.startsWith("OK")){
            status_lbl.setText("Connected to server");
            connect2server_btn.setEnabled(false);
            start_pause_btn.setEnabled(true);
            fromTimeSpinner.setEnabled(false);
            toTimeSpinner.setEnabled(false);
        }
        else {            
            status_lbl.setText(resp);    
        }
            
    }//GEN-LAST:event_connect2server_btnActionPerformed

    private void sunriseSpinnerStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_sunriseSpinnerStateChanged
        sunrise.setTime((int)(((Date)sunriseSpinner.getValue()).getTime()+3600000)/1000); //TODO: spinner trochę dziwnie działa, gdy cofa się o godzinę przed północ 23:30->0:30
        //System.out.println(sunrise.getHour()+":"+sunrise.getMin());
        //System.out.println(((Date)sunriseSpinner.getValue()).getTime()+3600000);
        
    }//GEN-LAST:event_sunriseSpinnerStateChanged

    private void sunsetSpinnerStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_sunsetSpinnerStateChanged
        sunset.setTime((int)(((Date)sunsetSpinner.getValue()).getTime()+3600000)/1000);
    }//GEN-LAST:event_sunsetSpinnerStateChanged

    private void toTimeSpinnerStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_toTimeSpinnerStateChanged
        toTime.setTime((int)(((Date)toTimeSpinner.getValue()).getTime()+3600000)/1000);
    }//GEN-LAST:event_toTimeSpinnerStateChanged

    private void fromTimeSpinnerStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_fromTimeSpinnerStateChanged
        fromTime.setTime((int)(((Date)fromTimeSpinner.getValue()).getTime()+3600000)/1000);
    }//GEN-LAST:event_fromTimeSpinnerStateChanged

    /**
     * 
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(IntelligentRoomClient.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(IntelligentRoomClient.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(IntelligentRoomClient.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(IntelligentRoomClient.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {                
                IntelligentRoomClient window= new IntelligentRoomClient();
                window.setVisible(true);
            }
        });
        
        
    }
    private Console console;
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField address_from;
    private javax.swing.JComboBox cb_COM;
    private javax.swing.JTextField client_form;
    private javax.swing.JButton connect2server_btn;
    private javax.swing.JButton connect_arduino_btn;
    private javax.swing.JLabel day_lbl;
    private javax.swing.JLabel day_lbl_const;
    private javax.swing.JSpinner fromTimeSpinner;
    private javax.swing.JLabel hour_lbl;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JTextField jTextField2;
    private javax.swing.JTextField lamp1_form;
    private javax.swing.JSlider lamp1_slider;
    private javax.swing.JLabel min_lbl;
    private javax.swing.JButton optimal_illumination_btn;
    private javax.swing.JLabel optimal_illumination_lbl;
    private javax.swing.JLabel photo_lbl;
    private javax.swing.JLabel photo_lbl1;
    private javax.swing.JTextField port_form;
    private javax.swing.JButton reset_btn;
    private javax.swing.JButton set_lamp1_btn;
    private javax.swing.JSpinner simulationStepSpinner;
    private javax.swing.JButton start_pause_btn;
    private javax.swing.JLabel status_lbl;
    private javax.swing.JSpinner sunriseSpinner;
    private javax.swing.JSpinner sunsetSpinner;
    private javax.swing.JLabel temp_lbl;
    private javax.swing.JLabel temp_lbl2;
    private javax.swing.JSpinner toTimeSpinner;
    // End of variables declaration//GEN-END:variables
}
