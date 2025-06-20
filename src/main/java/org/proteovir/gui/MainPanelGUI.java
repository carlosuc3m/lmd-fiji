package org.proteovir.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

import javax.swing.JPanel;

import org.proteovir.roimanager.RoiManagerConsumer;


/**
 * A simple Swing application demonstrating a 4:3 window with:
 * - A square canvas on the left that fills the full height
 * - A vertical column of buttons on the right
 */
public class MainPanelGUI extends JPanel {
    private static final long serialVersionUID = -2838447707927014616L;
    
    
    JPanel canvasPanel;
    
    SidePanel sidePanel;
    
    public static final double W_H_RATIO = 5d / 3d;
    
    private static final int MINIMUM_WIDTH = 40;

    public MainPanelGUI(RoiManagerConsumer consumer) {

        // Use BorderLayout to place canvas on the left and buttons on the right
        setLayout(null);

        // Square canvas panel
        canvasPanel = new JPanel(null) {
            private static final long serialVersionUID = -2243479840349844620L;

			@Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                // just paint a gray bg if nothing in it
                if (getComponentCount() == 0) {
                    g.setColor(Color.LIGHT_GRAY);
                    g.fillRect(0, 0, getWidth(), getHeight());
                }
            }
        };
        
        sidePanel = new SidePanel(consumer);

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
                
                int canvasW = (int) (rawW / W_H_RATIO);
                canvasW = Math.max(1, canvasW);
                
                int sideW = Math.max(0, rawW - canvasW);

                canvasPanel.setBounds(0, 0, canvasW, rawH);
                sidePanel.setBounds(canvasW, 0, sideW, rawH);
            }
        });
    }
}
