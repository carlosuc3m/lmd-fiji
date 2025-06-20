package org.proteovir.gui.components;

import javax.swing.*;
import javax.swing.event.EventListenerList;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * A custom button component built on a JPanel for absolute rendering control.
 * This avoids all conflicts with JButton's Look-and-Feel UI delegate, guaranteeing
 * that the custom background colors and GIF animation will render correctly.
 * It manually implements button-like behavior (ActionListeners, click-state).
 */
public class ColoredButton extends JPanel {
    private static final long serialVersionUID = 1L;

    // --- State Flags ---
    private boolean selected = false;
    private boolean loading = false;
    private boolean isPressed = false; // For visual click feedback

    // --- Animation & Icon ---
    private final Timer animationTimer;
    private final ImageIcon animationIcon;

    // --- Configuration ---
    private String text;
    private final Color selectedEnabledColor;
    private final Color unselectedEnabledColor;
    private final Color selectedDisabledColor;
    private final Color unselectedDisabledColor;

    // --- For Firing ActionEvents ---
    private final EventListenerList listenerList = new EventListenerList();

    private static final String ICON_PATH = "icons_samj/loading_animation_samj.gif";

    public ColoredButton(String text,
                          Color selectedEnabledColor,
                          Color unselectedEnabledColor,
                          Color selectedDisabledColor,
                          Color unselectedDisabledColor) {
        this.text = text;
        this.selectedEnabledColor = selectedEnabledColor;
        this.unselectedEnabledColor = unselectedEnabledColor;
        this.selectedDisabledColor = selectedDisabledColor;
        this.unselectedDisabledColor = unselectedDisabledColor;

        // Synchronously load the icon to prevent race conditions.
        this.animationIcon = getIcon(ICON_PATH);
        if (this.animationIcon == null) {
            System.err.println("FATAL: Could not load animation icon: " + ICON_PATH);
        }

        // The reliable animation driver.
        this.animationTimer = new Timer(1000 / 15, e -> repaint()); // 15 FPS
        this.animationTimer.setCoalesce(true);

        // Manually add button behavior with a MouseListener.
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (isEnabled()) {
                    isPressed = true;
                    repaint();
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (isEnabled()) {
                    isPressed = false;
                    // Fire the action event only if the click completes inside the button
                    if (contains(e.getPoint())) {
                        fireActionPerformed(new ActionEvent(ColoredButton.this, ActionEvent.ACTION_PERFORMED, ""));
                    }
                    repaint();
                }
            }
        });

        // Set initial state
        setOpaque(false);
        setFocusable(true); // Allow the button to receive focus
        setBorder(BorderFactory.createLineBorder(Color.black));
        setFont(new Font("Default", Font.BOLD, 12));
    }
    
    // --- Public methods to control state, mimicking JButton ---

    public void setText(String text) {
        this.text = text;
        repaint();
    }
    
    public String getText() {
        return this.text;
    }

    public void setLoading(boolean isLoading) {
        if (this.loading == isLoading) return;
        this.loading = isLoading;
        if (isLoading) {
            animationTimer.start();
        } else {
            animationTimer.stop();
        }
        repaint();
    }
    
    public void setSelected(boolean isSelected) {
        if (this.selected == isSelected) return;
        this.selected = isSelected;
        repaint();
    }
    
    public boolean isSelected() {
        return this.selected;
    }
    
    // --- Action Listener implementation, mimicking JButton ---
    
    public void addActionListener(ActionListener l) {
        listenerList.add(ActionListener.class, l);
    }
    
    public void removeActionListener(ActionListener l) {
        listenerList.remove(ActionListener.class, l);
    }
    
    protected void fireActionPerformed(ActionEvent event) {
        Object[] listeners = listenerList.getListenerList();
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == ActionListener.class) {
                ((ActionListener) listeners[i + 1]).actionPerformed(event);
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        // Start with a clean slate. super.paintComponent is good practice for JPanels.
        super.paintComponent(g);
        
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        // 1. Determine background color based on our own state flags.
        Color bgColor;
        if (isEnabled()) {
            bgColor = selected ? selectedEnabledColor : unselectedEnabledColor;
            // Add visual feedback for click
            if (isPressed) {
                bgColor = bgColor.darker();
            }
        } else {
            bgColor = selected ? selectedDisabledColor : unselectedDisabledColor;
        }
        g2.setColor(bgColor);
        g2.fillRect(0, 0, getWidth(), getHeight());
        
        // 2. Calculate layout for icon and text
        FontMetrics fm = g2.getFontMetrics();
        int textWidth = fm.stringWidth(this.text);
        int textHeight = fm.getHeight();
        Font font = fm.getFont();
        if (textWidth > getWidth() || textHeight > getHeight()) {
        	while (textWidth + 10 > getWidth() || textHeight + 10 > textHeight) {
        		float size = fm.getFont().getSize2D() - 0.5f;
        		if (size < 0)
        			break;
        		font = fm.getFont().deriveFont(size);
        		g2.setFont(font);
        		fm = g2.getFontMetrics();
                textWidth = fm.stringWidth(this.text);
                textHeight = fm.getHeight();
        	}
        } else {
        	while (textWidth + 10 < getWidth() && textHeight + 10 < getHeight()) {
        		float size = fm.getFont().getSize2D() + 0.5f;
        		font = fm.getFont().deriveFont(size);
        		g2.setFont(font);
        		fm = g2.getFontMetrics();
                textWidth = fm.stringWidth(this.text);
                textHeight = fm.getHeight();
        	}
        }
        int iconWidth = 0, iconHeight = 0, gap = 0;

        if (loading && animationIcon != null) {
            iconHeight = Math.max(1, getHeight() - 8);
            double aspectRatio = (double) animationIcon.getIconWidth() / animationIcon.getIconHeight();
            iconWidth = (int) (iconHeight * aspectRatio);
            gap = 5;
        }
        
        int startX = (getWidth() - textWidth) / 2 - gap - iconWidth;
        
        // 3. Draw the animated icon
        if (loading && animationIcon != null) {
            int iconX = startX;
            int iconY = (getHeight() - iconHeight) / 2;
            g2.drawImage(animationIcon.getImage(), iconX, iconY, iconWidth, iconHeight, this);
        }
        
        // 4. Draw the text
        int textX = startX + iconWidth + gap;
        int textY = (getHeight() - fm.getHeight()) / 2 + fm.getAscent() ;
        g2.setColor(isEnabled() ? getForeground() : Color.GRAY);
        g2.setFont(fm.getFont());
        g2.drawString(this.text, textX, textY);
        
        g2.dispose();
    }

    private ImageIcon getIcon(String path) {
        if (path == null || path.isEmpty()) return null;
        URL url = getClass().getClassLoader().getResource(path);
        if (url == null) {
            System.err.println("Resource not found in classpath: " + path);
            return null;
        }
        try (InputStream in = url.openStream()) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buffer = new byte[4096];
            int n;
            while ((n = in.read(buffer)) > 0) {
                baos.write(buffer, 0, n);
            }
            return new ImageIcon(baos.toByteArray());
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    // ===================================================================================
    //  MAIN METHOD FOR STANDALONE PROOF OF CONCEPT
    // ===================================================================================
    public static void main(String[] args) {
        // --- Instructions ---
        // 1. Compile this class.
        // 2. Ensure you have a folder named 'icons_samj' in your classpath root (e.g., 'bin' or 'resources').
        // 3. Place 'loading_animation_samj.gif' inside that folder.
        // 4. Run this main method.
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("AnimatedButton Test (JPanel-based)");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 20));
            frame.getContentPane().setBackground(Color.DARK_GRAY);

            Color selEnabled = new Color(180, 220, 255);
            Color unselEnabled = new Color(220, 220, 220);
            Color selDisabled = new Color(210, 230, 245);
            Color unselDisabled = new Color(240, 240, 240);

            ColoredButton button = new ColoredButton("Click Me", selEnabled, unselEnabled, selDisabled, unselDisabled);
            button.setPreferredSize(new Dimension(250, 60));
            button.setForeground(Color.BLACK);
            button.setFont(new Font("SansSerif", Font.BOLD, 16));

            // Add a standard ActionListener to prove it works like a button
            button.addActionListener(e -> {
                System.out.println("Button clicked! Toggling selection and starting load...");
                button.setSelected(!button.isSelected());

                // Simulate a 4-second loading process
                button.setLoading(true);
                button.setEnabled(false);
                button.setText("Loading, please wait...");

                new Timer(4000, event -> {
                    button.setLoading(false);
                    button.setEnabled(true);
                    button.setText("Click Me");
                }) {{ setRepeats(false); }}.start();
            });

            frame.add(button);
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}