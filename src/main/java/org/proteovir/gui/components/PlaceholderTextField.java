package org.proteovir.gui.components;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

import javax.swing.JTextField;

public class PlaceholderTextField extends JTextField {
    private static final long serialVersionUID = 5112778641734509160L;
	private String placeholder;
    private Color placeholderColor;

    public PlaceholderTextField(String placeholder) {
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

    /**
     * Override paste so that if we're currently "empty" (showing placeholder),
     * we first clear, then let the normal JTextField.paste() insert the clipboard text.
     */
    @Override
    public void paste() {
        // if empty (i.e. placeholder showing), clear so paste goes into the real content
        String pasteString = getClipboardString();
        if (pasteString == null)
        	return;
        setText(pasteString);
    }
    
    public static String getClipboardString() {
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        try {
            if (clipboard.isDataFlavorAvailable(DataFlavor.stringFlavor)) {
                return (String) clipboard.getData(DataFlavor.stringFlavor);
            }
        } catch (UnsupportedFlavorException | IOException ex) {
            ex.printStackTrace();
        }
        return null;
    }
}
