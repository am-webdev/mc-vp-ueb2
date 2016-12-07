package de.htw.mp.ui;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
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
	 * TODO Calculate the mean color of all given images. Or return PINK if there are no images.
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
	 * TODO Calculate the mean image of all given images. Or return NULL if there are no images.
	 * 
	 * @param imageFiles
	 * @return
	 */
	public BufferedImage getMeanImage(File ... imageFiles) {
		
		// no images? return null
		if(imageFiles.length == 0) return null;		
		
		return null; 
	}
	
	/**
	 * TODO Sort the elements in the database based on the similarity to the search query.
	 * The similarity will be calculated between to features. Features are are stored in
	 * the FeatureContainer and the FeatureType specifies which feature should be used.
	 *  
	 * @param query
	 * @param database
	 * @param featureType
	 * @return sorted list of database elements
	 */
	public List<FeatureContainer> retrieve(FeatureContainer query, FeatureContainer[] database, FeatureType featureType) {
		return Arrays.stream(database).collect(Collectors.toList());
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
		return sortedList.get(0).getCategory();
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
}