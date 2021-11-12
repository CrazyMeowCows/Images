//Imports--------------------------------------------------------------------------------------------------------------------------------------------
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.io.IOException;
import PIDMethods;

@SuppressWarnings("serial")
public class PIDSimulator extends JPanel {
//Constants------------------------------------------------------------------------------------------------------------------------------------------
    static final int timeStep = 10;
    static final double length = 500;
    static final int width = 1500;
    static final int height = 700;
    static final int pivotX = width/2;
    static final int pivotY = 100;
    static final double friction = 0.001;
    static final double rotation = Math.PI*2;

    static final DrawingManager panel = new DrawingManager();
    static final Font font = new Font("Serif", Font.PLAIN, 20);
    static final Image pendulum = PIDMethods.imageURL("https://github.com/CrazyMeowCows/Images/blob/main/Pendulum.png?raw=true");

//Variables------------------------------------------------------------------------------------------------------------------------------------------
    static double pGain = 0;
    static double iGain = 0;
    static double dGain = 0;

    static long time = System.currentTimeMillis();
    static double gravity = 50;
    static double angle = Math.toRadians(90);
    static float motorMax = 5;
    static float motorOut = 0;
    static double velocity = 0;
    static double acceleration = 0;
    static int pendulumX = (int)Math.round(pivotX+Math.sin(angle)*length);
    static int pendulumY = (int)Math.round(pivotY+Math.cos(angle)*length);

    public static void main(String[] args) {
    //Frame Setup------------------------------------------------------------------------------------------------------------------------------------
        JFrame frame = new JFrame("PID Simulator");
        panel.setLayout(null);
        panel.setPreferredSize(new Dimension(width, height));

    //Inout fields-----------------------------------------------------------------------------------------------------------------------------------
        PIDMethods.label("P Gain: ", 10, 5);
        PIDMethods.label("I Gain: ", 10, 35);
        PIDMethods.label("D Gain: ", 10, 65);
        PIDMethods.label("Reset ∡: ", 10, 125);
        PIDMethods.label("Max Pwr: ", 10, 155);
        
        JTextField pText = PIDMethods.text("0", 100, 5);
        JTextField iText = PIDMethods.text("0", 100, 35);
        JTextField dText = PIDMethods.text("0", 100, 65);
        JTextField rAngle = PIDMethods.text("" + Math.round(Math.toDegrees(angle)), 100, 125);
        JTextField maxPwr = PIDMethods.text("" + Math.round(motorMax), 100, 155);

        JButton reset = PIDMethods.button("Reset To Start Angle", width/2, 30, 300, 50);

        frame.add(panel);

    //Main Frame Properties--------------------------------------------------------------------------------------------------------------------------
        frame.setIconImage(PIDMethods.imageURL("https://github.com/CrazyMeowCows/Images/blob/main/AdambotsLogoBlack.png?raw=true"));
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);  

    //Action Listeners-------------------------------------------------------------------------------------------------------------------------------
        Action pidInputs = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                try {
                    pGain = Double.parseDouble(pText.getText());
                    iGain = Double.parseDouble(iText.getText());
                    dGain = Double.parseDouble(dText.getText());
                } catch (NumberFormatException value) {
                    System.out.println("Invalid Entry");
                }
            }
        };
        pText.addActionListener(pidInputs);
        iText.addActionListener(pidInputs);
        dText.addActionListener(pidInputs);

        Action maxInput = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                try {
                    motorMax = Float.parseFloat(maxPwr.getText());
                } catch (NumberFormatException value) {
                    System.out.println("Invalid Entry");
                }
            }
        };
        maxPwr.addActionListener(maxInput);

        Action clicked = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                try {
                    angle = Math.toRadians(Double.parseDouble(rAngle.getText()));
                    velocity = 0;
                    acceleration = 0;
                } catch (NumberFormatException value) {
                    System.out.println("Invalid Entry");
                }
            }
        };
        reset.addActionListener(clicked);

    //Timer------------------------------------------------------------------------------------------------------------------------------------------
        time = System.currentTimeMillis();
        Timer timer = new Timer(timeStep, new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                Physics();
                frame.repaint();
            }
        });
        timer.start();  
    }

//Physics--------------------------------------------------------------------------------------------------------------------------------------------
    static void Physics () {
        acceleration = (Math.sin(angle)*-gravity)/length;
        motorOut = Math.min(motorMax, motorOut);
        acceleration += motorOut;

        velocity += acceleration;
        velocity = velocity*(1-friction);
        angle += Math.toRadians(velocity);
        if(angle > rotation){angle -= rotation;}
        if(angle < -rotation){angle += rotation;}

        pendulumX = (int)Math.round(pivotX+Math.sin(angle)*length);
        pendulumY = (int)Math.round(pivotY+Math.cos(angle)*length);
    }

//Drawing--------------------------------------------------------------------------------------------------------------------------------------------
    static class DrawingManager extends JPanel {
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;

            setBackground(new Color(100, 100, 100));

            g.setColor(new Color(255, 255, 255, 30));
            for(int i = -1*75; i <= height-pivotY; i += 75){
                g.drawLine(0, pivotY+i, width, pivotY+i);
            }
            for(int i = -10*75; i <= width-pivotX; i += 75){
                g.drawLine(pivotX+i, 0, pivotX+i, height);
            }

            g.setColor(new Color(255, 255, 255));
            g.drawLine(pivotX, pivotY, pendulumX, pendulumY);
            g.setColor(new Color(0, 0, 0));
            g.fillOval(pivotX-10/2, pivotY-10/2, 10, 10);

            PIDMethods.drawRotatedImage(g2d, pendulum, angle, pendulumX, pendulumY);

            g.setFont(font);
            g.setColor(new Color(255, 255, 255));
            g.drawString("Angle: " + PIDMethods.round(Math.toDegrees(angle)), width-150, 25);
            g.drawString("Velocity: " + PIDMethods.round(velocity), width-150, 55);
            g.drawString("Accel.: " + PIDMethods.round(acceleration), width-150, 85);
            g.drawString("Motor: " + PIDMethods.round(motorOut), width-150, 115);

            try {
                g.drawString("FPS: " + 1000/(System.currentTimeMillis()-time), 10, height-10);
            } catch (ArithmeticException value) {}
            time = System.currentTimeMillis();
        }
    }
}
