import java.io.*;
import lejos.nxt.*;
import lejos.nxt.comm.*;

public class NXTController {
  public static void main(String [] args) throws Exception {
    String connected = "Connected";
    String waiting = "Waiting...";
    String closing = "Closing...";
    String out = "";
    int sleepDuration = 0;
    byte[] bytes = new byte[4];
    LCD.drawString(waiting,0,0);
    NXTConnection connection = Bluetooth.waitForConnection(); 
    LCD.clear();
    LCD.drawString(connected,0,0);
    Packet p;
    Message m;
    
    while (true) {


      
      DataInputStream dis = connection.openDataInputStream();
      DataOutputStream dos = connection.openDataOutputStream();
//      sleepDuration += 100;
//      Thread.sleep(sleepDuration);

//      do {
//    	  dis.read(bytes, 0, 4);
//      } while (dis.available() > 0);
      p = Packet.deserialize(dis);
      
      Message.NullMessage mess = new Message.NullMessage();
      Packet pack = new Packet(Packet.PacketType.STATETYPE, mess);
      
      if(p.msg.type == Message.MessageType.NULLMSG){
          LCD.clear();
          LCD.drawString(p.msg.toString(), 0, 0);
          dos.write(pack.seralize());
          dos.flush();
          break;
      } else {
          LCD.clear();
          LCD.drawString("NOTNULL", 0, 0);
      }
    	  
      


      //dos.write(bytes);
      
      //dos.flush();
      

      dis.close();
      dos.close();

    }
    Thread.sleep(10000);
  }
 }