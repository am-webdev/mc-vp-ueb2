package de.htw.mp.ui.component;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;

import javax.swing.JComponent;

/**
 * @author Nico Hezel
 */
public class ColorView extends JComponent {

	private static final long serialVersionUID = 3017702760660613243L;

	private Color color = null;
	
	//
	// painting
	//
	@Override
	public void paintComponent(Graphics g) {		
		Dimension displaySize = this.getSize();	// display size
		
		// background color
		g.setColor(color);
		g.fillRect(0, 0, displaySize.width, displaySize.height);
	}
	
	/**
	 * Change the image
	 * 
	 * @param image
	 */
	public void setColor(Color color) {
		this.color = color;
		
		// redraw the component
		invalidate();
		repaint();
	}
}
