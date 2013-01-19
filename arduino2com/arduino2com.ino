int photoPin = 0; // photoresistor analog pin
int tempPin = 5;  // temperature analog pin
int lamp0Pin= 3; //lamp 1 PMW pin
int lamp1Pin= 5; //lamp 1 PMW pin

int lamp0=0;
int lamp1=0;

int photoVal = 0; // photoresistor voltage
float tempVal;    // temperature value

int delayTime = 200; // dalay in ms

void setup()
{
  pinMode(photoPin, INPUT); // set pin as input
  pinMode(lamp0Pin, OUTPUT);
  pinMode(lamp1Pin, OUTPUT);
  analogWrite(lamp1Pin, 0);
  
  Serial.begin(57600); // set COM port
}

void loop()
{
  //if (Serial.available()){
    photoVal = analogRead(photoPin); //read photoresistor voltage                      
    Serial.print(photoVal);
    Serial.print(";");
    
    tempVal= analogRead(tempPin)*5/1024.0;
    tempVal= (tempVal - 0.5) / 0.01;                     
    Serial.println(tempVal); 
    
    while(Serial.available()>0){
      lamp0 = Serial.parseInt();
      lamp1 = Serial.parseInt();
      //if(Serial.read()==10){
      if (Serial.read() == '\n') {
        analogWrite(lamp0Pin, constrain(lamp0, 0, 255));
        analogWrite(lamp1Pin, constrain(lamp1, 0, 255));
      }
      //}
    } 
  //}  
  delay(delayTime);
}
