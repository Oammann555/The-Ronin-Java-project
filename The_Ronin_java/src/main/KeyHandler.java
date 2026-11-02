package main;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class KeyHandler implements KeyListener {

    // volatile: ถูกเขียนจาก EDT แต่อ่านจาก game thread
    public volatile boolean wPressed, aPressed, sPressed, dPressed;
    public volatile boolean ePressed;   // เก็บอาวุธ (one-shot)
    public volatile boolean rPressed;   // เริ่มใหม่ (one-shot)

    @Override public void keyTyped(KeyEvent e) { }

    @Override
    public void keyPressed(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_W -> wPressed = true;
            case KeyEvent.VK_A -> aPressed = true;
            case KeyEvent.VK_S -> sPressed = true;
            case KeyEvent.VK_D -> dPressed = true;
            case KeyEvent.VK_E -> ePressed = true;
            case KeyEvent.VK_R -> rPressed = true;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_W -> wPressed = false;
            case KeyEvent.VK_A -> aPressed = false;
            case KeyEvent.VK_S -> sPressed = false;
            case KeyEvent.VK_D -> dPressed = false;
        }
    }
}