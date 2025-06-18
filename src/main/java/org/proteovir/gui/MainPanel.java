package org.proteovir.gui;


import javax.swing.*;
import java.awt.*;

/**
 * A simple Swing application demonstrating a 4:3 window with:
 * - A square canvas on the left that fills the full height
 * - A vertical column of buttons on the right
 */
public class MainPanel extends JPanel {
    private static final int FRAME_WIDTH = 800;  // 4 units
    private static final int FRAME_HEIGHT = 600; // 3 units (4:3 aspect)

    public MainPanel() {
        // Set preferred size to maintain 4:3 aspect ratio
        setPreferredSize(new Dimension(FRAME_WIDTH, FRAME_HEIGHT));

        // Use BorderLayout to place canvas on the left and buttons on the right
        setLayout(new BorderLayout());

        // Square canvas panel
        JPanel canvasPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                // Example drawing: fill background
                g.setColor(Color.LIGHT_GRAY);
                g.fillRect(0, 0, getWidth(), getHeight());
                // You can add more custom drawing here
            }
        };
        // Make width == height to stay square
        canvasPanel.setPreferredSize(new Dimension(FRAME_HEIGHT, FRAME_HEIGHT));
        add(canvasPanel, BorderLayout.WEST);

        // Button panel on the right
        JPanel buttonPanel = new JPanel(new GridLayout(0, 1, 10, 10));
        // Remaining width for buttons
        buttonPanel.setPreferredSize(new Dimension(FRAME_WIDTH - FRAME_HEIGHT, FRAME_HEIGHT));
        // Add several buttons
        for (int i = 1; i <= 5; i++) {
            buttonPanel.add(new JButton("Button " + i));
        }
        add(buttonPanel, BorderLayout.CENTER);
        setVisible(true);
    }

    public static void main(String[] args) {
    }

	public void close() {
		// TODO Auto-generated method stub
		
	}

	public void setCancelCallback(Runnable runnable) {
		// TODO Auto-generated method stub
		
	}
}
