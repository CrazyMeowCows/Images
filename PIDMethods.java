//Imports--------------------------------------------------------------------------------------------------------------------------------------------
import java.awt.*;
import javax.swing.*;
import java.net.URL;
import javax.imageio.ImageIO;
import java.io.IOException;
import java.awt.geom.AffineTransform;

class PIDMethods{  
//Round With 3 decimal places------------------------------------------------------------------------------------------------------------------------
    public static double round(double num){  
        return (double)Math.round(num*1000)/1000;
    }  

//Creating and returning JLabels----------------------------------------------------------------------------------------------------------------------
    public static JLabel label(String name, int x, int y){  
        JLabel label = new JLabel(name);
        label.setFont(PIDSimulator.font);
        label.setForeground(Color.WHITE);
        label.setBounds(x, y, 150, 25);

        PIDSimulator.panel.add(label);
        return label;
    }  

//Creating and returning JTextFields------------------------------------------------------------------------------------------------------------------
    public static JTextField text(String value, int x, int y){  
        JTextField text = new JTextField(value);
        text.setFont(PIDSimulator.font);
        text.setBounds(x, y, 80, 25);

        PIDSimulator.panel.add(text);
        return text;
    }  

//Creating and returning JButtons---------------------------------------------------------------------------------------------------------------------
    public static JButton button(String name, int x, int y, int w, int h){  
        JButton button = new JButton(name);
        button.setFont(PIDSimulator.font);
        button.setBounds(x-w/2, y-h/2, w, h);
        button.setForeground(Color.BLACK);
        button.setBackground(Color.WHITE);
        button.setFocusPainted(false);

        PIDSimulator.panel.add(button);
        return button;
    } 

//Getting An Image from a URL------------------------------------------------------------------------------------------------------------------------
    public static Image imageURL(String link){  
        Image finalImage = null;
        try {
            URL logoURL = new URL(link);
            Image icon = ImageIO.read(logoURL);  
            finalImage = icon;
        } catch(IOException ie) {
            ie.printStackTrace();
        }
        return finalImage;
    } 

//Drawing an Image with Specified Rotation-----------------------------------------------------------------------------------------------------------
    public static void drawRotatedImage(Graphics2D g2d, Image image, double angle, int x, int y){  
        AffineTransform backup = g2d.getTransform();
        AffineTransform a = AffineTransform.getRotateInstance(-angle, x, y);
        g2d.setTransform(a);
        g2d.drawImage(image, x-image.getWidth(null)/2, y-image.getHeight(null)/2, null);
        g2d.setTransform(backup);
    }  

//Drawing an Image with Specified Rotation-----------------------------------------------------------------------------------------------------------
public static void drawRotatedTextOffPendulum(Graphics2D g2d, String text, double dist){  
    int x = (int)Math.round(PIDSimulator.pivotX+Math.sin(PIDSimulator.angle)*dist)+10;
    int y = (int)Math.round(PIDSimulator.pivotY+Math.cos(PIDSimulator.angle)*dist);

    g2d.setFont(PIDSimulator.font);
    g2d.setColor(Color.white);

    AffineTransform backup = g2d.getTransform();
    AffineTransform a = AffineTransform.getRotateInstance(-PIDSimulator.angle, x, y);
    g2d.setTransform(a);
    g2d.drawString(text, x, y);
    g2d.setTransform(backup);
}  

//Drawing an Image with Specified Rotation-----------------------------------------------------------------------------------------------------------
    public static void drawArrowOffPendulum(double length, double dist, Graphics g){  
        int x1 = (int)Math.round(PIDSimulator.pivotX+Math.sin(PIDSimulator.angle)*dist);
        int y1 = (int)Math.round(PIDSimulator.pivotY+Math.cos(PIDSimulator.angle)*dist);
        int x2 = (int)Math.round(x1+Math.cos(PIDSimulator.angle)*length);
        int y2 = (int)Math.round(y1-Math.sin(PIDSimulator.angle)*length);

        g.setColor(new Color(255, 255, 255));
        g.drawLine(x1, y1, x2, y2);
        g.fillOval(x2-5/2, y2-5/2, 5, 5);
    }  
}