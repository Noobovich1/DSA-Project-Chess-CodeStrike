package main;

import java.awt.Dimension;
import javax.swing.JFrame;

public class main {
    public static void main(String[] args) {
        JFrame window = new JFrame("Code Strike Chess");  
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); //Closing the window will also force closing the program
        //enable resize
        window.setResizable(true); // pretty straight forward for what this does

        //Adding GamePanel to the window
        GamePanel gp = new GamePanel();
        gp.setPreferredSize(new Dimension(1200, 800));

        window.add(gp);
        window.pack();
        window.setLocationRelativeTo(null); // window will appear at the centre of the screen not top left
        window.setVisible(true);

        gp.launch();
    }
}
