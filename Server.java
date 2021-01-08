// A-Team Somethin Special
// GUI for train server

   import java.awt.event.*;
   import java.awt.*;
   import java.net.*;
   import java.io.*;  

    public class Server extends Frame implements Runnable, KeyListener, MouseListener, WindowListener
   {
   
      private final boolean canRunAnimations = false;				//False to disable animations
   
      private boolean instructions = false, exiting = false, easter = false, easterGates[];
      private int view = 0;
      private int animation = 0, xAnimation =0, yAnimation = 0, maskAnimation=0;
      private Color mask;
      private final Color TRAIN_A_COLOR = new Color(255,0,255);
      private final Color TRAIN_B_COLOR = new Color(0,255,255);
      private final Color TRAIN_C_COLOR = new Color(255,255,0);
      private Font GUIFont, instructionsFont, instructionsTitle;
      private int[] trainDistance;
      private long oldTimeAnimation, oldTime;
      private int[] speedsForDisplay;
      private Point[] displayPoint;
   
      private Graphics2D g, m;												//Used for double buffering in windowed mode
      private Image backbuffer, map, mapA, mapB, mapC, minimap, face, trainimage, trainimage2;	//Used for double buffering in windowed mode
   
      private Thread trainThread;
      private TrainServer server;
   
      public final static int HEIGHT = 768, WIDTH = 1024;
   
       public static void main(String[] args) throws Exception
      {
         InetAddress serverIP;
      
         new Server();
      }
   
       public Server()
      {
         easterGates = new boolean[8];
         for(int i=0; i<8; i++)
            easterGates[i] = false;
      	
         this.addKeyListener(this);
         this.addMouseListener(this);
         this.addWindowListener(this);
      
         initGraphics();
         trainDistance = new int[3];
         displayPoint = new Point[3];
         displayPoint[0] = new Point(0,0);
         displayPoint[1] = new Point(0,0);
         displayPoint[2] = new Point(0,0);
         trainThread = new Thread(this);
         trainThread.start();
      
         server = new TrainServer("Train Server");
         server.start();
      
         this.setVisible(true);
      }
   
       private void initGraphics()
      {
         this.setSize(WIDTH, HEIGHT);
         setLocation((int)Toolkit.getDefaultToolkit().getScreenSize().getWidth()/2-this.getWidth()/2,(int)Toolkit.getDefaultToolkit().getScreenSize().getHeight()/2-this.getHeight()/2);
         setResizable(false);
         setTitle("A-Team Somethin' Special: Trains");
         setUndecorated(false);
         setVisible(true);
      
         backbuffer = this.createImage(getWidth(), getHeight());
         g = (Graphics2D) backbuffer.getGraphics();
      
         minimap = this.createImage(getWidth(), getHeight());
         m = (Graphics2D) minimap.getGraphics();
      
         mask = new Color(0, 0, 0, 191);
         GUIFont = new Font("Sans Serif", Font.PLAIN, 20);
         instructionsTitle = new Font("Serif", Font.BOLD, 26);
         instructionsFont = new Font("Monospaced", Font.PLAIN, 14);
      
      // gets map image
         MediaTracker watch=new MediaTracker(this);
         map = Toolkit.getDefaultToolkit().getImage("TrackOverview.png");
         mapA = Toolkit.getDefaultToolkit().getImage("TrackA.png");
         mapB = Toolkit.getDefaultToolkit().getImage("TrackB.png");
         mapC = Toolkit.getDefaultToolkit().getImage("TrackC.png");
         face = Toolkit.getDefaultToolkit().getImage("face.gif");
         trainimage = Toolkit.getDefaultToolkit().getImage("ctrain.png");
         trainimage2 = Toolkit.getDefaultToolkit().getImage("train.png");
      
         watch.addImage(map, 0);
         watch.addImage(mapA, 1);
         watch.addImage(mapB, 2);
         watch.addImage(mapC, 3);
         watch.addImage(face, 4);
         watch.addImage(trainimage, 5);
         watch.addImage(trainimage2, 6);
      	
         try
         {watch.waitForAll();} 
             catch (InterruptedException e){}
      }
   
       public void run() // while the entire basis of math is true... this should not ever be true, yet still this loop executes... strange....
      {
         while (0==0)
         {
            display();
            displayPoint = server.getDisplayPoints();
            if(System.currentTimeMillis()-oldTime > 1000)
            {
               speedsForDisplay = server.getSpeeds();
               for(int t=0; t<speedsForDisplay.length; t++)
                  trainDistance[t]+=speedsForDisplay[t];
               oldTime=System.currentTimeMillis();
            }
            try
            {Thread.currentThread().sleep(10);}
                catch (Exception e){}
         }
      }
   
       private void display()
      {
      // backdrop & map
         g.setColor(Color.white);
         g.fillRect(0, 0, WIDTH, HEIGHT);
      
         try
         {
            if (view == 0)
               displayOverview();
            else if(view == 5)
            {
               if(animation<25)
               {
                  displayOverview();
                  animation++;
               }
               else
                  view=2;	
               g.setColor(Color.black);
               xAnimation = animation*35;
               if(xAnimation>WIDTH-30)
                  xAnimation = WIDTH-30;
                  
               yAnimation = animation*25;
               if(yAnimation>HEIGHT-20)
                  yAnimation = HEIGHT-20;
               g.drawRect(7,30, xAnimation-70+WIDTH/3, yAnimation-70+HEIGHT/3);
               g.drawRect(7,30, xAnimation-35+WIDTH/3, yAnimation-35+HEIGHT/3);
               g.drawRect(7,30, xAnimation+WIDTH/3, yAnimation+HEIGHT/3);
            }
            else if(view == -1)
            {
               if(animation>0)
               {
                  displayClient(0);
                  animation--;
               }
               else
                  view=0;	
               g.setColor(Color.black);
               xAnimation = animation*35;
               if(xAnimation<7)
                  xAnimation = 7;
               yAnimation = animation*25;
               if(yAnimation<30)
                  yAnimation = 30;
               g.drawRect(7,30, xAnimation-70+WIDTH/3, yAnimation-70+HEIGHT/3);
               g.drawRect(7,30, xAnimation-35+WIDTH/3, yAnimation-35+HEIGHT/3);
               g.drawRect(7,30, xAnimation+WIDTH/3, yAnimation+HEIGHT/3);
            }
            else if(view == 1)
               displayClient(0);
            else if(view == 5)
            {
               if(animation<25)
               {
                  displayOverview();
                  animation++;
               }
               else
                  view=4;	
               g.setColor(Color.black);
               xAnimation = animation*35;
               if(xAnimation>WIDTH-30)
                  xAnimation = WIDTH-30;
               yAnimation = animation*20;
               if(yAnimation>HEIGHT-20)
                  yAnimation = HEIGHT-20;
               g.drawRect(7,(int)(HEIGHT/3.03 + 7)-yAnimation/2+35, xAnimation-70+WIDTH/3, yAnimation-70+HEIGHT/3);
               g.drawRect(7,(int)(HEIGHT/3.03 + 7)-yAnimation/2+15, xAnimation-35+WIDTH/3, yAnimation-35+HEIGHT/3);
               g.drawRect(7,(int)(HEIGHT/3.03 + 7)-yAnimation/2, xAnimation+WIDTH/3, yAnimation+HEIGHT/3);
               
            }
            else if(view == -3)
            {
               if(animation>0)
               {
                  displayClient(1);
                  animation--;
               }
               else
                  view=0;	
               g.setColor(Color.black);
               xAnimation = animation*35;
               if(xAnimation<7)
                  xAnimation = 7;
               yAnimation = animation*20;
               if(yAnimation<30)
                  yAnimation = 30;
               g.drawRect(7,(int)(HEIGHT/3.03 + 7)-yAnimation/2+35, xAnimation-70+WIDTH/3, yAnimation-70+HEIGHT/3);
               g.drawRect(7,(int)(HEIGHT/3.03 + 7)-yAnimation/2+15, xAnimation-35+WIDTH/3, yAnimation-35+HEIGHT/3);
               g.drawRect(7,(int)(HEIGHT/3.03 + 7)-yAnimation/2, xAnimation+WIDTH/3, yAnimation+HEIGHT/3);
               
            }             
            else if(view == 2)
               displayClient(1);
            else if(view == 5)
            {
               if(animation<25)
               {
                  displayOverview();
                  animation++;
               }
               else
                  view=6;	
               g.setColor(Color.black);
               xAnimation = animation*35;
               if(xAnimation>WIDTH-30)
                  xAnimation = WIDTH-30;
               yAnimation = animation*20;
               if(yAnimation>HEIGHT-20)
                  yAnimation = HEIGHT-20;
               g.drawRect(7,(int)(2*HEIGHT/3.03-4)-yAnimation+70, xAnimation-70+WIDTH/3, yAnimation+HEIGHT/3);
               g.drawRect(7,(int)(2*HEIGHT/3.03-4)-yAnimation+35, xAnimation-35+WIDTH/3, yAnimation+HEIGHT/3);
               g.drawRect(7,(int)(2*HEIGHT/3.03-4)-yAnimation, xAnimation+WIDTH/3, yAnimation+HEIGHT/3);
            }
            else if(view == -5)
            {
               if(animation>0)
               {
                  displayClient(2);
                  animation--;
               }
               else
                  view=0;	
               g.setColor(Color.black);
               xAnimation = animation*35;
               if(xAnimation<7)
                  xAnimation = 7;
               yAnimation = animation*20;
               if(yAnimation<30)
                  yAnimation = 30;
               g.drawRect(7,(int)(2*HEIGHT/3.03-4)-yAnimation+70, xAnimation-70+WIDTH/3, yAnimation+HEIGHT/3);
               g.drawRect(7,(int)(2*HEIGHT/3.03-4)-yAnimation+35, xAnimation-35+WIDTH/3, yAnimation+HEIGHT/3);
               g.drawRect(7,(int)(2*HEIGHT/3.03-4)-yAnimation, xAnimation+WIDTH/3, yAnimation+HEIGHT/3);
            }     
            
            else if(view == 3)
               displayClient(2);
         }
             catch (Exception e)
            {e.printStackTrace();}
      
         if (exiting)
            displayClosing();
         else if (instructions)
            displayInstructions();
      
         getGraphics().drawImage(backbuffer,0, 0, null);
      }
   
       private void displayOverview()
      {
         g.drawImage(map, WIDTH - map.getWidth(null) - 10, 20, this);
      
         g.setFont(GUIFont);
      	
         g.setColor(TRAIN_A_COLOR);
         if(displayPoint[0]!=null)
         {
            if (!easter)
               g.fillOval((int)(displayPoint[0].x+(WIDTH - map.getWidth(null) -10)-5),(int)(displayPoint[0].y+20-5),10,10);
            else
               g.drawImage(face,(int)(displayPoint[0].x+(WIDTH - map.getWidth(null) -10)-26),(int)(displayPoint[0].y+20-35),this);
         }
         g.drawString(trainDistance[0]+" units", 350, 70);
      	
         g.setColor(TRAIN_B_COLOR);
         if(displayPoint[1]!=null)
         {
            if (!easter)
               g.fillOval((int)(displayPoint[1].x+(WIDTH - map.getWidth(null)-10)-5),(int)(displayPoint[1].y+20-5),10,10);
            else
               g.drawImage(face,(int)(displayPoint[1].x+(WIDTH - map.getWidth(null)-10)-26),(int)(displayPoint[1].y+20-35),this);
         }
         g.drawString(trainDistance[1]+" units", 350, 305);
      	
         g.setColor(TRAIN_C_COLOR);
         if(displayPoint[2]!=null)
         {
            if (!easter)
               g.fillOval((int)(displayPoint[2].x+(WIDTH - map.getWidth(null)-10)-5),(int)(displayPoint[2].y+20-5),10,10);
            else
               g.drawImage(face,(int)(displayPoint[2].x+(WIDTH - map.getWidth(null)-10)-26),(int)(displayPoint[2].y+20-35),this);
         }
         g.drawString(trainDistance[2]+" units", 350, 555);
      	
         g.setColor(Color.black);
         g.drawString("Track Overview", WIDTH - 145, 45);
         g.drawString("Train A Distance:", 350, 45);
         g.drawString("Train B Distance:", 350, 280);
         g.drawString("Train C Distance:", 350, 530);	
         g.drawString("Hold F1 for instructions", WIDTH - 210, HEIGHT - 10);
      
      // minimap backdrop
         g.fillRect(0, 0, WIDTH/3, HEIGHT);
         for (int i=0; i<3; i++)
            displayMinimap(i, server.getMap(i));
      }
   
       private void displayClient(int trackNum)
      {
         String mapKey = server.getMap(trackNum);
      
         if (mapKey != null) // train connected
         {
            double location = Double.parseDouble(mapKey.split(":")[0]);
            int speed = Integer.parseInt(mapKey.split(":")[1]), direction = Integer.parseInt(mapKey.split(":")[2]);
            boolean stopped = Boolean.parseBoolean(mapKey.split(":")[3]);
         
            g.drawImage(clientMap(trackNum, location, speed, direction, stopped), 0, 0, this);
         }
         
         else // train not connected
         {
            String trackLetter = "" + (char)(trackNum + 65);
            g.setColor(Color.black);
            g.setFont(GUIFont);
            g.drawString("Track " + trackLetter, WIDTH/2 - 20, HEIGHT/2);
            g.drawString("not running", WIDTH/2 - 30, HEIGHT/2 + 20);
         }
      }
   
       private void displayInstructions()
      {
      	/*
         if(instructions)
         {
            if(maskAnimation<=180)
            {
               maskAnimation+=30;
               mask = new Color(0,0,0,maskAnimation);
            }
         }
         else
         {
            if(maskAnimation>0)
            {
               maskAnimation-=30;
               mask = new Color(0,0,0,maskAnimation);
            }
         }   
         if(maskAnimation>0)
         {
      
         }
      
         if(maskAnimation>=180)
         {
      	*/
      	
      	// displays semi-transparent "mask" over standard GUI
         g.setColor(mask);
         g.fillRect(0, 0, WIDTH, HEIGHT);
      	
         // displays white box for instructions to be printed        
         g.setColor(Color.white);
         g.fillRect(WIDTH / 4, HEIGHT / 4, WIDTH / 2, HEIGHT / 2);
         
         // displays instructions
         g.setColor(Color.black);
         g.setFont(instructionsTitle);
         if (view == 0)
            g.drawString("INSTRUCTIONS FOR TRAIN SERVER", WIDTH/4 + 13, HEIGHT/4 +25);
         else
            g.drawString("INSTRUCTIONS FOR TRAIN CONTROL", WIDTH/4 + 13, HEIGHT/4 + 25);
         
         g.setFont(instructionsFont);
         if (view == 0)
         {
            g.drawString("To take control of Train A, press the '1' key.", WIDTH/4 + 20, HEIGHT/4 + 60);
            g.drawString("To take control of Train B, press the '2' key.", WIDTH/4 + 20, HEIGHT/4 + 80);
            g.drawString("To take control of Train C, press the '3' key.", WIDTH/4 + 20, HEIGHT/4 + 100);
            
            g.drawString("To return to the track overview, press the '0' key.", WIDTH/4 + 20, HEIGHT/4 + 140);
            g.drawString("To exit, press the Escape key.", WIDTH/4 + 20, HEIGHT/4 + 160);
         }
            
         else
         {
            g.drawString("To increase speed, press the up arrow key.", WIDTH/4 + 20, HEIGHT/4 + 60);
            g.drawString("To decrease speed, press the down arrow key.", WIDTH/4 + 20, HEIGHT/4 + 80);
            g.drawString("To direct train forward, press the right arrow key.", WIDTH/4 + 20, HEIGHT/4 + 100);
            g.drawString("To direct train backwards, press the left arrow key.", WIDTH/4 + 20, HEIGHT/4 + 120);
            
            g.drawString("To stop train, press the spacebar.", WIDTH/4 + 20, HEIGHT/4 + 160);
            g.drawString("	(to resume activity, press spacebar again)", WIDTH/4 + 20, HEIGHT/4 + 180);
            
            g.drawString("To take control of the current train, press the ENTER key.", WIDTH/4 + 20, HEIGHT/4 + 220);
            g.drawString("To return to the track overview, press '0' key.", WIDTH/4 + 20, HEIGHT/4 + 240);
            g.drawString("To exit, press the Escape key.", WIDTH/4 + 20, HEIGHT/4 + 260);
         }
         //}
      }
   
       private void displayMinimap(int trackNum, String mapKey)
      {
         if (mapKey != null) // train connected
         {
            double location = Double.parseDouble(mapKey.split(":")[0]);
            int speed = Integer.parseInt(mapKey.split(":")[1]), direction = Integer.parseInt(mapKey.split(":")[2]);
            boolean stopped = Boolean.parseBoolean(mapKey.split(":")[3]);
         
            g.drawImage(clientMap(trackNum, location, speed, direction, stopped), 0, (int)(trackNum*HEIGHT/3.03 + 17), WIDTH/3 - 5, (int)(HEIGHT/3.03 - 10), this);
         }
         
         else // train not connected
         {
            String trackLetter = "" + (char)(trackNum + 65);
         
            g.setColor(Color.white);
            g.fillRect(0,(int)(trackNum*HEIGHT/3.03 + 12), WIDTH/3 - 5,(int)(HEIGHT/3.03 - 5));
         
            g.setColor(Color.black);
            g.setFont(GUIFont);
            g.drawString("Track " + trackLetter, WIDTH/6 - 40, trackNum*HEIGHT/3 + HEIGHT/6);
            g.drawString("not running", WIDTH/6 - 50, trackNum*HEIGHT/3 + HEIGHT/6 + 20);
         }
      }
   
       private Image clientMap(int trackNum, double location, int speed, int direction, boolean stopped)
      {
         Image clientMap = null;
         String trackName = "Unknown", displaySpeed = "Unknown", displayDirection = "Unknown";
         Color col = Color.black;
      
      	// backdrop & map
         m.setColor(Color.white);
         m.fillRect(0, 0, WIDTH, HEIGHT);
      
         m.setColor(Color.black);
         m.setFont(GUIFont);    
      
         if (trackNum == 0)
         {
            trackName = "Track A";
            clientMap = mapA;
            col = TRAIN_A_COLOR;
         }
         else if (trackNum == 1)
         {
            trackName = "Track B";
            clientMap = mapB;
            col = TRAIN_B_COLOR;
         }
         else if (trackNum == 2)
         {
            trackName = "Track C";
            clientMap = mapC;
            col = TRAIN_C_COLOR;
         }
      
         if (speed == 0)
            displaySpeed = "Stopped";
         else if (speed == 1)
            displaySpeed = "Low";
         else if (speed == 2)
            displaySpeed = "Medium";
         else if (speed == 3)
            displaySpeed = "High";
      
         if (direction == 0)
            displayDirection = "Forward";
         else if (direction == 1)
            displayDirection = "Backward";
      
         try
         {
            m.drawImage(clientMap, WIDTH/2 - map.getWidth(null)/2, 0, this);
         
            m.setColor(Color.black);
            m.drawString("Location: " + location, WIDTH - 124, HEIGHT - 10);
         }
             catch (java.lang.NullPointerException e)
            {
               m.setColor(Color.red);
               m.drawString("Train not found!", 10, 45);
               m.drawString("Location: Unknown", WIDTH - 176, HEIGHT - 10);
            }
      		
         if (easter)
         {
            m.drawImage(face,(int)(displayPoint[trackNum].x+(WIDTH/2 - clientMap.getWidth(null)/2)-26),(int)(displayPoint[trackNum].y-35),this);
            m.drawImage(trainimage, WIDTH - 268, HEIGHT/2 - trainimage.getWidth(null)/2, this);
            m.drawImage(trainimage, trainimage.getWidth(null) + 10, HEIGHT/2 - trainimage.getWidth(null)/2, -trainimage.getWidth(null), trainimage.getHeight(null), this);
         }
         else
         {
            m.setColor(col);
            m.fillOval((int)(displayPoint[trackNum].x+(WIDTH/2 - clientMap.getWidth(null)/2)-5),(int)(displayPoint[trackNum].y-5),10,10);
            m.drawImage(trainimage2, WIDTH - trainimage2.getWidth(null) - 10, HEIGHT/2 - trainimage2.getWidth(null)/2, this);
            m.drawImage(trainimage2, trainimage2.getWidth(null) + 10, HEIGHT/2 - trainimage2.getWidth(null)/2, -trainimage2.getWidth(null), trainimage2.getHeight(null), this);
         }
      	
         if (stopped)
         {
            m.setColor(Color.red);
            m.drawString("Emergency Stop Activated", WIDTH - 244, HEIGHT - 36);
         }
      
         if (server.getControl(trackNum))
         {
            m.setColor(Color.blue);
            m.drawString("Server Control Activated", WIDTH - 225, 45);
         }
      
         m.setColor(Color.black);
         g.setFont(GUIFont);
         m.drawString(trackName, WIDTH/2 - 33, 45);
         m.drawString("Speed: " + displaySpeed, 10, HEIGHT - 35);
         m.drawString("Direction: " + displayDirection, 10, HEIGHT - 10);
         m.drawString("Press F1 for instructions", WIDTH/2 - 110, HEIGHT - 10);
      
         return minimap;
      }
   
       private void displayClosing()
      {
      	// displays semi-transparent "mask" over standard GUI
         g.setColor(mask);
         g.fillRect(0, 0, WIDTH, HEIGHT);
      
      	// displays white box for instructions to be printed
         g.setColor(Color.white);
         g.fillRect(WIDTH / 4, HEIGHT / 4, WIDTH / 2, HEIGHT / 2);
      	
         g.setColor(Color.black);
         g.setFont(new Font("Serif", Font.BOLD, 50));
         g.drawString("CLOSING...", WIDTH/4 + 50, HEIGHT/4 + 50);
         g.drawString("RESETTING", WIDTH/4 + 50, HEIGHT/4 + 100);
         g.drawString("TRAINS...", WIDTH/4 + 50, HEIGHT/4 + 150);
      }
   
       private void shutDown()
      {
         exiting = true;
         display();
         server.exit();	
         System.exit(0);
      }
   
       public void keyPressed(KeyEvent e)
      {
         switch(e.getKeyCode())
         {
            case KeyEvent.VK_1:	
               //if(!canRunAnimations)
               //   view = 2;
               //else
               view = 1;
               for(int i=0; i<8; i++)
                  easterGates[i] = false;
               break;
            case KeyEvent.VK_2:
               //if(!canRunAnimations)
               //   view = 4;
               //else
               //   view = 3;
               view = 2;
               for(int i=0; i<8; i++)
                  easterGates[i] = false;
               break;
            case KeyEvent.VK_3:
               //if(!canRunAnimations)
               //   view = 6;
               //else
               //   view = 5;
               view = 3;
               for(int i=0; i<8; i++)
                  easterGates[i] = false;
               break;
            case KeyEvent.VK_0:
               if(!canRunAnimations)
                  view = 0;
               else
               {
                  if(view!=0)
                  {
                     xAnimation =0;
                     yAnimation =0;
                     view = -1*(view-1);
                  }
               }
               for(int i=0; i<8; i++)
                  easterGates[i] = false;
               break;
            case KeyEvent.VK_ESCAPE:
               for(int i=0; i<8; i++)
                  easterGates[i] = false;
               shutDown();
               break;
            case KeyEvent.VK_ENTER:
               if (view > 0 && view < 4)
                  server.changeControl(view-1);
               for(int i=0; i<8; i++)
                  easterGates[i] = false;
               break;
            case KeyEvent.VK_F1:
               instructions = !instructions;
               for(int i=0; i<8; i++)
                  easterGates[i] = false;
               break;
         	
            case KeyEvent.VK_UP:
               if (view > 0 && view < 4 && server.getControl(view - 1))
               {
                  String mapKey = server.getMap(view - 1);
               
                  if (mapKey != null) // train connected
                  {
                     int spd = Integer.parseInt(mapKey.split(":")[1]), dir = Integer.parseInt(mapKey.split(":")[2]);
                  
                     spd++;
                     if (spd > 3)
                        spd = 3;
                  
                     server.setTrainSpeed(view - 1, spd + 4*dir);
                  }
               }
               for(int i=0; i<8; i++)
                  easterGates[i] = false;
               break;
            case KeyEvent.VK_DOWN:
               if (view > 0 && view < 4 && server.getControl(view - 1))
               {
                  String mapKey = server.getMap(view - 1);
               
                  if (mapKey != null) // train connected
                  {
                     int spd = Integer.parseInt(mapKey.split(":")[1]), dir = Integer.parseInt(mapKey.split(":")[2]);
                  
                     spd--;
                     if (spd < 0)
                        spd = 0;
                  
                     server.setTrainSpeed(view - 1, spd + 4*dir);
                  }
               }
               for(int i=0; i<8; i++)
                  easterGates[i] = false;
               break;
            case KeyEvent.VK_LEFT:
               if (view > 0 && view < 4 && server.getControl(view - 1))
               {
                  String mapKey = server.getMap(view - 1);
               
                  if (mapKey != null) // train connected
                  {
                     int spd = Integer.parseInt(mapKey.split(":")[1]), dir = Integer.parseInt(mapKey.split(":")[2]);
                  
                     dir = 1;
                  
                     server.setTrainSpeed(view - 1, spd + 4*dir);
                  }
               }
               for(int i=0; i<8; i++)
                  easterGates[i] = false;
               break;
            case KeyEvent.VK_RIGHT:
               if (view > 0 && view < 4 && server.getControl(view - 1))
               {
                  String mapKey = server.getMap(view - 1);
               
                  if (mapKey != null) // train connected
                  {
                     int spd = Integer.parseInt(mapKey.split(":")[1]), dir = Integer.parseInt(mapKey.split(":")[2]);
                  
                     dir = 0;
                  
                     server.setTrainSpeed(view - 1, spd + 4*dir);
                  }
               }
               for(int i=0; i<8; i++)
                  easterGates[i] = false;
               break;
            case KeyEvent.VK_SPACE:
               if (view > 0 && view < 4 && server.getControl(view - 1))
               {
                  String mapKey = server.getMap(view - 1);
               
                  if (mapKey != null) // train connected
                  {
                     boolean stop = Boolean.parseBoolean(mapKey.split(":")[3]);
                  
                     stop = !stop;
                  
                     server.setTrainStop(view - 1, stop);
                  }
               }
               for(int i=0; i<8; i++)
                  easterGates[i] = false;
               break;
         		
         	// face easter egg cases
            case KeyEvent.VK_A:
               if (easterGates[6])
                  easterGates[7] = true;
               else if (easterGates[4])
                  easterGates[5] = true;
               else
                  for(int i=0; i<8; i++)
                     easterGates[i] = false;
               break;
            case KeyEvent.VK_C:
               easterGates[0] = true;
               break;
            case KeyEvent.VK_D:
               if (easterGates[5])
                  easterGates[6] = true;
               else
                  for(int i=0; i<8; i++)
                     easterGates[i] = false;
               break;
            case KeyEvent.VK_H:
               if (easterGates[0])
                  easterGates[1] = true;
               else
                  for(int i=0; i<8; i++)
                     easterGates[i] = false;
               break;
            case KeyEvent.VK_I:
               if (easterGates[2])
                  easterGates[3] = true;
               else
                  for(int i=0; i<8; i++)
                     easterGates[i] = false;
               break;
            case KeyEvent.VK_O:
               if (easterGates[1])
                  easterGates[2] = true;
               else
                  for(int i=0; i<8; i++)
                     easterGates[i] = false;
               break;
            case KeyEvent.VK_R:
               if (easterGates[7])
               {
                  easter = !easter;
                  for(int i=0; i<8; i++)
                     easterGates[i] = false;
               }
               else if (easterGates[3])
                  easterGates[4] = true;     
               else
                  for(int i=0; i<8; i++)
                     easterGates[i] = false;
               break;
            default:
               for(int i=0; i<8; i++)
                  easterGates[i] = false;
               break;
         }
      }
   
       public void windowClosing(WindowEvent e)
      {shutDown();}
   
       public void keyReleased(KeyEvent e){}
       public void keyTyped(KeyEvent e){}
       public void mouseClicked(MouseEvent e){}
       public void mouseEntered(MouseEvent e){}
       public void mouseExited(MouseEvent e){}
       public void mousePressed(MouseEvent e){}
       public void mouseReleased(MouseEvent e){}
       public void windowDeactivated(WindowEvent e){}
       public void windowActivated(WindowEvent e){}
       public void windowDeiconified(WindowEvent e){}
       public void windowIconified(WindowEvent e){}
       public void windowClosed(WindowEvent e){}
       public void windowOpened(WindowEvent e){}
   }
