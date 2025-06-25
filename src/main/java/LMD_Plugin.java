import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.SwingUtilities;

import org.proteovir.gui.SidePanel;

import ij.ImageJ;
import ij.plugin.PlugIn;

public class LMD_Plugin implements PlugIn {
	
	public static void main(String[] args) {
		new ImageJ();
		new LMD_Plugin().run("");
	}

	@Override
	public void run(String arg) {
        SwingUtilities.invokeLater(new Runnable() {
        	public void run() {
            	ij.plugin.frame.PlugInFrame frame = new ij.plugin.frame.PlugInFrame("Proteovir LMD Segmentator");
            	SidePanel gui = new SidePanel(null);
                frame.add(gui);
                frame.pack();
                frame.setSize(getInitialDims());
                frame.setLocationRelativeTo(null);
                frame.setVisible(true);
                frame.addWindowListener(new WindowAdapter() {
                    @Override
                    public void windowClosed(WindowEvent e) {
                    	gui.close();
                    }
                });
    	    	}
        	});
	}
	
	private static Dimension getInitialDims() {
		Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        int screenW = screen.width;
        int screenH = screen.height;
        int w = screenW;
        int h = w * 3 / 4;
        if (h > screenH) {
            h = screenH;
            w = h * 4 / 3;
        }
        //return new Dimension((int) (w * 0.8d), (int) (h * 0.8));
        return new Dimension((int) (w * 3 * 0.8d / 5), (int) (h * 0.8));
	}
}
