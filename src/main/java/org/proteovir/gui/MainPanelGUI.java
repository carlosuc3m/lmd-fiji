package org.proteovir.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

import javax.swing.JPanel;

/**
 * A simple Swing application demonstrating a 4:3 window with:
 * - A square canvas on the left that fills the full height
 * - A vertical column of buttons on the right
 */
public class MainPanelGUI extends JPanel {
    private static final long serialVersionUID = -2838447707927014616L;
    
    
    JPanel canvasPanel;
    
    SidePanelGUI sidePanel;
    
    public static final double W_H_RATIO = 4d / 3d;
    
    private static final int MINIMUM_WIDTH = 40;

    public MainPanelGUI() {

        // Use BorderLayout to place canvas on the left and buttons on the right
        setLayout(null);

        // Square canvas panel
        canvasPanel = new JPanel() {
            private static final long serialVersionUID = -7553678106776829518L;

			@Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                // Example drawing: fill background
                g.setColor(Color.LIGHT_GRAY);
                g.fillRect(0, 0, getWidth(), getHeight());
                // You can add more custom drawing here
            }
        };

        setMinimumSize(new Dimension(MINIMUM_WIDTH, (int) (MINIMUM_WIDTH / W_H_RATIO)));

        add(canvasPanel);
        add(sidePanel);
        
        organiseComponents();
    }
    
    private void organiseComponents() {
    	addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                int rawW = getWidth();
                int rawH = getHeight();
                
                int canvasW = (int) (rawW * 3 / 4);
                canvasW = Math.max(1, canvasW);
                
                int sideW = Math.max(0, rawW - canvasW);

                canvasPanel.setBounds(0, 0, sideW, rawH);
                sidePanel.setBounds(sideW, 0, canvasW, rawH);
            }
        });
    }
}
