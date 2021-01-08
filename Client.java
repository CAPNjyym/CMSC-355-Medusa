// A-Team Somethin Special
// GUI for train client

   import java.awt.*;
   import java.awt.image.BufferedImage;
   import javax.imageio.ImageIO;
   import java.awt.event.*;
   import java.io.*;
   import javax.swing.JOptionPane;
   import java.net.InetAddress;

    public class Client extends Frame implements Runnable, KeyListener, MouseListener, WindowListener
   {
      private boolean instructions = false, exiting = false, easter = false, easterGates[];
      private Color mask;
      private Font GUIFont, instructionsFont, instructionsTitle;
   
      private final int[][] TRACK_SENSOR_POSITION = {{0,124,95,69,45,27,11},		//The index of trackPoints when train is at a sensor
         	      					{0,113,86,72,28,11},	
         						{3,7,13,18,26,44,69} };
   
      private Point[] trackPoints;
      private int currentPoint;
      private double icon_location, old_icon_location;
      private int timeConstant;
      private long oldTimeAnimation;
   
   
      private Graphics2D g;															//Used for double buffering in windowed mode
      private Image backbuffer, map, face, trainimage, trainimage2;		//Used for double buffering in windowed mode
   
      private Thread gameUpdateThread, thread;
      private TrainClient client;
   
      public final static int HEIGHT = 768, WIDTH = 1024;
   
   // should work without any arguments once the correct ips are found
       public static void main(String[] args) throws Exception
      {
         try
         {	
         //*****************************
         //*									*
         //* UNCOMMENT TO TEST LOCALLY	*
         //*									*
          //  Integer.parseInt("A");
         //*									*
         //*****************************
            
            
            if (args.length >= 2)
               new Client(args[0], Integer.parseInt(args[1]));
            else if (args.length == 1)
               new Client("128.172.167.179", Integer.parseInt(args[0]));
            else
            {
               String ip = InetAddress.getLocalHost().toString();
               if (ip.contains("/"))
                  ip = ip.split("/")[1];
               int trainNum = 0;
            
               if (ip.equals("128.172.167.178")) // its train A
                  trainNum = 0;
               else if (ip.equals("128.172.167.179")) // its train B
                  trainNum = 1;
               else if (ip.equals("128.172.167.180")) // its train C
                  trainNum = 2;
            	
               new Client("128.172.167.179", trainNum);
            }
         }
         
             catch (Exception e)
            {
               new Client("localhost", 0); // for testing outside train lab
               e.printStackTrace();
               //System.exit(0);
            }
      }
   
       public Client(String ip, int train)
      {
         easterGates = new boolean[8];
         for(int i=0; i<8; i++)
            easterGates[i] = false;
         	
      
         this.addKeyListener(this);
         this.addMouseListener(this);
         this.addWindowListener(this);
         loadTrackInfo(train);
         initGraphics(train);
      
         try
         {
            client = new TrainClient(TrainClient.ipFromString(ip), train);
            client.start();
         }
             catch (Exception e)
            {e.printStackTrace();}
      
         thread = new Thread(this);
         thread.start();
      }
   
       private void loadTrackInfo(int train)
      {
         java.util.ArrayList<Point> temp = new java.util.ArrayList<Point>();
         String ts = new String();
      
         try
         {
            java.util.Scanner s = null;
            if (train == TrainClient.TRACK_A)
               s = new java.util.Scanner(new File("trackA.map"));
            else if (train == TrainClient.TRACK_B)
               s = new java.util.Scanner(new File("trackB.map"));
            else if (train == TrainClient.TRACK_C)
               s = new java.util.Scanner(new File("trackC.map"));
         
            while (s.hasNext())
            {
               ts = s.nextLine();
               temp.add(new Point(Integer.parseInt(ts.split(",")[0]),Integer.parseInt(ts.split(",")[1])));
            }
            s.close();
         
            trackPoints = new Point[temp.size()];
            for(int t=0; t<trackPoints.length; t++)
               trackPoints[t] = temp.get(t);
            currentPoint = 0;
         }
             catch (Exception e)
            {e.printStackTrace();}
      }
   
       private void initGraphics(int trainNum)
      {
         this.setSize(WIDTH, HEIGHT);
         setLocation((int)Toolkit.getDefaultToolkit().getScreenSize().getWidth()/2-this.getWidth()/2,(int)Toolkit.getDefaultToolkit().getScreenSize().getHeight()/2-this.getHeight()/2);
         setResizable(false);
         setTitle("A-Team Somethin' Special: Trains");
         setUndecorated(false);
         setVisible(true);
      
         backbuffer = this.createImage(getWidth(), getHeight());
         g = (Graphics2D)backbuffer.getGraphics();
      
         mask = new Color(0, 0, 0, 191);
         GUIFont = new Font("Sans Serif", Font.PLAIN, 20);
         instructionsTitle = new Font("Serif", Font.BOLD, 26);
         instructionsFont = new Font("Monospaced", Font.PLAIN, 14);
      
      // gets map image
         MediaTracker watch=new MediaTracker(this);
      
         String mapName = "Unknown";
         if (trainNum == TrainClient.TRACK_A)
            mapName = "TrackA.png";
         else if (trainNum == TrainClient.TRACK_B)
            mapName = "TrackB.png";
         else if (trainNum == TrainClient.TRACK_C)
            mapName = "TrackC.png";
      
         map = Toolkit.getDefaultToolkit().getImage(mapName);
         face = Toolkit.getDefaultToolkit().getImage("face.gif");
         trainimage = Toolkit.getDefaultToolkit().getImage("ctrain.png");
         trainimage2 = Toolkit.getDefaultToolkit().getImage("train.png");
      
         watch.addImage(map, 0);
         watch.addImage(face, 1);
         watch.addImage(trainimage, 2);
         watch.addImage(trainimage2, 3);
      
         try
         {watch.waitForAll();}
             catch (Exception e)
            {e.printStackTrace();}
      }
   
       public void run()
      {
         while(0==0) // while the entire basis of math is true... this should not ever be true, yet still this loop executes... strange....
         {
            client.sendInfo();
            display();
            client.setTrainDisplayPoint(trackPoints[currentPoint].x, trackPoints[currentPoint].y);
         
         //radar points on map
            if((icon_location *10) % 2 == 0 && icon_location !=-1 && icon_location!= old_icon_location)
            {
               currentPoint = TRACK_SENSOR_POSITION[client.trackNum()][(int)icon_location];
               old_icon_location = icon_location;
            }
            
            switch(client.speed())
            {
               case 1:
                  if(client.trackNum() == TrainClient.TRACK_A)
                     timeConstant = 420;
                  else if(client.trackNum() == TrainClient.TRACK_B)
                     timeConstant = 395;
                  else if(client.trackNum() == TrainClient.TRACK_C)
                     timeConstant = 139;
                  break;
               case 2:
                  if(client.trackNum() == TrainClient.TRACK_A)
                     timeConstant = 52;
                  else if(client.trackNum() == TrainClient.TRACK_B)
                     timeConstant = 110;
                  else if(client.trackNum() == TrainClient.TRACK_C)
                     timeConstant = 93;
                  break;
               case 3:
                  if(client.trackNum() == TrainClient.TRACK_A)
                     timeConstant = 47;
                  else if(client.trackNum() == TrainClient.TRACK_B)
                     timeConstant = 79;
                  else if(client.trackNum() == TrainClient.TRACK_C)
                     timeConstant = 47;
                  break;
            }
         
            if(client.speed()!=0)
               if(System.currentTimeMillis()-oldTimeAnimation > timeConstant)
               {
                  if (client.direction()==0)
                     currentPoint++;
                  else
                     currentPoint--;
               
                  if (client.trackNum() == TrainClient.TRACK_C)
                  {
                     if(currentPoint > trackPoints.length-1)
                        currentPoint = trackPoints.length-1;
                     if(currentPoint < 0)
                        currentPoint = 0;
                  }
                  else
                  {
                     if(currentPoint > trackPoints.length-1)
                        currentPoint = 0;
                     if(currentPoint < 0)
                        currentPoint = trackPoints.length-1;
                  }
                  oldTimeAnimation = System.currentTimeMillis();
               }
            try
            {Thread.currentThread().sleep(10);}
                catch (InterruptedException e){}
         
         // for when server is shut down
            if (client.serverShutDown())
            {
               exiting = true;
               if (client.trainLocation() == 4.5)
                  client.exit();
            }
         }
      }
   
       private void display()
      {
         String trackName = "Unknown", displaySpeed = "Unknown", displayDirection = "Unknown";
      
         if (client.trackNum() == TrainClient.TRACK_A)
            trackName = "Track A";
         else if (client.trackNum() == TrainClient.TRACK_B)
            trackName = "Track B";
         else if (client.trackNum() == TrainClient.TRACK_C)
            trackName = "Track C";
      
         if (client.speed() == 0)
            displaySpeed = "Stopped";
         else if (client.speed() == 1)
            displaySpeed = "Low";
         else if (client.speed() == 2)
            displaySpeed = "Medium";
         else if (client.speed() == 3)
            displaySpeed = "High";
      
         if (client.direction() == 0)
            displayDirection = "Forward";
         else if (client.direction() == 1)
            displayDirection = "Backward";
      
      // backdrop & map
         g.setColor(Color.white);
         g.fillRect(0, 0, WIDTH, HEIGHT);
      
         g.setColor(Color.black);
         g.setFont(GUIFont);
      
         try
         {
            displayMap();
            icon_location = client.trainLocation();
            g.setColor(Color.black);
            g.drawString("Location: " + icon_location, WIDTH - 124, HEIGHT - 10);
         }
             catch (java.lang.NullPointerException e)
            {
               g.setColor(Color.red);
               g.drawString("Train not found!", 10, 45);
               g.drawString("Location: Unknown", WIDTH - 176, HEIGHT - 10);
            }
      
         if (client.stopped())
         {
            g.setColor(Color.red);
            g.drawString("Emergency Stop Activated", WIDTH - 244, HEIGHT - 36);
         }
      	
         if (client.controlled())
         {
            g.setColor(Color.blue);
            g.drawString("Server Control Activated", WIDTH - 225, 45);
         }
      
         g.setColor(Color.black);
         g.drawString(trackName, WIDTH/2 - 33, 45);
         g.drawString("Speed: " + displaySpeed, 10, HEIGHT - 35);
         g.drawString("Direction: " + displayDirection, 10, HEIGHT - 10);
         g.drawString("Press F1 for instructions", WIDTH/2 - 110, HEIGHT - 10);
      	
         if (exiting)
            displayClosing();
         else if (instructions)
            displayInstructions();
      
         getGraphics().drawImage(backbuffer,0, 0, null);
      }
   
       private void displayMap()
      {
         g.drawImage(map, WIDTH/2 - map.getWidth(null)/2, 25, this);
         
         if (easter)    
         {
            g.drawImage(face, trackPoints[currentPoint].x+(WIDTH/2 - map.getWidth(null)/2)-26,trackPoints[currentPoint].y+25-35,this);
            g.drawImage(trainimage, WIDTH - 268, HEIGHT/2 - trainimage.getWidth(null)/2, this);
            g.drawImage(trainimage, trainimage.getWidth(null) + 10, HEIGHT/2 - trainimage.getWidth(null)/2, -trainimage.getWidth(null), trainimage.getHeight(null), this);
         }
         else
         {
            g.setColor(Color.red);
            g.fillOval(trackPoints[currentPoint].x+(WIDTH/2 - map.getWidth(null)/2)-5,trackPoints[currentPoint].y+25-5,10,10);
            g.drawImage(trainimage2, WIDTH - trainimage2.getWidth(null) - 10, HEIGHT/2 - trainimage2.getWidth(null)/2, this);
            g.drawImage(trainimage2, trainimage2.getWidth(null) + 10, HEIGHT/2 - trainimage2.getWidth(null)/2, -trainimage2.getWidth(null), trainimage2.getHeight(null), this);
         }
      }
     
       private void displayInstructions()
      {
         // displays semi-transparent "mask" over standard GUI
         g.setColor(mask);
         g.fillRect(0, 0, WIDTH, HEIGHT);
      
         // displays white box for instructions to be printed
         g.setColor(Color.white);
         g.fillRect(WIDTH / 4, HEIGHT / 4, WIDTH / 2, HEIGHT / 2);
      
      	// displays instructions
         g.setColor(Color.black);
         g.setFont(instructionsTitle);
         g.drawString("INSTRUCTIONS FOR TRAIN CONTROL", WIDTH/4 + 13, HEIGHT/4 + 25);
      
         g.setFont(instructionsFont);
         g.drawString("To increase speed, press the up arrow key.", WIDTH/4 + 20, HEIGHT/4 + 60);
         g.drawString("To decrease speed, press the down arrow key.", WIDTH/4 + 20, HEIGHT/4 + 80);
         g.drawString("To direct train forward, press the right arrow key.", WIDTH/4 + 20, HEIGHT/4 + 100);
         g.drawString("To direct train backwards, press the left arrow key.", WIDTH/4 + 20, HEIGHT/4 + 120);
      
         g.drawString("To stop train, press the spacebar.", WIDTH/4 + 20, HEIGHT/4 + 160);
         g.drawString("	(to resume activity, press spacebar again)", WIDTH/4 + 20, HEIGHT/4 + 180);
      
         g.drawString("To exit, press the Escape key.", WIDTH/4 + 20, HEIGHT/4 + 220);
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
         g.drawString("TRAIN...", WIDTH/4 + 50, HEIGHT/4 + 150);
      }
   	
       private void exit()
      {
         exiting = true;
         display();      
         client.exit();     
         System.exit(0);
      }
   
       public void keyPressed(KeyEvent e)
      {
         if (!exiting)
         {
            switch (e.getKeyCode())
            {
               case KeyEvent.VK_UP:
                  for(int i=0; i<8; i++)
                     easterGates[i] = false;
                  if (!client.controlled())
                     client.increaseSpeed();
                  break;
               case KeyEvent.VK_DOWN:
                  for(int i=0; i<8; i++)
                     easterGates[i] = false;
                  if (!client.controlled())
                     client.decreaseSpeed();
                  break;
               case KeyEvent.VK_LEFT:
                  for(int i=0; i<8; i++)
                     easterGates[i] = false;
                  if (!client.controlled())
                     client.changeDirection(1);
                  break;
               case KeyEvent.VK_RIGHT:
                  for(int i=0; i<8; i++)
                     easterGates[i] = false;
                  if (!client.controlled())
                     client.changeDirection(0);
                  break;
               case KeyEvent.VK_SPACE:
                  for(int i=0; i<8; i++)
                     easterGates[i] = false;
                  if (!client.controlled())
                     client.halt();
                  break;
               case KeyEvent.VK_ESCAPE:
                  for(int i=0; i<8; i++)
                     easterGates[i] = false;
                  exit();
                  break;
               case KeyEvent.VK_F1:
                  for(int i=0; i<8; i++)
                     easterGates[i] = false;
                  instructions = !instructions;
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
      }
   
       public void windowClosing(WindowEvent e)
      {exit();}
   
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
