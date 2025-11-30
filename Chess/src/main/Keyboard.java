package main;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class Keyboard extends KeyAdapter {

    @Override
    public void keyPressed(KeyEvent e){
        int code=e.getKeyCode();
        if(code ==KeyEvent.VK_R){
            if(e.getSource() instanceof GamePanel){
                ((GamePanel)e.getSource()).ResetGame();
            }
        }
    }
}