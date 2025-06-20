package org.proteovir.gui.components;
import javax.swing.*;

import ai.nets.samj.gui.LoadingButton;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.CodeSource;
import java.security.ProtectionDomain;

/**
 * A JButton subclass with solid background colors for disabled, selected, and unselected states,
 * and an animated GIF displayed at the right edge while keeping the text centered.
 */
public class ColoredButton extends JButton {
    private static final long serialVersionUID = 1936762451363129150L;
	private final Color selectedEnabledColor;
    private final Color unselectedEnabledColor;
    private final Color selectedDisabledColor;
    private final Color unselectedDisabledColor;
    private final Icon animationIcon;
    
    private static final int ICON_GAP = 5;
    private static final int ICON_SIZE = 20;
    private static final String ICON_PATH = "icons_samj/loading_animation_samj.gif";

    /**
     * @param text            the button text
     * @param selectedEnabledColor background color when not selected
     * @param unselectedEnabledColor   background color when selected
     * @param selectedDisabledColor   background color when disabled
     * @param unselectedDisabledColor   background color when disabled
     */
    public ColoredButton(String text,
            				Color selectedEnabledColor,
                            Color unselectedEnabledColor,
                            Color selectedDisabledColor,
                            Color unselectedDisabledColor) {
        super(text);
        this.selectedEnabledColor = selectedEnabledColor;
        this.unselectedEnabledColor = unselectedEnabledColor;
        this.selectedDisabledColor = selectedDisabledColor;
        this.unselectedDisabledColor = unselectedDisabledColor;
        this.animationIcon = getIcon(ICON_PATH, ICON_SIZE);

        // Disable default background painting
        setContentAreaFilled(false);
        setOpaque(false);
        setBorderPainted(true);
        setFocusPainted(true);

        // Toggle selection state on click
        addActionListener((ActionEvent e) -> {
            getModel().setSelected(!getModel().isSelected());
            repaint();
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                            RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // Determine background color based on state
        Color bg;
        if (!isEnabled() && !getModel().isSelected()) {
            bg = unselectedDisabledColor;
        } else if (isEnabled() && !getModel().isSelected()) {
            bg = unselectedEnabledColor;
        } else if (!isEnabled()) {
            bg = selectedDisabledColor;
        } else {
            bg = selectedEnabledColor;
        }
        
        Color fg;
        if (!isEnabled())
        	fg = new Color(200, 200, 200);
        else
        	fg = new Color(0, 0, 0);

        // Fill background
        g2.setColor(bg);
        g2.fillRect(0, 0, getWidth(), getHeight());

        // Draw centered text
        String text = getText();
        FontMetrics fm = g2.getFontMetrics();
        int textWidth = fm.stringWidth(text);
        int textHeight = fm.getAscent();
        int xText = (getWidth() - textWidth) / 2;
        int yText = (getHeight() + textHeight) / 2 - fm.getDescent();
        g2.setColor(fg);
        g2.drawString(text, xText, yText);

        // Draw animation icon on the right
        if (animationIcon != null) {
            int iconWidth = animationIcon.getIconWidth();
            int iconHeight = animationIcon.getIconHeight();
            int xIcon = getWidth() - iconWidth - ICON_GAP;
            int yIcon = (getHeight() - iconHeight) / 2;
            animationIcon.paintIcon(this, g2, xIcon, yIcon);
        }

        g2.dispose();
        // Paint border if any
        super.paintBorder(g);
    }
    
	private ImageIcon getIcon(String path, int smallestSide) {
		while (path.indexOf("//") != -1) path = path.replace("//", "/");
		URL url = LoadingButton.class.getClassLoader().getResource(path);
		if (url == null) {
			File f = findJarFile(LoadingButton.class);
			if (f.getName().endsWith(".jar")) {
				try (URLClassLoader clsloader = new URLClassLoader(new URL[]{f.toURI().toURL()})){
					url = clsloader.getResource(path);
				} catch (IOException e) {
				}
			}
		}
		if (url != null) {
			ImageIcon icon = new ImageIcon(url);
			int width = icon.getIconWidth();
			int height = icon.getIconHeight();
			int min = Math.min(width, height);
			double scale = (double) smallestSide / (double) min;
			icon.setImage(icon.getImage().getScaledInstance((int) (width * scale), (int) (height * scale), Image.SCALE_DEFAULT));
			return icon;
		}
		return null;
	}
	
	private static File findJarFile(Class<?> clazz) {
        ProtectionDomain protectionDomain = clazz.getProtectionDomain();
        if (protectionDomain != null) {
            CodeSource codeSource = protectionDomain.getCodeSource();
            if (codeSource != null) {
                URL location = codeSource.getLocation();
                if (location != null) {
                    try {
                        return new File(URI.create(location.toURI().getSchemeSpecificPart()).getPath());
                    } catch (URISyntaxException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return null;
    }
}
