/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package intelligentroomclient;

import java.awt.event.ActionEvent;
import java.util.logging.Level;
import java.util.logging.Logger;
import jssc.SerialPort;
import jssc.SerialPortEvent;
import jssc.SerialPortEventListener;
import jssc.SerialPortException;

/**
 *
 * @author tsm
 */
public class IntelligentRoomClient extends javax.swing.JFrame {
    
    SerialPort serialPort;
    
    int photo=0;
    double temp=0.0;
    
    boolean isRunning=false;
    
    SimulationTime time; //in sec
    
    int lamp1=0;
    
    SimulationTime sunrise=new SimulationTime(6,0);
    SimulationTime sunset=new SimulationTime(16,0);
    
    public void stepSimulation(int photo, double temp){
        setPhoto(photo);
        setTemp(temp);

        if(isRunning && console!=null){                                

            //RAPORT:
            if(time.getHour()==0&&time.getMin()==0){
                console.writeMsg("==== Day "+time.getDay()+" ====");
            }
            if(time.getHour()==sunrise.getHour()&&time.getMin()==sunrise.getMin()){
                console.writeMsg("==== Sun rised ====");
            }
            if(time.getHour()==sunset.getHour()&&time.getMin()==sunset.getMin()){
                console.writeMsg("==== Sunset ====");
            }
            console.writeMsg(String.format("%2d",time.getHour())+":"+
                    String.format("%02d",time.getMin())+" :: Photoresistor = "+photo+", lamp= "+lamp1+", temp= "+temp+" °C"); 


            time.addSecs(15*60); // TIME incrementation
            showTime();
        }
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
        
        try {
            int half_sun_time=(sunset.getTime()-sunrise.getTime())/2;
            serialPort.writeString((255-(Math.abs(time.getTime()%SimulationTime.DAY_SECS - sunrise.getTime() - half_sun_time)/(half_sun_time/255)))+","+lamp1+"\n"); // MAGIC LINE xD
        } catch (SerialPortException ex) {
            Logger.getLogger(IntelligentRoomClient.class.getName()).log(Level.SEVERE, null, ex);
        }
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
                        System.out.println(ex);
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
    
    /**
     * Creates new form IntelligentRoomClient
     */
    public IntelligentRoomClient() {
        time=new SimulationTime(0);
        initComponents();
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
        jPanel2 = new javax.swing.JPanel();
        SetLamp1 = new javax.swing.JButton();
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
                        .addComponent(temp_lbl))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(photo_lbl1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(photo_lbl)
                        .addGap(66, 66, 66)
                        .addComponent(optimal_illumination_btn, javax.swing.GroupLayout.PREFERRED_SIZE, 173, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap(19, Short.MAX_VALUE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(photo_lbl1)
                    .addComponent(photo_lbl)
                    .addComponent(optimal_illumination_btn, javax.swing.GroupLayout.DEFAULT_SIZE, 28, Short.MAX_VALUE))
                .addGap(10, 10, 10)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(temp_lbl2)
                    .addComponent(temp_lbl))
                .addContainerGap())
        );

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder("Manual set"));

        SetLamp1.setText("SetLamp");
        SetLamp1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                SetLamp1MousePressed(evt);
            }
        });
        SetLamp1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                SetLamp1ActionPerformed(evt);
            }
        });
        SetLamp1.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                SetLamp1KeyPressed(evt);
            }
        });

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
                .addComponent(SetLamp1)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(lamp1_slider, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(SetLamp1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
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

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                .addComponent(start_pause_btn, javax.swing.GroupLayout.PREFERRED_SIZE, 89, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(reset_btn)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(day_lbl_const, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(day_lbl, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(58, 58, 58)
                .addComponent(hour_lbl)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(min_lbl)
                .addGap(51, 51, 51))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                .addContainerGap(76, Short.MAX_VALUE)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(hour_lbl)
                    .addComponent(jLabel3)
                    .addComponent(min_lbl)
                    .addComponent(day_lbl)
                    .addComponent(day_lbl_const)
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
                .addGap(27, 27, 27)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(status_lbl)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void SetLamp1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_SetLamp1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_SetLamp1ActionPerformed

    private void lamp1_formActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_lamp1_formActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_lamp1_formActionPerformed

    private void SetLamp1KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_SetLamp1KeyPressed
        
    }//GEN-LAST:event_SetLamp1KeyPressed

    private void SetLamp1MousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_SetLamp1MousePressed
        setLamp1(Byte.valueOf(lamp1_form.getText()));
        
    }//GEN-LAST:event_SetLamp1MousePressed

    private void lamp1_sliderStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_lamp1_sliderStateChanged
        setLamp1(lamp1_slider.getValue());
        
    }//GEN-LAST:event_lamp1_sliderStateChanged

    private void start_pause_btnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_start_pause_btnActionPerformed
        if(isRunning){
            isRunning=false;
            start_pause_btn.setText("Resume");
        } else {
            isRunning=true;
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
        start_pause_btn.setEnabled(true);
    }//GEN-LAST:event_connect_arduino_btnActionPerformed

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
    private javax.swing.JButton SetLamp1;
    private javax.swing.JTextField address_from;
    private javax.swing.JComboBox cb_COM;
    private javax.swing.JTextField client_form;
    private javax.swing.JButton connect2server_btn;
    private javax.swing.JButton connect_arduino_btn;
    private javax.swing.JLabel day_lbl;
    private javax.swing.JLabel day_lbl_const;
    private javax.swing.JLabel hour_lbl;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JTextField jTextField2;
    private javax.swing.JTextField lamp1_form;
    private javax.swing.JSlider lamp1_slider;
    private javax.swing.JLabel min_lbl;
    private javax.swing.JButton optimal_illumination_btn;
    private javax.swing.JLabel photo_lbl;
    private javax.swing.JLabel photo_lbl1;
    private javax.swing.JTextField port_form;
    private javax.swing.JButton reset_btn;
    private javax.swing.JButton start_pause_btn;
    private javax.swing.JLabel status_lbl;
    private javax.swing.JLabel temp_lbl;
    private javax.swing.JLabel temp_lbl2;
    // End of variables declaration//GEN-END:variables
}
