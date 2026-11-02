package main;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class MouseHandler extends MouseAdapter {

    public volatile boolean leftClicked;
    public volatile boolean blocking;
    public volatile long blockPressedAt = 0;

    public volatile int mouseX, mouseY;

    @Override
    public void mousePressed(MouseEvent e) {
        mouseX = e.getX();
        mouseY = e.getY();
        if (e.getButton() == MouseEvent.BUTTON1) leftClicked = true;
        if (e.getButton() == MouseEvent.BUTTON3) {
            blocking = true;
            long now = System.currentTimeMillis();
            if (now - blockPressedAt >= GameConfig.PARRY_COOLDOWN_MS) blockPressedAt = now;
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON3) blocking = false;
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        mouseX = e.getX();
        mouseY = e.getY();
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        mouseX = e.getX();
        mouseY = e.getY();
    }
}
