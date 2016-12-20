package de.htw.mp.ui;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import javax.imageio.ImageIO;

import de.htw.mp.model.FeatureContainer;

/**
 * Simple data set viewer. Categorizes and lists all image files in a directory.
 * The UI provides an image viewer and mean color calculation.
 * 
 * @author Nico Hezel
 */
public class DatasetViewer extends DatasetViewerBase {
	
	private static final long serialVersionUID = -6288314471660252417L;

	/**
	 * Calculate the mean color of all given images. Or return PINK if there are no images.
	 * 
	 * @param imageFiles
	 * @return
	 */
	public Color getMeanColor(File ... imageFiles) {
		int avrRed = 0;
		int avrGreen = 0;
		int avrBlue = 0;
		
		if(imageFiles.length == 0) {
			return Color.PINK;	// no images? return PINK
		} else {
			for (File imgFile : imageFiles) {
				BufferedImage bufferdImg = null;
				
				int red = 0;
				int green = 0;
				int blue = 0;
				
				try {
					// Read Image from file system
					bufferdImg = ImageIO.read(imgFile);
				} catch (IOException e) {
					e.printStackTrace();
				}

				// ensure color spectrum is in a correct RGB
				bufferdImg = ensureCorrectColorSpectrum(bufferdImg);
				
				int width = bufferdImg.getWidth();
				int height = bufferdImg.getHeight();
				int[] pixels = new int[width * height];
				bufferdImg.getRGB(0, 0, width, height, pixels, 0, width);
				//System.out.println("Length:\t"+pixels.length+"\nPath:\t"+imgFile.getPath());				
				
				// sum up color per channel per pixel (pos)
				for(int y = 0; y < height; y++) { for(int x = 0; x < width; x++) {
					int pos = y * width + x;
						int rgb 	= pixels[pos];
						red 	+= (rgb >> 16) & 0xff; 
						green 	+= (rgb >> 8) & 0xff;
						blue 	+= rgb & 0xff;
					}
				}
				avrRed 		= (red 		/ (width*height));
				avrGreen 	= (green 	/ (width*height));
				avrBlue		= (blue 	/ (width*height));
				
//				System.out.println("File:\t"+imgFile.getName() +
//						"\n\tred:\t"+avrRed+
//						"\n\tgreen:\t"+avrGreen+
//						"\n\tblue:\t"+avrBlue);
			}
		}
		
		return new Color((avrRed), (avrGreen), (avrBlue));
	}
	
	/**
	 * Calculate the mean image of all given images. Or return NULL if there are no images.
	 * @param imageFiles
	 * @return
	 */
	public BufferedImage getMeanImage(File ... imageFiles) {

		BufferedImage images[] = new BufferedImage[imageFiles.length];
		List<int[]> imagePixels = new ArrayList<int[]>(); 
		for (int i = 0; i < imageFiles.length; i++) {
			BufferedImage currentImg = null;
			try {
				// Read Image from file system
				currentImg = ImageIO.read(imageFiles[i]);
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			// ensure color spectrum is in a correct RGB
			currentImg = ensureCorrectColorSpectrum(currentImg);
			images[i] = currentImg;
			int pixels[] = new int[currentImg.getHeight() * currentImg.getWidth()];
			imagePixels.add(currentImg.getRGB(0, 0, currentImg.getWidth(), currentImg.getHeight(), pixels, 0, currentImg.getWidth()));
		}
	
		// Prepare new bufferdImage that can be used to add the avr Color per pixel later on
		BufferedImage average = new BufferedImage(images[0].getWidth(), images[0].getHeight(), BufferedImage.TYPE_INT_RGB);
		int width = average.getWidth();
		int height = average.getHeight();		
		int avrPixels[] = new int[width * height];

		//calculate mean color per pixel
		for (int pixelPointer = 0; pixelPointer < avrPixels.length; pixelPointer++) {
			
			int avrRed = 0;
			int avrGreen = 0;
			int avrBlue = 0;

			int currentRed = 0;
			int currentGreen = 0;
			int currentBlue = 0;
			
			for (int i = 0; i < images.length; i++) {
				
				int crntPixels[] = imagePixels.get(i);
				
				int crntRGB = crntPixels[pixelPointer];
				int r 	= (crntRGB >> 16) & 0xff; 
				int g	= (crntRGB >> 8) & 0xff;
				int b	= (crntRGB >> 0) & 0xff;
				
				currentRed += r;
				currentGreen += g;
				currentBlue += b;
			}
			
			avrRed = currentRed/imageFiles.length;
			avrGreen = currentGreen/imageFiles.length;
			avrBlue = currentBlue/imageFiles.length;
				
			// Calculate average per pixel
			avrRed 		= preventColorOverflow(avrRed);
			avrGreen 	= preventColorOverflow(avrGreen);
			avrBlue 	= preventColorOverflow(avrBlue);
						
			avrPixels[pixelPointer] =  (avrRed << 16) | (avrGreen << 8) | avrBlue;
		}
		
		average.setRGB(0, 0, width, height, avrPixels, 0, width);
		
		return average;
	}
	
	/**
	 * Sort the elements in the database based on the similarity to the search query.
	 * The similarity will be calculated between to features. Features are are stored in
	 * the FeatureContainer and the FeatureType specifies which feature should be used.
	 *  
	 * @param query
	 * @param database
	 * @param featureType
	 * @return sorted list of database elements
	 */
	public List<FeatureContainer> retrieve(FeatureContainer query, FeatureContainer[] database, FeatureType featureType) {
		

		List<FeatureWrapper> listUnsorted = new ArrayList<FeatureWrapper>();
		
		// based on featureType, generate a list with values compared to the database
		for (FeatureContainer feature : database) {
			listUnsorted.add(new FeatureWrapper(feature, getDistanceBy(featureType, query, feature)));
		}

		// sort "list" by new Comparator
		Comparator<FeatureWrapper> vactorComparator = new Comparator<FeatureWrapper>() {				
			@Override
			public int compare(FeatureWrapper o1, FeatureWrapper o2) {
				return Double.compare(o1.featureVector, o2.featureVector);
			}
		};
		listUnsorted.sort(vactorComparator);
		
		List<FeatureContainer> sortedDatabase = new ArrayList<FeatureContainer>();
		
		for(FeatureWrapper entry : listUnsorted) {
			sortedDatabase.add(entry.getFeature());
	    }
		return sortedDatabase;
	}
	
	private Double getDistanceBy(FeatureType featureType, FeatureContainer origin, FeatureContainer current) {
		double rtn = 0d;
		switch(featureType) {
			case MeanColor:
				// calculate color difference between a and b
				rtn = getColorDistance(origin.getMeanColor(), current.getMeanColor());
				break;
			case MeanImage:
				// calculate image difference between a and b
				int pixels[] = new int[origin.getMeanImage().getHeight() * origin.getMeanImage().getWidth()];
				for (int i = 0; i < pixels.length; i++) {
					int diff = 0;
					
					int crntRGB = pixels[i];
					int crntR 	= (crntRGB >> 16) & 0xff; 
					int crntG	= (crntRGB >> 8) & 0xff;
					int crntB	= (crntRGB >> 0) & 0xff;
					
					int orgnRGB = pixels[i];
					int orgnR 	= (orgnRGB >> 16) & 0xff; 
					int orgnG	= (orgnRGB >> 8) & 0xff;
					int orgnB	= (orgnRGB >> 0) & 0xff;
					
					diff += getColorDistance(new Color(orgnR, orgnG, orgnB), new Color(crntR, crntG, crntB));
					
					rtn = diff/pixels.length;
				}
				break;
			}
		return rtn;
	}

	private double getColorDistance(Color origin, Color current) {
		int diffR = current.getRed() - origin.getRed();
		int diffG = current.getGreen() - origin.getGreen();
		int diffB = current.getBlue() - origin.getBlue();
		
		return Math.sqrt(
					diffR*diffR +
					diffG*diffG +
					diffB*diffB
				);
	}

	/**
	 * TODO Predict the category.
	 * Make the prediction based on the sorted list of features (images or categories). 
	 * 
	 * @param sortedList
	 * @param k
	 * @return predicted category
	 */
	public String classify(List<FeatureContainer> sortedList, int k) {
		
        Map<String, Integer> kmap = new TreeMap<String, Integer>();
		
		for (int i = 0; i < sortedList.size(); i++) {
			if (i > k ) {
				break;
			}
			// adds 1 to the current counter and given category
			if (kmap.containsKey(sortedList.get(i).getCategory())) {
				kmap.replace(sortedList.get(i).getCategory(), kmap.get(sortedList.get(i).getCategory())+1); 
			} else {
				// Initialize the counter with 1 if there is currently no entry for given key
				kmap.put(sortedList.get(i).getCategory(), 1); 
			}
		}
		
		return ((TreeMap<String, Integer>) kmap).lastKey();
		
		
	}
	
	private int preventColorOverflow(int singleColor) {
		int fixedColor = singleColor;
		if (singleColor > 255) {
			fixedColor = 255;
			System.out.println("Given Color("+singleColor+") greater than 255, set to 255");
		} else if(singleColor<0) {
			fixedColor = 0;
			System.out.println("Given Color("+singleColor+") smaller than 0, set to 0");
		}
		return fixedColor;
	}
	
	private BufferedImage ensureCorrectColorSpectrum(BufferedImage bufferedImage) {
		// ensure color spectrum is in a correct RGB
		if(bufferedImage.getType() != BufferedImage.TYPE_INT_RGB && bufferedImage.getType() != BufferedImage.TYPE_INT_ARGB) {
			
			BufferedImage biRGB = new BufferedImage(bufferedImage.getWidth(),
					bufferedImage.getHeight(),
					BufferedImage.TYPE_INT_RGB);
			Graphics2D g = biRGB.createGraphics();
			g.drawImage(bufferedImage, 0, 0, null);
			g.dispose();
			bufferedImage = biRGB;
		}
		return bufferedImage;
	}
	
	public class FeatureWrapper {

	    private FeatureContainer feature;
	    private double featureVector;

	    public FeatureWrapper(FeatureContainer feature, double featureVector) {
	        this.feature = feature;
	        this.featureVector = featureVector;
	    }
	    
	    public FeatureContainer getFeature() {
	    	return this.feature;
	    }
	}
}