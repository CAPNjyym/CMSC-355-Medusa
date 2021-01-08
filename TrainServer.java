   import java.io.*;
   import java.net.*;
   import java.util.ArrayList;
   import java.awt.Point;

    public class TrainServer extends Thread
   {
      private ServerSocket socket;
      private Thread connector;
      private ArrayList<ReceiveThread> receiveThreads;
      private int phase;
      private boolean waitState, running;
      private static final int TIMEOUT = 50;
   	
      public String mapA, mapB, mapC;
   
      public static final byte 	UPDATE = 0,
      									SERVER_CONTROL = 1,
      									CHANGE_SPEED = 2,
      									DISCONNECT = 3,
      									SENSOR_TRIPPED = 4,
      									CONNECT = 5,
      									E_STOP = 6,
      									CONTROL = 7, 
      							SERVER_CLOSE = 8;
   										
       public TrainServer(String name)
      {
         super(name);
         mapA = null;
         mapB = null;
         mapC = null;
         waitState = false;
         phase = 0;
      
         initNet();
      
         connector =
             new Thread()
            {
                public void run()
               {
                  while(true)
                  {
                     try
                     {
                        Socket tempSocket = socket.accept();
                        BufferedReader tempReader = new BufferedReader(new InputStreamReader(tempSocket.getInputStream()));
                        PrintStream tempOut = new PrintStream(tempSocket.getOutputStream());
                        receiveThreads.add(new ReceiveThread(receiveThreads.size(),tempSocket,tempReader,tempOut));
                        receiveThreads.get(receiveThreads.size()-1).start();
                     }
                     
                         catch(Exception e)
                        {e.printStackTrace();}
                  }
               }
            };
      
         connector.start();
      
      }
   
       private void initNet()
      {
         try
         {
            receiveThreads = new ArrayList<ReceiveThread>();
            if (socket==null)
               socket = new ServerSocket(TrainClient.SERVER_PORT);
            running = true;
         
            System.out.println(java.net.InetAddress.getLocalHost());
         }
         
             catch(Exception e)
            {e.printStackTrace();}
      }
   
       public void run()
      {
      
         while (true)
         {
         /*				aTime--;
         System.out.println("runfgh increment" + aTime);
         	if(aTime<0)
         	{
         		aTime=aConstantTime;
         		if(!changedSpeed)
         		{
         			sendTo(TrainClient.TRACK_A, CHANGE_SPEED+":"+1);
         			changedSpeed=true;
         		}
         	}
         */
         // *******TIMEOUT LOGIC**********
         
         
         ////TRAIN A
            ReceiveThread tempThread = getReceiveThreadByTrack(0); //so we don't go through a loop a bajillion times
            if(tempThread!=null)
            {
               if((newSpeeds[0] == 0 || newSpeeds[0] == 4) && speeds[0] == newSpeeds[0])   //if speed is still zero
               {
                  if(tempThread.timeOut())	//is timer expired yet?
                  {
                     if(newSpeeds[0] == 0)
                        newSpeeds[0] = 5;
                     else
                        newSpeeds[0] = 1;
                     sendTo(TrainClient.TRACK_A, CHANGE_SPEED+":"+newSpeeds[0]);
                     speeds[0]=newSpeeds[0];
                  
                     tempThread.resetTimer();
                  }
                  else		//...timer is not expired yet!
                     tempThread.incrementTimer();
               }
               else
                  tempThread.resetTimer();
            }
         ///TRAIN B
            tempThread = getReceiveThreadByTrack(1);
         
            if(tempThread!=null)
            {
               if((newSpeeds[1] == 0 || newSpeeds[1] == 4) && speeds[1] == newSpeeds[1])   //if speed is still zero
               {
               
                  if(tempThread.timeOut())	//is timer expired yet?
                  {
                     if(newSpeeds[1] == 0)
                        newSpeeds[1] = 5;
                     else
                        newSpeeds[1] = 1;
                     sendTo(TrainClient.TRACK_B, CHANGE_SPEED+":"+newSpeeds[1]);
                     speeds[1]=newSpeeds[1];
                     tempThread.resetTimer();
                  }
                  else		//...timer is not expired yet!
                     tempThread.incrementTimer();
               }
               else
                  tempThread.resetTimer();
            }
         	///TRAIN C
            tempThread = getReceiveThreadByTrack(2);
            if(tempThread!=null)
            {
               if((newSpeeds[2] == 0 || newSpeeds[2] == 4) && speeds[2] == newSpeeds[2])   //if speed is still zero
               {
                  if(tempThread.timeOut())	//is timer expired yet?
                  {
                     if(newSpeeds[2] == 0)
                        newSpeeds[2] = 5;
                     else
                        newSpeeds[2] = 1;
                     sendTo(TrainClient.TRACK_C, CHANGE_SPEED+":"+newSpeeds[2]);
                     speeds[2]=newSpeeds[2];
                     tempThread.resetTimer();
                  }
                  else		//...timer is not expired yet!
                     tempThread.incrementTimer();
               }
               else
                  tempThread.resetTimer();
            }
         //**********END TIMEOUT LOGIC******************
         
            try
            {this.sleep(100);}
                catch (Exception e){}
         }
      }
   
       public void setRunning(boolean b)
      {
         running = b;
      }
   
       public void exit()
      {
         sendToAll(""+SERVER_CLOSE);
         while (!(mapA == null && mapB ==null && mapC == null))
         {}
      }
       public void close()
      {
         if (connector != null)
         {
            connector.stop();
            connector = null;
         }
      
         for (int t=0; t<receiveThreads.size(); t++)
         {
            try
            {
               receiveThreads.get(t).close();
               receiveThreads.get(t).stop();
            }
            
                catch (java.lang.IndexOutOfBoundsException e){};
         }
      }
   
       public ReceiveThread getReceiveThreadByTrack(int track)
      {
         for(int t=0; t<receiveThreads.size(); t++)
         {
            if(receiveThreads.get(t).getIndex()== track)
               return receiveThreads.get(t);
         }
         return null;
      }   
   
       public void setTrainSpeed(int track, int deltaSpeed)
      {
         for(int t=0;t<receiveThreads.size();t++)
            if(receiveThreads.get(t).getIndex() == track)
               receiveThreads.get(t).getOut().println(CHANGE_SPEED+":"+deltaSpeed);
      }
   	
       public void setTrainStop(int track, boolean stop)
      {
         for(int t=0;t<receiveThreads.size();t++)
            if(receiveThreads.get(t).getIndex() == track)
               receiveThreads.get(t).getOut().println(E_STOP+":"+stop);
      }
   	
       public void setTrainControl(int track, boolean con)
      {
         System.out.println("sending");
         for(int t=0;t<receiveThreads.size();t++)
            if(receiveThreads.get(t).getIndex() == track)
               receiveThreads.get(t).getOut().println(CONTROL+":"+con);
      }
   
       public String[] getAttached()
      {
         String[] temp = new String[receiveThreads.size()];
         for(int t=0; t<receiveThreads.size(); t++)
         {
         // temp[t]=receiveThreads.get(t).getSocket().getInetAddress().toString().split("/")[1];
            temp[t] = receiveThreads.get(t).getPlayerName();
            while(temp[t].equals(" "))
               temp[t] = receiveThreads.get(t).getPlayerName();
         // System.out.println(temp[t]);
         }
         return temp;
      }
   
       public void sendTo(int track, String message)
      {
         for(int t=0; t<receiveThreads.size(); t++)
         {
            if(receiveThreads.get(t).getIndex()== track)
            {
               receiveThreads.get(t).getOut().println(message);
               break;
            }
         }
      }
   
       public void sendToAll(String message)
      {
         for(int t=0; t<receiveThreads.size(); t++)
         {
            receiveThreads.get(t).getOut().println(message);
         }
      }
   
       private void updateMap(int trainNum, double location, int speed, int direction, boolean stopped)
      {
         if (trainNum == 0) // mapA
         {
            mapA = location + ":" + speed + ":" + direction + ":" + stopped;
         }
         else if (trainNum == 1) // mapB
         {
            mapB = location + ":" + speed + ":" + direction + ":" + stopped;
         }
         else if (trainNum == 2) // mapC
         {
            mapC = location + ":" + speed + ":" + direction + ":" + stopped;
         }
      }
   
       private void disconnect(int trainNum)
      {
         if (trainNum == 0) // mapA
            mapA = null;
         else if (trainNum == 1) // mapB
            mapB = null;
         else if (trainNum == 2) // mapC
            mapC = null;
      }  
   
       public String getMap(int trainNum)
      {
         if (trainNum == 0) // mapA
            return mapA;
         else if (trainNum == 1) // mapB
            return mapB;
         else if (trainNum == 2) // mapC
            return mapC;
         return null;
      }
   
       public void changeControl(int track)
      {
         System.out.println("telling");
         control[track] = !control[track];
         setTrainControl(track, control[track]);
      }
   	
       public boolean getControl(int track)
      {
         return control[track];
      }
      
       public Point[] getDisplayPoints()
      {
         Point[] temp = new Point[3];
         for(int t=0; t<receiveThreads.size(); t++)
            temp[receiveThreads.get(t).getIndex()] = receiveThreads.get(t).getDisplayPoint();
         return temp;
      }
   	
       public void updateSpeed(int trackNum, int speed)
      {
         speeds[trackNum] = speed;
      }
      
       public int[] getSpeeds()
      {
         return speeds;
      }
   
    //$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$
         //TRAIN SPEED ADJUSTMENT METHODS (If-statement Hell)
         //For all arrays, TrainClient.TRACK_A, TrainClient.TRACK_B, TrainClient.TRACK_C
   
      int[] lastHit = new int[3]; //Keeps track of the last hit sensor for each train.
      int[] sensors = new int[3]; //array of the last sensors that were hit for each track; 0-a, 1-b, 2-c
      int[] speeds	= new int[3];   //array of the current speeds of each track
      boolean[] hit = new boolean[3];	//array of the track that activated the sensorControls method. Only one value is true at  time
      boolean[] control = new boolean[3];// array of each track, true if the server has taken control of that track
      int[] newSpeeds = new int[3];
   //		boolean aSlowTimer = false, changedSpeed=false;
   //		int aConstantTime = 18;
   //		int aTime = aConstantTime;
   
   		//hits is either 0 for A, 1 for B, or 2 for C
       public void sensorControls(int hits, int sen)
      {
      
         sensors[hits] = sen;
      
      	  //int[] sensors = getSensors();
      	  //int[] speeds = getSpeeds();
      	  //boolean[] control = getControl();
      	  //If the value in control is true, the train is under manual control. If it is false, the train is under automatic control.
      
      		//setting last hit
         for (int x=0; x<=2; x++)
         {
            if (sensors[x] != -1)
            {
               lastHit[x] = sensors[x];
            }
         }
      
      	  // setting hit
         hit[hits] = true;
         for(int a=1; a<3; a++)
            hit[(hits+a)%3] =false;
      
      
         
         newSpeeds[0] = speeds[0];
         newSpeeds[1] = speeds[1];
         newSpeeds[2] = speeds[2];
      
      	  
      
        /* System.out.println("sensors - speeds - control - hit - lastHit");
         for(int a=0; a<3; a++)
         {
            System.out.print(sensors[a] + " - ");
            System.out.print(speeds[a] + " - ");
            System.out.print(control[a] + " - ");
            System.out.print(hit[a] + " - ");
            System.out.println(lastHit[a]);
         }
      */
      
      
      //Ensures that C does not run off the track.
         if (!control[2])
         {
            if(sensors[2] == 6 && speeds[2]<4)
               newSpeeds[2] = 7;
            else if(sensors[2] == 0 && speeds[2]>4)
               newSpeeds[2] = 1;
            else if((sensors[2] <=3 && speeds[2]<4) || (sensors[2]<=5 && speeds[2]>4))
               newSpeeds[2] = ( (speeds[2]<4) ? (1) : (5) );
            else if(sensors[2]==4 && speeds[2]<4)
               newSpeeds[2]=3;
            else if(sensors[2]==5 && speeds[2]<4)
               newSpeeds[2]=1;
         }
      
         if(!control[1])
         {
         //slow B down!!
            if(sensors[1]==5 && speeds[1]<4)
               newSpeeds[1]=2;
            else if(sensors[1]==2 && speeds[1]>4)
               newSpeeds[1]=6;
            else if(sensors[1]<=1 && speeds[1]<4)
               newSpeeds[1]=1;
            else if(sensors[1]==4 && speeds[1]>4)
               newSpeeds[1]=5;
            //speed B up!!
            else if(sensors[1]==3 && speeds[1]<4)
               newSpeeds[1]=3;
            else if(sensors[1]==0 && speeds[1]>4)
               newSpeeds[1]=7;
         }
         
         if(!control[0])
         {
          //slow A down!!!
            if(sensors[0]==1)
               newSpeeds[0]= ( (speeds[0]<4) ? (1) : (5) );
            if(sensors[0]==5 && speeds[0]>4)
               newSpeeds[0]=5;
          //speed A up!!
            if(sensors[0]==6 && speeds[0]<4)
               newSpeeds[0]=3;
            if(sensors[0]==0 && speeds[0]>4)
               newSpeeds[0]=7;
         /*				
         	
         	if(sensors[0]==1)
         		changedSpeed=false;
         	if(sensors[0]==1 && speeds[0]<4 && !changedSpeed)
         		aSlowTimer = true;
         	if(sensors[0]==0 && speeds[0]<4)
         		aSlowTimer = false;
         */
         }
      
      	  //AC intersection
         if(!control[0] && !control[2])
         {
            if ( (sensors[0]==0 && speeds[0]<4) || (sensors[0]==6 && speeds[0]>4) )
            {
               newSpeeds[0] = ( (speeds[0]<4) ? (0) : (4) );
               if (sensors[2] > 2)
               {
                  newSpeeds[0] = ( (speeds[0]<4) ? (3) : (7) );
               }
            }
            //if ( (lastHit[2]==0 || lastHit[2]==1 || (lastHit[2]==2 && speeds[2]>4)) && ((sensors[0]==0 && speeds[0]<4) || (sensors[0]==6 && speeds[0]>4)))
            //   newSpeeds[0] = ( (speeds[0]<4) ? (0) : (4) );
            	
            else if ( sensors[2]==2 && speeds[2]<4 && ((sensors[0]==0 || sensors[0]==6) && (speeds[0]==0 || speeds[0]==4) ))
               newSpeeds[0] = ( (speeds[0]==0) ? (3) : (7) );
            /* OLD STUFF
            //if A is moving towards the AC intersection, and C is also moving towards or is in the AC intersection and A hits, then A stops
            if( (sensors[2]==0 || sensors[2]==1 || sensors[2]==2) && speeds[2]>4 && ((sensors[0]==0 && speeds[0]<4) || (sensors[0]==6 && speeds[0]>4)))
               newSpeeds[0] = ( (speeds[0]<4) ? (0) : (4) );
            
            //If C hits c2 and is moving away from the AC intersections, and A is stopped at a6 or a0, then start it full speed
            else if( sensors[2]==2 && speeds[2]>4 && hit[2] & ((sensors[0]==0 || sensors[0]==6) && (speeds[0]==0 || speeds[0]==4)) )
               newSpeeds[0] = ( (speeds[0]==0) ? (3) : (7) );
            	
            */
            
            //if C2 and is moving towards AC intersection and A last hit A0 or A6 and is moving towards AC intersection, then stop C
            //else if(((sensors[2]==1 && speeds[2]<4) || (sensors[2]==2 && speeds[2]>4)) && ((lastHit[0]==0 && speeds[0]<4) || (lastHit[0]==6 && speeds[0]>4)) )
            //   newSpeeds[2] = ( (speeds[2]<4) ? (0) : (4) );
            
            //if A is moving away from the AC intersection and C is stopped at c2, move C
            else if( ((sensors[0]==6 && speeds[0]<4) || (sensors[0]==0 && speeds[0]>4)) && (sensors[2]==1 || sensors[2]==2) && (speeds[2]==0 || speeds[2]==4) )
               newSpeeds[2]= ( (speeds[2]==0) ? (1) : (5) );
         }
      
        	  //BC intersection
         if(!control[1] && !control[2])
         {
         	//if C and B are moving towards the BC intersection and B hits 5 or 0, then stop B
            if( ((sensors[2]==4 && speeds[2]>4) || (sensors[2]==3 && speeds[2]<4)) && ((sensors[1]==5 && speeds[1]>4) || (sensors[1]==0 && speeds[1]<4)) && hit[1] )
               newSpeeds[1] = ( (speeds[1]<4) ? (0) : (4) );
            
            //if C is moving away from the BC intersection and B is stopped at b0 or b5, then start B
            else if( ((sensors[2]==4 && speeds[2]<4) || (sensors[2]==3 && speeds[2]>4)) && hit[2] && ((sensors[1]==5 || sensors[1]==0) && (speeds[1]==0 || speeds[1]==4)) )
               newSpeeds[1] = ( (speeds[1]==0) ? (3) : (7) );
            
            //C STOPPING AT BC
            // else if( ((sensors[2]==4 && speeds[2]>4) || (sensors[2]==3 && speeds[2]<4)) && ((lastHit[1]==5 && speeds[1]>4) || (lastHit[1]==0 && speeds[1]<4 && speeds[1]!=0 && speeds[1]!=4)) )
            //    newSpeeds[2] = ( (speeds[2]<4) ? (0) : (4) );
            else if ( (sensors[2]==4 && speeds[2]>4) && ((lastHit[1]==0 && speeds[1]<4) || (lastHit[1]==5 && speeds[1]>4)))
               newSpeeds[2] = 4;
            	
            else if (sensors[2]==2 && speeds[2]<4)
            {
               newSpeeds[2] = 0;
               if ((sensors[1]==5 && speeds[1]<4) || (sensors[1]==0 && speeds[1]>4) || (lastHit[1]!=0 && lastHit[1]!=5))
               {
                  try
                  {
                     Thread.currentThread().sleep(700);
                  } 
                      catch (Exception e) {}
                  newSpeeds[2] = 1;
               }
            }
            
            //C STARTING AT BC
            else if( ((sensors[1]==5 && speeds[1]<4) || (sensors[1]==0 && speeds[1]>4)) && (((sensors[2]==4 && lastHit[2]==4)||(sensors[2]==3 && lastHit[2]==3))&& (speeds[2]==0 || speeds[2]==4)) )
               newSpeeds[2] = ( (speeds[2]==0) ? (1) : (5) );
         
         }
      
      	  //AB intersection
         if(!control[0] && !control[1])
         {
                     
            
            //if a and b are moving forward towards the first intersection and B hits, then B stops
            if(sensors[1]==4 && (sensors[0]==4 || sensors[0]==-1) && speeds[1]<4 && speeds[0]<4 && hit[1])
               newSpeeds[1]=0;
            //if a and b are moving forward towards the first intersection and A hits, then A stops
            else if((sensors[1]==4 || sensors[1]==-1) && sensors[0]==4 && speeds[1]<4 && speeds[0]<4 && hit[0])
               newSpeeds[0]=0;
            
            //if a (backward) and b (forward) are moving towards each other and a hits, then b stops
            else if(sensors[1]==4 && (sensors[0]==3 || sensors[0]==2) && (lastHit[0]==3 || lastHit[0]==2) && speeds[1]<4 && speeds[0]>=4 && hit[1])
               newSpeeds[1]=0;
            //if a (backward) and b (forward) are moving towards each other and a hits, then a stops
            else if(sensors[1]==4 && sensors[0]==2 && speeds[1]<4 && speeds[0]>=4 && (speeds[1]!=0 || speeds[1]!=4) && hit[0])
               newSpeeds[0]=4;
            
            //if a and b are moving reverse towards each other (2nd intersection) and A hits, then A stops
            else if(sensors[1]==3 && (sensors[0]==2 || sensors[0]==-1) && speeds[1]>4 && speeds[0]>=4 && hit[1])
               newSpeeds[1]=4;
            //if a and b are moving reverse towards each other (2nd intersection) and B hits, then B stops
            else if((sensors[1]==3 || sensors[1]==-1) && sensors[0]==2 && speeds[1]>4 && speeds[0]>=4 &&  hit[0])
               newSpeeds[0]=4;
            
            //if a (forward) and b (backwards) are moving towards each other and a hits, then B stops
            else if(sensors[1]==3 && (sensors[0]==3 || sensors[0]==4) && (lastHit[0]==3 || lastHit[0]==4) && speeds[1]>4 && speeds[0]<4 && hit[1])
               newSpeeds[1]=4;
            //if a (forward) and b (backwards) are moving towards each other and a hits, then a stops
            else if(sensors[1]==3 && sensors[0]==4 && speeds[1]>4 && speeds[0]<4 && (speeds[1]!=0 || speeds[1]!=4) && hit[0])
               newSpeeds[0]=0;
            
            //restart B
            else if( ((sensors[0]==2 && speeds[0]<4) || (sensors[0]==4 && speeds[0]>4))  && (sensors[1]==3 || sensors[1]==4) && (speeds[1]==4 || speeds[1]==0) )
               newSpeeds[1] = ( (speeds[1]==0) ? (3) : (7) );
            
            //restart A
            else if( ((sensors[1]==4 && speeds[1]>=4) || (sensors[1]==3 && speeds[1]<4)) && ((sensors[0]==2 && lastHit[0]==2) || (sensors[0]==3 && lastHit[0]==3) || (sensors[0]==4 && lastHit[0]==4) && (speeds[0]==0 || speeds[0]==4)) )
               newSpeeds[0] = ( (speeds[0]<4) ? (3) : (7) );
         }
      
      
         /*System.out.println("newSpeeds");
         for(int a=0; a<3; a++)
            System.out.print(newSpeeds[a] + " - ");
         System.out.println();
      */
      
         sendTo(TrainClient.TRACK_A, CHANGE_SPEED+":"+newSpeeds[0]);
         sendTo(TrainClient.TRACK_B, CHANGE_SPEED+":"+newSpeeds[1]);
         sendTo(TrainClient.TRACK_C, CHANGE_SPEED+":"+newSpeeds[2]);
         speeds[0]=newSpeeds[0];
         speeds[1]=newSpeeds[1];
         speeds[2]=newSpeeds[2];
      	  //A.speed = newSpeeds[0];
      	  //B.speed = newSpeeds[1];
      	  //C.speed = newSpeeds[2];
      }
    //?????????????????????
   
       class ReceiveThread extends Thread
      {
         private int index;
         public int counter;
         private boolean readyToPlay;
         private String playerName;
         private Socket socket;
         private BufferedReader in;
         private PrintStream out;
         private Point displayPoint;
      
          public ReceiveThread(int t, Socket s, BufferedReader i, PrintStream o)
         {
            super(t+"");
            playerName = new String(" ");
            displayPoint = new Point();
            readyToPlay = false;
            index=t;
            socket = s;
            in = i;
            out= o;
         }
      
      
          public boolean timeOut()
         {
            return (counter > TIMEOUT);
         }
      
          public void incrementTimer()
         {
            counter++;
         }
      
          public void resetTimer()
         {
            counter=0;
         }
      
          private void removeClient()
         {
            receiveThreads.remove(index);
            close();
         }
         
          public String getPlayerName()
         {
            return playerName;
         }
      
          public int getIndex()
         {
            return index;
         }
      
          public Point getDisplayPoint()
         {
            return displayPoint;
         }
      
          public void setDisplayPoint(int x, int y)
         {
            displayPoint.setLocation(x,y);
         }
      
          public void setIndex(int x)
         {
            index = x;
         }
          public boolean isReadyToPlay()
         {
            return readyToPlay;
         }
      
          public void setReadyToPlay(boolean b)
         {
            readyToPlay = b;
         }
      
          public PrintStream getOut()
         {
            return out;
         }
      
          public Socket getSocket()
         {
            return socket;
         }
      
          public void close()
         {
            try
            {
               out.close();
               in.close();
               socket.close();
            }
                catch(Exception e){}
         }
      
          public void run()
         {
            byte messageType = 0;
            String message = new String();
         
            while (true)
            {
               try
               {
                  if ((message = in.readLine()) != null)
                  {
                     messageType = Byte.parseByte(message.split(":")[0]);
                     //System.out.println(messageType);
                  
                     switch (messageType)
                     {
                        case UPDATE:
                           int trainNum = Integer.parseInt(message.split(":")[1]);
                           double location = Double.parseDouble(message.split(":")[2]);
                           int speed = Integer.parseInt(message.split(":")[3]) % 4, direction = Integer.parseInt(message.split(":")[3]) / 4;
                           boolean stopped = Boolean.parseBoolean(message.split(":")[4]);
                           int xer = Integer.parseInt(message.split(":")[5]), yer = Integer.parseInt(message.split(":")[6]);
                           if(trainNum==getIndex())
                              setDisplayPoint(xer,yer);
                           updateMap(trainNum, location, speed, direction, stopped);
                           updateSpeed(trainNum,speed+direction*4);
                           break;
                        case SENSOR_TRIPPED:
                           //System.out.println(message + "   speed:" + speeds[1]);
                           sensorControls(Integer.parseInt(message.split(":")[1]), Integer.parseInt(message.split(":")[2]));
                           break;
                        case CHANGE_SPEED:
                        //received code
                           break;
                        case DISCONNECT:
                           disconnect(Integer.parseInt(message.split(":")[1]));
                           break;
                        case CONNECT:
                           setIndex(Integer.parseInt(message.split(":")[1]));
                           break;
                      
                     }
                  }
               }
               
                   catch(SocketException e)
                  {removeClient();}
                   catch(Exception e)
                  {this.stop();}
            }
         }
      }
   }