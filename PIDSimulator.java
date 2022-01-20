//Imports--------------------------------------------------------------------------------------------------------------------------------------------
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

@SuppressWarnings("serial")
public class PIDSimulator extends JPanel {
//Constants------------------------------------------------------------------------------------------------------------------------------------------
    static final int timeStep = 20;         //simulation time step (milliseconds)
    static final double length = 0.25;      //length of pendulum arm  (meters)
    static final double PendMass=1;         //mass of weight at bottom of pendulum (kg)
    static final int metersToPixel = 2000;  //value determines pixel length of the pendulum
    static final GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
    static final int width = gd.getDisplayMode().getWidth()-100;
    static final int height = gd.getDisplayMode().getHeight()-100;
    static final int pivotX = width/2;  
    static final int pivotY = 100;
    static final double friction = 0.1;     //friction torque of pendulum pivot point (Nm)
    static final double rotation = Math.PI*2;


    static final DrawingManager panel = new DrawingManager();
    static final Font font = new Font("Serif", Font.PLAIN, 20);
    static final Image pendulum = PIDMethods.imageURL("https://github.com/CrazyMeowCows/Images/blob/main/Pendulum.png?raw=true");

//Variables------------------------------------------------------------------------------------------------------------------------------------------
    static double pGain = 0;
    static double iGain = 0;
    static double dGain = 0;
    static double TargetAngle = Math.toRadians(-45);
    static double pCommand = 0;
    static double iCommand = 0;
    static double dCommand = 0;
    static double AngleError = 0;
    static double IntError = 0;
    static double PreviousError = 0;
    static double Tgravity = 0;
    static double Tfriction= 0;
    static boolean stepToggle = false;
    static double gravity = 9.81;     //gravitational constant (9.81 m/s2)  

    static long time = System.currentTimeMillis();

    static double angle = Math.toRadians(45);
    static double motorMax = 5;
    static double motorOut = 0;
    static double velocity = 0;
    static double acceleration = 0;
    static int pendulumX = (int)Math.round(pivotX+Math.sin(angle)*length*2000);
    static int pendulumY = (int)Math.round(pivotY+Math.cos(angle)*length*2000);
    static int TargetX = (int)Math.round(pivotX+Math.sin(TargetAngle)*length*1500);
    static int TargetY = (int)Math.round(pivotY+Math.cos(TargetAngle)*length*1500);

    public static void main(String[] args) {
    //Frame Setup------------------------------------------------------------------------------------------------------------------------------------
        JFrame frame = new JFrame("PID Simulator");
        panel.setLayout(null);
        panel.setPreferredSize(new Dimension(width, height));

    //Inout fields-----------------------------------------------------------------------------------------------------------------------------------
        PIDMethods.label("P Gain: ", 10, 5);
        PIDMethods.label("I Gain: ", 10, 35);
        PIDMethods.label("D Gain: ", 10, 65);
        PIDMethods.label("Target ∡: ", 10, 95);
        PIDMethods.label("Start ∡: ", 10, 225);
        PIDMethods.label("Motor Limit: ", 10, 255);

        
        JTextField pText = PIDMethods.text("0", 100, 5);
        JTextField iText = PIDMethods.text("0", 100, 35);
        JTextField dText = PIDMethods.text("0", 100, 65);
        JTextField TAtext = PIDMethods.text("" + Math.toDegrees(TargetAngle), 100, 95);
        JTextField rAngle = PIDMethods.text("" + Math.round(Math.toDegrees(angle)), 125, 225);
        JTextField maxPwr = PIDMethods.text("" + Math.round(motorMax), 125, 255);


        JButton reset = PIDMethods.button("Reset To Start Angle", width/2, 30, 300, 50);
        JButton pause = PIDMethods.button("Pause", width/4, height-50, 200, 50);
        JButton start = PIDMethods.button("Play", width/4*2, height-50, 200, 50);
        JButton step = PIDMethods.button("Step Forward", width/4*3, height-50, 200, 50);
        JButton flip = PIDMethods.button("Flip Target ∡", 100, 150, 150, 30);
        JButton gravonoff = PIDMethods.button("Gravity on/off", width-125, 275, 150, 30);

        frame.add(panel);

    //Main Frame Properties--------------------------------------------------------------------------------------------------------------------------
        frame.setIconImage(PIDMethods.imageURL("https://github.com/CrazyMeowCows/Images/blob/main/AdambotsLogoBlack.png?raw=true"));
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);  

    //Timer------------------------------------------------------------------------------------------------------------------------------------------
        time = System.currentTimeMillis();
        Timer timer = new Timer(timeStep, new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                PIDCalc();
                Physics();
                frame.repaint();
            }
        });
        timer.start();  

    //Action Listeners-------------------------------------------------------------------------------------------------------------------------------
        Action pidInputs = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                try {
                    pGain = Double.parseDouble(pText.getText());
                    iGain = Double.parseDouble(iText.getText());
                    dGain = Double.parseDouble(dText.getText());
                    TargetAngle = Math.toRadians(Double.parseDouble(TAtext.getText()));
                    motorMax = Double.parseDouble(maxPwr.getText());
                    
                } catch (NumberFormatException value) {
                    System.out.println("Invalid Entry");
                }
            }
        };
        pText.addActionListener(pidInputs);
        iText.addActionListener(pidInputs);
        dText.addActionListener(pidInputs);
        TAtext.addActionListener(pidInputs);
        maxPwr.addActionListener(pidInputs);

        Action resetClicked = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                try {
                    angle = Math.toRadians(Double.parseDouble(rAngle.getText()));
                    velocity = 0;
                    acceleration = 0;
                    IntError=0;
                } catch (NumberFormatException value) {
                    System.out.println("Invalid Entry");
                }
            }
        };
        reset.addActionListener(resetClicked);

        Action flipClicked = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                try {
                    TargetAngle=TargetAngle*-1;
                } catch (NumberFormatException value) {
                    System.out.println("Invalid Entry");
                }
            }
        };
        flip.addActionListener(flipClicked);

        Action gravonoffClicked = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                try {
                    if(gravity==0){gravity=9.81;}
                    else{gravity=0;};
                } catch (NumberFormatException value) {
                    System.out.println("Invalid Entry");
                }
            }
        };
        gravonoff.addActionListener(gravonoffClicked);


        Action stopClicked = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                timer.stop();
            }
        };
        pause.addActionListener(stopClicked);

        Action startClicked = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                timer.start();
            }
        };
        start.addActionListener(startClicked);

        Action stepClicked = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                timer.stop();
                PIDCalc();
                Physics();
                frame.repaint();
            }
        };
        step.addActionListener(stepClicked);
    }

//PID Calculation of Motor Torque--------------------------------------------------------------------------------------------------------------------------------------------
    static void PIDCalc () {
        AngleError=TargetAngle-angle;                       //calc angle error (target angle-current angle)
        pCommand = pGain * AngleError;                      //calculate Proportional command
        dCommand = dGain * (AngleError - PreviousError)/((double)timeStep/1000);    //calculate Derivative command
        //if(Math.abs(motorOut)<motorMax){IntError=IntError+AngleError;}              //Accumulate the error if motor is not at its max value (anti-windup)
        //if(iGain==0){IntError=0;}                           //zero out the accumulated iCommand if iGain is set to zero (Integral not being used)
        iCommand = iCommand + AngleError*iGain;
        if(iGain==0){iCommand=0;}
        //iCommand = iGain * IntError;                        //calculate the Integral command
        iCommand = Math.min(motorMax, iCommand);            //limit intergral torque command to limit of motor 
        iCommand = Math.max(-motorMax, iCommand);           //      to prevent it from endlessly accumulating
        motorOut = pCommand + dCommand + iCommand;          //sum P I and D terms into full command
        motorOut = Math.min(motorMax, motorOut);            //limit motor torque command to 
        motorOut = Math.max(-motorMax, motorOut);           //     the capability of the motor
        PreviousError = AngleError;                           //Put current angle error into previous error for next cycle
}

//Physics--------------------------------------------------------------------------------------------------------------------------------------------
    static void Physics () {
        Tgravity = (PendMass*gravity*length*Math.sin(angle));         //Torque on pivot due to gravity pulling down on weight
        Tfriction = friction*Math.signum(velocity);                   //Torque on pivot due to friction (always opposes velocity)
        acceleration = (motorOut-Tgravity-Tfriction)/(PendMass*length*length);  // angular accel = total torque/(mass*length^2) 
        velocity += acceleration*timeStep/1000;                       //change in velocity is acceleration * time step
        angle += velocity*timeStep/1000;

        pendulumX = (int)Math.round(pivotX+Math.sin(angle)*length*metersToPixel);
        pendulumY = (int)Math.round(pivotY+Math.cos(angle)*length*metersToPixel);
        TargetX = (int)Math.round(pivotX+Math.sin(TargetAngle)*length*metersToPixel);
        TargetY = (int)Math.round(pivotY+Math.cos(TargetAngle)*length*metersToPixel);
    }

//Drawing--------------------------------------------------------------------------------------------------------------------------------------------
    static class DrawingManager extends JPanel {
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;

            setBackground(new Color(100, 100, 100)); //Fill Background with grey

        //Drawing Background Grid Centered on the Pivot
            g.setColor(new Color(255, 255, 255, 30));
            for(int i = -1*75; i <= height-pivotY; i += 75){
                g.drawLine(0, pivotY+i, width, pivotY+i);
            }
            for(int i = -15*75; i <= width-pivotX; i += 75){
                g.drawLine(pivotX+i, 0, pivotX+i, height);
            }

        //Drawing Pendulum and Target Line
            g.setColor(new Color(255, 255, 255));
            g.drawLine(pivotX, pivotY, pendulumX, pendulumY);
            g.setColor(new Color(255, 0, 0, 100));
            g.drawLine(pivotX, pivotY, TargetX, TargetY);
            g.fillOval(TargetX-50, TargetY-50, 100, 100);
            g.drawOval(TargetX-50, TargetY-50, 100, 100);
            g.setColor(new Color(0, 0, 0));
            g.fillOval(pivotX-10/2, pivotY-10/2, 10, 10);

            PIDMethods.drawRotatedImage(g2d, pendulum, angle, pendulumX, pendulumY);

        //Drawing Arrows Showing P, I, D
            PIDMethods.drawArrowOffPendulum(pCommand*10, length/4*metersToPixel, g);
            PIDMethods.drawRotatedTextOffPendulum(g2d, "P", length/4*metersToPixel);
            PIDMethods.drawArrowOffPendulum(iCommand*10, length/4*metersToPixel*2, g);
            PIDMethods.drawRotatedTextOffPendulum(g2d, "I", length/4*metersToPixel*2);
            PIDMethods.drawArrowOffPendulum(dCommand*10, length/4*metersToPixel*3, g);
            PIDMethods.drawRotatedTextOffPendulum(g2d, "D", length/4*metersToPixel*3);

        //Drawing Values in the Upper Right
            g.setFont(font);
            g.setColor(new Color(255, 255, 255));
            g.drawString("Angle: " + PIDMethods.round(Math.toDegrees(angle)), width-150, 25);
            g.drawString("Velocity: " + PIDMethods.round(velocity), width-150, 55);
            g.drawString("Accel.: " + PIDMethods.round(acceleration), width-150, 85);
            g.drawString("P: " + PIDMethods.round(pCommand), width-150, 115);
            g.drawString("I: " + PIDMethods.round(iCommand), width-150, 145);
            g.drawString("D: " + PIDMethods.round(dCommand), width-150, 175);
            g.drawString("Motor: " + PIDMethods.round(motorOut), width-150, 205);
            g.drawString("Gravity: "+PIDMethods.round(gravity),width-150,235);

        //Displaying FPS
            try {
                g.drawString("FPS: " + 1000/(System.currentTimeMillis()-time), 10, height-10);
            } catch (ArithmeticException value) {}
            time = System.currentTimeMillis();
        }
    }
}
