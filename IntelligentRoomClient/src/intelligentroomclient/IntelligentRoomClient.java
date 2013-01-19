/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package intelligentroomclient;

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
    
    int time=0; //in sec
    int lamp1=0;
    
    int sunrise=21600;
    int sunset=64800;
    
    public int getPhoto() {
        return photo;
    }

    public void setPhoto(int photo) {
        this.photo = photo;
        photo_lbl.setText(String.valueOf(photo));
        if (photo < 300){
            setLamp1(getLamp1()+5);
        }
        if (photo > 350){
            setLamp1(getLamp1()-5);
        }
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
            int half_sun_time=(sunset-sunrise)/2;
            serialPort.writeString((255-(Math.abs(time%86400 - half_sun_time*2)/(half_sun_time/255)))+","+lamp1+"\n"); // MAGIC LINE xD
        } catch (SerialPortException ex) {
            Logger.getLogger(IntelligentRoomClient.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
        min_lbl.setText(String.format("%02d",time/60%60));
        hour_lbl.setText(String.format("%02d",time/60/60%24));
        day_lbl.setText(""+(time/86400));
        
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
                            setPhoto(Integer.valueOf(measures[0]));
                            setTemp(Double.valueOf(measures[1]));
                            setTime(time+15*60);
                            System.out.println("Photo = "+photo+", temp= "+temp);
                            //serialPort.writeString(lamp1+"");
                           // serialPort.writeByte((byte)10); //newline
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
        initComponents();
        serialPort = new SerialPort("COM3");
        try {
            //Open port
            serialPort.openPort();
            //We expose the settings. You can also use this line - serialPort.setParams(9600, 8, 1, 0);
            serialPort.setParams(SerialPort.BAUDRATE_57600, 
                                 SerialPort.DATABITS_8,
                                 SerialPort.STOPBITS_1,
                                 SerialPort.PARITY_NONE);
            
            //serialPort.writeString("100\n");
            
            int mask = SerialPort.MASK_RXCHAR + SerialPort.MASK_CTS + SerialPort.MASK_DSR;//Prepare mask
            serialPort.setEventsMask(mask);//Set mask
            serialPort.addEventListener(new SerialPortReader());//Add SerialPortEventListener
            
            //Read the data of 10 bytes. Be careful with the method readBytes(), if the number of bytes in the input buffer
            //is less than you need, then the method will wait for the right amount. Better to use it in conjunction with the
            //interface SerialPortEventListener.
            //byte[] buffer = serialPort.readBytes(10);
            
            //System.out.println(buffer);
            
            //Writes data to port
            //serialPort.writeBytes("255\n".getBytes());
            
            //while(true);
            //Closing the port
            //serialPort.closePort();
        }
        catch (SerialPortException ex) {
            System.out.println(ex);
        }
        setTime(0);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        lamp1_form = new javax.swing.JTextField();
        SetLamp1 = new javax.swing.JButton();
        temp_lbl2 = new javax.swing.JLabel();
        photo_lbl = new javax.swing.JLabel();
        photo_lbl1 = new javax.swing.JLabel();
        temp_lbl = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        lamp1_slider = new javax.swing.JSlider();
        hour_lbl = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        min_lbl = new javax.swing.JLabel();
        day_lbl = new javax.swing.JLabel();
        day_lbl_const = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        lamp1_form.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                lamp1_formActionPerformed(evt);
            }
        });

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

        temp_lbl2.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        temp_lbl2.setText("Temperature:");

        photo_lbl.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        photo_lbl.setText("N/A");

        photo_lbl1.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        photo_lbl1.setText("Photoresistor:");

        temp_lbl.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        temp_lbl.setText("N/A");

        jLabel1.setText("Lamp1");

        lamp1_slider.setMaximum(255);
        lamp1_slider.setValue(0);
        lamp1_slider.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                lamp1_sliderStateChanged(evt);
            }
        });

        hour_lbl.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        hour_lbl.setForeground(new java.awt.Color(0, 0, 102));
        hour_lbl.setText("--");

        jLabel3.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        jLabel3.setForeground(new java.awt.Color(0, 0, 102));
        jLabel3.setText(":");

        min_lbl.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        min_lbl.setForeground(new java.awt.Color(0, 0, 102));
        min_lbl.setText("--");

        day_lbl.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        day_lbl.setForeground(new java.awt.Color(0, 0, 102));
        day_lbl.setText("-");

        day_lbl_const.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        day_lbl_const.setForeground(new java.awt.Color(0, 0, 102));
        day_lbl_const.setText("Day");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(22, 22, 22)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(temp_lbl2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(temp_lbl))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(3, 3, 3)
                        .addComponent(jLabel1)
                        .addGap(18, 18, 18)
                        .addComponent(lamp1_form, javax.swing.GroupLayout.PREFERRED_SIZE, 59, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(SetLamp1)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(lamp1_slider, javax.swing.GroupLayout.PREFERRED_SIZE, 359, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(layout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addComponent(photo_lbl1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(photo_lbl)
                .addGap(18, 18, 18)
                .addComponent(day_lbl_const, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(day_lbl, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(hour_lbl)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(min_lbl)
                .addGap(24, 24, 24))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(22, 22, 22)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(photo_lbl1)
                    .addComponent(photo_lbl)
                    .addComponent(hour_lbl)
                    .addComponent(jLabel3)
                    .addComponent(min_lbl)
                    .addComponent(day_lbl)
                    .addComponent(day_lbl_const))
                .addGap(16, 16, 16)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(temp_lbl2)
                    .addComponent(temp_lbl))
                .addGap(28, 28, 28)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lamp1_form, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(SetLamp1)
                    .addComponent(jLabel1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lamp1_slider, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(138, Short.MAX_VALUE))
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

    /**
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
                new IntelligentRoomClient().setVisible(true);
            }
        });
        
        
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton SetLamp1;
    private javax.swing.JLabel day_lbl;
    private javax.swing.JLabel day_lbl_const;
    private javax.swing.JLabel hour_lbl;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JTextField lamp1_form;
    private javax.swing.JSlider lamp1_slider;
    private javax.swing.JLabel min_lbl;
    private javax.swing.JLabel photo_lbl;
    private javax.swing.JLabel photo_lbl1;
    private javax.swing.JLabel temp_lbl;
    private javax.swing.JLabel temp_lbl2;
    // End of variables declaration//GEN-END:variables
}
