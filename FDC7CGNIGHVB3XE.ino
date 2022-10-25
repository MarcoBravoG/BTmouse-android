
#include<SoftwareSerial.h>

SoftwareSerial m = SoftwareSerial(9,8);

void setup() {
  //pinMode(ledpin, OUTPUT);  
  //Serial.begin(57600);
  m.begin(57600);   
    // start serial communication at 9600bps 57600
}
void loop() {
  if( m.available() > 0 )       // if data is available to read
  {
    moveMouse(m.read());
  }

}


void moveMouse(unsigned char c){
  unsigned char scale = 5;
  switch (c){
    case 'w': //up
    Mouse.move(0,scale*-1);
    break;
    case 's': //down
    Mouse.move(0,scale);
    break;
    case 'd': //right
    Mouse.move(scale,0);
    break;
    case 'a': //left
    Mouse.move(scale*-1,0);
    break;
  
    case 't': //TopRightToBottomLeft
    Mouse.move(scale*-1,scale*-1);
    break;
    case 'y':  //TopLeftToBottomRight
    Mouse.move(scale,scale*-1);
    break;
    case 'g':  //BottomLeftToTopRight
    Mouse.move(scale,scale);
    break;
    case 'h':  //BottomRightToTopLeft
    Mouse.move(scale*-1,scale);
    break;
  
    case 'q':
    Mouse.click(MOUSE_LEFT);
    break;
    case 'e':
    Mouse.click(MOUSE_RIGHT);    
  }
}
