package org.proteovir.gui.components;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

/**
 * A JButton subclass with solid background colors for disabled, selected, and unselected states,
 * and an animated GIF displayed at the right edge while keeping the text centered.
 */
public class SolidColorButton extends JButton {
    private static final long serialVersionUID = 1936762451363129150L;
	private final Color unselectedColor;
    private final Color selectedColor;
    private final Color disabledColor;
    private final Icon animationIcon;
    private static final int ICON_GAP = 5;

    /**
     * @param text            the button text
     * @param animationIcon   an Icon (e.g., an animated GIF) to display on the right
     * @param unselectedColor background color when not selected
     * @param selectedColor   background color when selected
     * @param disabledColor   background color when disabled
     */
    public SolidColorButton(String text,
                            Icon animationIcon,
                            Color unselectedColor,
                            Color selectedColor,
                            Color disabledColor) {
        super(text);
        this.unselectedColor = unselectedColor;
        this.selectedColor = selectedColor;
        this.disabledColor = disabledColor;
        this.animationIcon = animationIcon;

        // Disable default background painting
        setContentAreaFilled(false);
        setOpaque(false);
        setBorderPainted(false);
        setFocusPainted(false);

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
        if (!isEnabled()) {
            bg = disabledColor;
        } else if (getModel().isSelected()) {
            bg = selectedColor;
        } else {
            bg = unselectedColor;
        }

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
        g2.setColor(getForeground());
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
}
