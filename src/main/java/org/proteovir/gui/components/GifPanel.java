package org.proteovir.gui.components;

import javax.swing.*;

import ai.nets.samj.gui.LoadingButton;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;

/**
 * A simple JPanel that displays an animated ImageIcon (e.g. a GIF)
 * and repaints itself on a fixed interval so the animation keeps running.
 */
public class GifPanel extends JPanel {
    private static final long serialVersionUID = 1L;
    private final ImageIcon gifIcon;
    private final Timer repaintTimer;

    /**
     * @param gifIcon the ImageIcon (animated GIF) to display
     * @param fps     how many frames-per-second to repaint at (e.g. 10–20 is usually fine)
     */
    public GifPanel(ImageIcon gifIcon, int fps) {
        if (gifIcon == null) throw new IllegalArgumentException("gifIcon must not be null");
        if (fps <= 0)      throw new IllegalArgumentException("fps must be >0");

        this.gifIcon = gifIcon;
        // set panel to preferred size matching the icon
        setPreferredSize(new Dimension(gifIcon.getIconWidth(), gifIcon.getIconHeight()));

        // Timer will fire every 1000/fps ms and call repaint()
        int delay = 1000 / fps;
        this.repaintTimer = new Timer(delay, e -> repaint());
        this.repaintTimer.setCoalesce(true);
        this.repaintTimer.start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        // center the icon
        int x = (getWidth()  - gifIcon.getIconWidth())  / 2;
        int y = (getHeight() - gifIcon.getIconHeight()) / 2;
        // paintIcon will register this panel as the ImageObserver, so frames advance
        gifIcon.paintIcon(this, g, x, y);
    }

    /** Call this to stop the animation when you no longer need it. */
    public void stopAnimation() {
        repaintTimer.stop();
    }

    /** Call this to restart the animation if it was stopped. */
    public void startAnimation() {
        if (!repaintTimer.isRunning()) {
            repaintTimer.start();
        }
    }
    
    public static void main(String[] args) {
    	ImageIcon spinner = getIcon("icons_samj/loading_animation_samj.gif");

    	// create the panel at, say, 15 FPS
    	GifPanel gifPanel = new GifPanel(spinner, 15);

    	// add it to your UI wherever you need the spinner:
    	JFrame frame = new JFrame("Demo");
    	frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    	frame.getContentPane().add(gifPanel);
    	frame.pack();
    	frame.setLocationRelativeTo(null);
    	frame.setVisible(true);
    }
	
	private static ImageIcon getIcon(String path) {
		while (path.indexOf("//") != -1) path = path.replace("//", "/");
		URL url = LoadingButton.class.getClassLoader().getResource(path);
		if (url != null) {
			ImageIcon icon = new ImageIcon(url);
			return icon;
		}
		return null;
	}
}
