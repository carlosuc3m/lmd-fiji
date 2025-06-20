package org.proteovir.gui;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;

import javax.swing.JTextField;

public class PlaceholderTextField extends JTextField {
    private static final long serialVersionUID = 5112778641734509160L;
	private String placeholder;
    private Color placeholderColor;

    protected PlaceholderTextField(String placeholder) {
        this.placeholder = placeholder;
        this.placeholderColor = Color.LIGHT_GRAY;
    }
    
    public void setTempPlaceholder(String text) {
    	String   oldText  = placeholder;
        Color    oldColor = placeholderColor;
        try {
            placeholder      = text;
            placeholderColor = new Color(255, 191, 191);
            setText("");
            // force an immediate paint on the EDT using the *current* width/height
            paintImmediately(0, 0, getWidth(), getHeight());
        } finally {
            // restore state immediately after painting
            placeholder      = oldText;
            placeholderColor = oldColor;
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (getText().isEmpty()) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setColor(placeholderColor);
            Insets ins = getInsets();
            FontMetrics fm = g2.getFontMetrics();
            int x = ins.left;
            // Vertically center the text:
            int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
            g2.drawString(placeholder, x, y);
            g2.dispose();
        }
    }
}
