package de.htw.mp.model;

import java.awt.Color;
import java.awt.image.BufferedImage;


/**
 * Simple POJO (Plain old Java object) for holding precalculated results.
 * 
 * @author Nico Hezel
 */
public class FeatureContainer {

	protected String name;
	protected String category;
	protected Color meanColor;
	protected BufferedImage meanImage;
	
	public FeatureContainer(String name, String category, Color meanColor, BufferedImage meanImage) {
		this.name = name;
		this.category = category;
		this.meanColor = meanColor;
		this.meanImage = meanImage;
	}

	public String getName() {
		return name;
	}
	
	public String getCategory() {
		return category;
	}

	public Color getMeanColor() {
		return meanColor;
	}

	public BufferedImage getMeanImage() {
		return meanImage;
	}
}
