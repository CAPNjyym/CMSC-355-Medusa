   import edu.vcu.cs.cmsc355.*;
   import java.net.*;
   import java.io.*;

    public class TrainClient extends Thread
   {
      private int spd = 0, dir = 0; // speed 0-3, direction 0-1
      private int trackNum = -1, sensorCount = -1;
      private double currentLocation = -1;
      private boolean stop = false, tripping, disconnect = false, controlled = false, serverClosing = false;
      private int pointX, pointY;
   
      private Socket clientSocket;
      private PrintStream serverOut;
      private BufferedReader serverIn;
   
      private TrackSpeedManager speedManager;
      private TrackSensorManager trackManager;  
   
      public static final int TRACK_A = 0, TRACK_B = 1, TRACK_C = 2;
      public static final int SERVER_PORT = 5550;
      private static final byte TRAIN_CONNECTED = 0, TRAIN_DISCONNECTED = 1;
   
       public TrainClient(InetAddress serverIP, int track)
      {
         trackNum = track;
      
         initNet(serverIP);
         initTrain();
      }
   
       private void initNet(InetAddress serverIP)
      {
         try
         {
            clientSocket = new Socket(serverIP, SERVER_PORT);
            serverOut = new PrintStream(clientSocket.getOutputStream());
            serverIn = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            send(TrainServer.CONNECT+":"+trackNum);
         }
         
             catch (Exception e)
            {System.exit(0);}
      }
   
       private void initTrain()
      {
         try
         {
            speedManager = new TrackSpeedManager();
            if (trackNum == TRACK_A)
               sensorCount = 7;
            else if (trackNum == TRACK_B)
               sensorCount = 6;
            else if (trackNum == TRACK_C)
               sensorCount = 7;
         
            trackManager = new TrackSensorManager(sensorCount);
         }
         
             catch (Exception e)
            {e.printStackTrace();}
      }
   
       public void run()
      {
         String tempInput = new String();
         while(true)
         {
            try
            {
               if ((tempInput=serverIn.readLine())!=null)
               {
                  switch (Byte.parseByte(tempInput.split(":")[0]))
                  {
                     case TrainServer.CHANGE_SPEED:
                        System.out.println(tempInput.split(":")[1]);
                        changeSpeed(Integer.parseInt(tempInput.split(":")[1]));
                        break;
                     case TrainServer.E_STOP:
                        setStop(Boolean.parseBoolean(tempInput.split(":")[1]));
                        break;
                     case TrainServer.CONTROL:
                        setControl(Boolean.parseBoolean(tempInput.split(":")[1]));
                        break;
                     case TrainServer.SERVER_CLOSE:
                        serverClosing = true;
                        break;
                  }
               }
            }
            
                catch(SocketException er)
               {this.stop();}
                catch(NullPointerException e)
               {}
                catch(Exception e)
               {e.printStackTrace();}
         }
      }
   
       public double trainLocation()
      {
         if (currentLocation <= -1)
            findTrain();
         
         else if (currentLocation < 0)
            currentLocation += sensorCount;
         
         else if ((int)(currentLocation) != (int)(currentLocation + .5)) // its not at a sensor
         {
            tripping = false;
            if (!trackManager.isThisSensorClear((int) (currentLocation-.5)))
               currentLocation -= .5;
            else if (!trackManager.isThisSensorClear((int) (currentLocation+.5)))
               currentLocation += .5;
         
            if ((int)(currentLocation + .5) == sensorCount) // in between first and last sensors
               if (!trackManager.isThisSensorClear(0))
                  currentLocation = 0.0;
         } 
         
         else // its at a sensor
         {
            if (!tripping)
            {
               sendSensor((int)currentLocation);
               tripping = true;
            }
            if (trackManager.isThisSensorClear((int) currentLocation))
            {
               if ((dir == 0 && (trackNum == TRACK_A || trackNum == TRACK_B)) || (dir == 1 && trackNum == TRACK_C))
                  currentLocation -= .5;
               else if ((dir == 1 && (trackNum == TRACK_A || trackNum == TRACK_B)) || (dir == 0 && trackNum == TRACK_C))
                  currentLocation += .5;
            }
         }
      
         return currentLocation;
      }
   
       private void findTrain()
      {
      // figures out where train is
         for(int i=0; i<sensorCount; i++)
            if (!trackManager.isThisSensorClear(i))
            {
               sendSensor((int)(currentLocation = i));
               break;
            }
      
         speedManager.setTrackSpeed(spd + 4*dir);
      }
   
       public void sendSensor(int sensor)
      {
         // location SHOULD BE #.0, not #.5
         send(TrainServer.SENSOR_TRIPPED + ":" + trackNum + ":" + sensor);
      }
   
       public void sendInfo()
      {
         send(TrainServer.UPDATE + ":" + trackNum + ":" + currentLocation + ":" + (spd + 4*dir) + ":" + stop + ":"+ pointX+":"+pointY);
      }
   
       public void send(String message)
      {
         if (!disconnect)
            serverOut.println(message);
      }
   
       public void close()
      {
         try
         {
            serverOut.close();
            serverIn.close();
            clientSocket.close();
         }
         
             catch(Exception e){}
      }
   
       public static InetAddress ipFromString(String ip) throws Exception
      {
         if(ip.equals("localhost"))
            return InetAddress.getLocalHost();  
         int t;
         byte[] x = new byte[4];
      	
         x[0]=new Integer(ip.substring(0,t=ip.indexOf("."))).byteValue();
         x[1]=new Integer(ip.substring(++t,t=ip.indexOf(".",t))).byteValue();
         x[2]=new Integer(ip.substring(++t,t=ip.indexOf(".",t))).byteValue();
         x[3]=new Integer(ip.substring(++t)).byteValue();
      	
         return java.net.InetAddress.getByAddress(x);
      }
   
       public void halt()
      {
         stop = !stop;
         if (stop)
            speedManager.setTrackSpeed(0 + 4*dir);
         else
            speedManager.setTrackSpeed(spd + 4*dir);  
      }
   	
       public void setStop(boolean newstop)
      {
         stop = newstop;
         if (stop)
            speedManager.setTrackSpeed(0 + 4*dir);
         else
            speedManager.setTrackSpeed(spd + 4*dir);  
      }
   
       public boolean serverShutDown()
      {
         return serverClosing;
      }
   
       public void exit()
      {
      	// put train outside of an intersection
         while (currentLocation != 4.5)
         {}	//run();
         spd = 0;
         dir = 0;
         speedManager.setTrackSpeed(spd + 4*dir);
      	
         send(TrainServer.DISCONNECT + ":" + trackNum);
         disconnect = true;
      
         close();
         System.exit(0);
      }
   		
       public boolean controlled()
      {
         return controlled;
      }
   	
       public void setControl(boolean con)
      {
         controlled = con;
      }
   
       public void setTrainDisplayPoint(int x, int y)
      {
         pointX= x;
         pointY = y;
      }
   
       public void increaseSpeed()
      {
         spd++;
         if (spd > 3)
            spd = 3;
      
         changeSpeed(spd + 4*dir);
      }
   
       public void decreaseSpeed()
      {
         spd--;
         if (spd < 0)
            spd = 0;
      
         changeSpeed(spd + 4*dir);
      }
   
       public void changeSpeed(int newspeed)
      {
         spd = newspeed % 4;
         dir = newspeed / 4;
         if (!stop)
            speedManager.setTrackSpeed(newspeed);
      }
   
       public void changeDirection(int direction)
      {
         if (direction == -1)
            dir =  1 - dir;
         else
            dir = direction;
      
         changeSpeed(spd + 4*dir);
      }
   
       public int speed()
      {
         return spd;
      }
   
       public int direction()
      {
         return dir;
      }
   
       public boolean stopped()
      {
         return stop;
      }
   
       public int trackNum()
      {
         return trackNum;
      }
   }
