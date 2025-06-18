package org.proteovir.gui;

import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

import javax.swing.JPanel;

/**
 * A simple Swing application demonstrating a 4:3 window with:
 * - A square canvas on the left that fills the full height
 * - A vertical column of buttons on the right
 */
public class SidePanelGUI extends JPanel {

    private static final long serialVersionUID = -8405747451234902128L;

	public SidePanelGUI() {
        
        organiseComponents();
    }
    
    private void organiseComponents() {
    	addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
            }
        });
    }

	public void close() {
		// TODO Auto-generated method stub
		
	}

	public void setCancelCallback(Runnable runnable) {
		// TODO Auto-generated method stub
		
	}
}
