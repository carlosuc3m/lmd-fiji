package org.proteovir.gui;

import org.proteovir.roimanager.ConsumerInterface;

/**
 * A simple Swing application demonstrating a 4:3 window with:
 * - A square canvas on the left that fills the full height
 * - A vertical column of buttons on the right
 */
public class MainPanel extends MainPanelGUI {

    private static final long serialVersionUID = 8550100052731297639L;

	public MainPanel(ConsumerInterface consumer) {
		super(consumer);
    }

	public void close() {
		// TODO Auto-generated method stub
		
	}

	public void setCancelCallback(Runnable runnable) {
		// TODO Auto-generated method stub
		
	}
}
