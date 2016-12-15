package de.htw.mp.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;

import de.htw.mp.model.FeatureContainer;
import de.htw.mp.ui.component.ColorView;
import de.htw.mp.ui.component.ImageView;

/**
 * Simple data set viewer. Categorizes and lists all image files in a directory.
 * The UI provides an image viewer and mean color calculation.
 * 
 * @author Nico Hezel
 */
public abstract class DatasetViewerBase extends JPanel {

	private static final long serialVersionUID = 8420613964694777857L;
	
	/**
	 * Type of features
	 */
	public static enum FeatureType { 
		MeanColor, MeanImage;
		
		public static FeatureType get(String value) {
			return FeatureType.valueOf(value.trim().replaceAll(" ", ""));
		}
	};	

	// spacing and border size
	private static final int border = 10;
	
	/**
	 * For each image category there exists a file list
	 */
	private Map<String, File[]> categoryToFileList = new HashMap<>();
	
	/**
	 * Map from category name to the feature container of a category
	 */
	private Map<String, FeatureContainer> categoryFeatures = new HashMap<>();
	
	/**
	 * Map from filename to feature container for the image
	 */
	private Map<String, FeatureContainer> imageFeatures = new HashMap<>();
	
	/**
	 * Content of the left list
	 */
	private JList<String> categoryList = null;
	private DefaultListModel<String> categoryListModel = null;
	
	/**
	 * Content of the right list
	 */
	private JList<String> imageFileList = null;
	private DefaultListModel<String> imageFileListModel = null;
	
	/**
	 * Content of the ranking list
	 */
	private JList<String> rankingList = null;
	private DefaultListModel<String> rankingListModel = null;

	/**
	 * Image display on the bottom left
	 */
	private ImageView imageDisplay = null;
	
	/**
	 * Color display on the bottom right
	 */
	private ColorView colorDisplay = null;
	
	/**
	 * Prediction value
	 */
	private JTextField predictionText = null;
	
	/**
	 * k-Nearest Neighbours value
	 */
	private JTextField kNearestNeighboursText = null;
	
	/**
	 * Which type of feature was selected
	 */
	private ButtonGroup featureGroup = null;
	
	/**
	 * Which database was selected
	 */
	private ButtonGroup databaseGroup = null;

	/**
	 * Constructor. Constructs the layout of the GUI components and loads the
	 * initial image.
	 */
	public DatasetViewerBase() {
		super(new BorderLayout(border, border));

		// the left browsing menu
		{
			JPanel browsePanel = new JPanel(new BorderLayout(border, border));	
			
			// add the menu
			{
				JPanel menuPanel = new JPanel(new GridLayout(1, 4, border, border));
				menuPanel.setPreferredSize(new Dimension(800, 200));
				browsePanel.add(menuPanel, BorderLayout.NORTH);
	
				// add open folder button
				{
					JButton openDirectoryBtn = new JButton("Open Folder");
					openDirectoryBtn.addActionListener(this::onOpenDirectoryClick); // click event handler
					menuPanel.add(openDirectoryBtn);
				}
	
				// add category combo box and a describing label
				{
					JPanel categoryMenuPanel = new JPanel(new GridBagLayout());
					menuPanel.add(categoryMenuPanel);
	
					GridBagConstraints c = new GridBagConstraints();
					c.fill = GridBagConstraints.BOTH;
					c.weighty = .2; // request any extra vertical space
					c.weightx = 1.0; // request any extra vertical space
	
					JLabel categoryLabel = new JLabel("Category");
					categoryMenuPanel.add(categoryLabel, c);
	
					// model containing all elements of the list
					categoryListModel = new DefaultListModel<>();
					categoryList = new JList<>(categoryListModel);
					categoryList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
					categoryList.addListSelectionListener(this::onCategoryListChange); // selection change  handler
					JScrollPane listScroller = new JScrollPane(categoryList);
					c.weighty = .8; // request any extra vertical space
					c.gridy = 1;
					categoryMenuPanel.add(listScroller, c);
				}
	
				// add category combo box and a describing label
				{
					JPanel categoryMenuPanel = new JPanel(new GridBagLayout());
					menuPanel.add(categoryMenuPanel);
	
					GridBagConstraints c = new GridBagConstraints();
					c.fill = GridBagConstraints.BOTH;
					c.weighty = .2; // request any extra vertical space
					c.weightx = 1.0; // request any extra vertical space
	
					JLabel categoryLabel = new JLabel("Image Files");
					categoryMenuPanel.add(categoryLabel, c);
	
					imageFileListModel = new DefaultListModel<>();
					imageFileList = new JList<>(imageFileListModel);				
					imageFileList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
					imageFileList.addListSelectionListener(this::onImageFileListChange); // selection change handler
					imageFileList.addMouseListener(new MouseAdapter() {
					    public void mouseClicked(MouseEvent evt) {
					        onImageFileClick(evt);
					    }
					});

					JScrollPane listScroller = new JScrollPane(imageFileList);
					c.weighty = .8; // request any extra vertical space
					c.gridy = 1;
					categoryMenuPanel.add(listScroller, c);
				}
			}
	
			// setup the image display
			imageDisplay = new ImageView();
			imageDisplay.setPreferredSize(new Dimension(400, 400));
			browsePanel.add(imageDisplay, BorderLayout.WEST);
			
			// setup the image display
			colorDisplay = new ColorView();
			colorDisplay.setPreferredSize(new Dimension(400, 400));
			browsePanel.add(colorDisplay, BorderLayout.EAST);
			add(browsePanel, BorderLayout.WEST);
		}

		// the right search menu
		{
			JPanel searchPanel = new JPanel(new BorderLayout(border, border));	
			
			// add category combo box and a describing label
			{
				JPanel searchOptionPanel = new JPanel(new GridBagLayout());
				searchPanel.add(searchOptionPanel);

				// Search Area Label
				GridBagConstraints c = new GridBagConstraints();
				c.fill = GridBagConstraints.BOTH;
				c.weighty = 0.03; // request any extra vertical space
				c.weightx = 1.0; // request any extra vertical space
				c.gridy = 0;				
				JLabel searchAreaLabel = new JLabel("Search Area");
				searchOptionPanel.add(searchAreaLabel, c);
				
				// Feature Label
				c.weighty = 0; // request any extra vertical space
				c.gridy = 1;				
				JLabel featureLabel = new JLabel("Feature");
				searchOptionPanel.add(featureLabel, c);
				
				// MeanColor Radio Button
				c.gridy = 2;
				JRadioButton meanColorButton = new JRadioButton("Mean Color");
				meanColorButton.setActionCommand("Mean Color");
				meanColorButton.setSelected(true);
				searchOptionPanel.add(meanColorButton, c);

				// MeanImage Radio Button
				c.gridy = 3;
				JRadioButton meanImageButton = new JRadioButton("Mean Image");
				meanImageButton.setActionCommand("Mean Image");
				searchOptionPanel.add(meanImageButton, c);
				
			    //Group the radio buttons.
			    featureGroup = new ButtonGroup();
			    featureGroup.add(meanColorButton);
			    featureGroup.add(meanImageButton);
			    
			    // spacer
			    c.weighty = 0.05; // request any extra vertical space
				c.gridy = 4;
				JLabel spacer1 = new JLabel("");
				searchOptionPanel.add(spacer1, c);
			    
				// Database Label
				c.weighty = 0; // request any extra vertical space
				c.gridy = 5;				
				JLabel databaseLabel = new JLabel("Database");
				searchOptionPanel.add(databaseLabel, c);
				
				// AllImages Radio Button
				c.gridy = 6;
				JRadioButton allImagesButton = new JRadioButton("All Images");
				allImagesButton.setActionCommand("All Images");
				allImagesButton.setSelected(true);
				searchOptionPanel.add(allImagesButton, c);

				// Categories Radio Button
				c.gridy = 7;
				JRadioButton categoriesButton = new JRadioButton("Categories");
				categoriesButton.setActionCommand("Categories");
				searchOptionPanel.add(categoriesButton, c);
				
			    //Group the radio buttons.
			    databaseGroup = new ButtonGroup();
			    databaseGroup.add(allImagesButton);
			    databaseGroup.add(categoriesButton);
			    
			    // spacer
			    c.weighty = 0.05; // request any extra vertical space
				c.gridy = 8;
				JLabel spacer2 = new JLabel("");
				searchOptionPanel.add(spacer2, c);
			    
				// Ranking Label
				c.weighty = 0; // request any extra vertical space
				c.gridy = 9;				
				JLabel rankingLabel = new JLabel("Ranking");
				searchOptionPanel.add(rankingLabel, c);				

				// model containing all elements of the list
				rankingListModel = new DefaultListModel<>();
				rankingList = new JList<>(rankingListModel);
				rankingList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
				rankingList.addListSelectionListener(this::onCategoryListChange); // selection change  handler
				JScrollPane listScroller = new JScrollPane(rankingList);
				c.weighty = .8; // request any extra vertical space
				c.gridy = 10;
				searchOptionPanel.add(listScroller, c);
				
				// k nearest neighbours
				c.weighty = 0; // request any extra vertical space
				c.gridy = 11;				
				JLabel kNearestNeighboursLabel = new JLabel("k-Nearest Neighbours");
				searchOptionPanel.add(kNearestNeighboursLabel, c);		
				
				// k nearest neighbours value
				c.gridy = 12;				
				kNearestNeighboursText = new JTextField("5");
				searchOptionPanel.add(kNearestNeighboursText, c);		
				
				// prediction label
				c.gridy = 13;				
				JLabel predictionLabel = new JLabel("Prediction:");
				searchOptionPanel.add(predictionLabel, c);		
				
				// prediction value
				c.gridy = 14;				
				predictionText = new JTextField("");
				predictionText.setEnabled(false);
				searchOptionPanel.add(predictionText, c);	
			}
			
			add(searchPanel, BorderLayout.EAST);
		}
		
		// load the initial image
		try {
			URL res = getClass().getResource("/Hummel.jpg");
			File imageFile = Paths.get(res.toURI()).toFile();
			updateMeanColorAndImage(getMeanColor(imageFile), getMeanImage(imageFile));
			imageDisplay.setImage(ImageIO.read(imageFile));
		} catch (Exception e) {
			e.printStackTrace();
		}
	};
	
	/**
	 * If a double click is registered a search will be triggered.
	 * 
	 * @param click
	 */
	private void onImageFileClick(MouseEvent click) {
		
		// Double-click detected
        if (click.getClickCount() == 2) {

        	// get the query
            int index = imageFileList.locationToIndex(click.getPoint());
            String filename = imageFileListModel.getElementAt(index);
			FeatureContainer query = imageFeatures.get(filename);

			// get the database
			String dbName = databaseGroup.getSelection().getActionCommand();
			Map<String, FeatureContainer> database = ("All Images".equalsIgnoreCase(dbName)) ? imageFeatures : categoryFeatures;

			// sort the elements
			FeatureType featureType = FeatureType.get(featureGroup.getSelection().getActionCommand());
			List<FeatureContainer> result = retrieve(query, database.values().toArray(new FeatureContainer[0]), featureType);

			// list all search results
			rankingListModel.clear();
			for (FeatureContainer element : result)
				rankingListModel.addElement(element.getName());

			// make a prediction
			int kNN = Integer.parseInt(kNearestNeighboursText.getText());
			String prediction = classify(result, kNN);
			predictionText.setText(prediction);
		}
	}

	/**
	 * Analysis all images inside the selected category and paint their mean
	 * color in the color panel. Lists all image files of the category in the
	 * image file list view.
	 * 
	 * @param event
	 */
	private void onCategoryListChange(ListSelectionEvent event) {
		if (event.getValueIsAdjusting() == false) {
			
			String categoryName = categoryList.getSelectedValue();
			if(categoryName != null) {				
				updateMeanColorAndImage(this.categoryFeatures.get(categoryName));		
				
				// list all the image file names
				imageFileListModel.clear();
				for (File file : categoryToFileList.get(categoryName))
					imageFileListModel.addElement(file.toPath().getFileName().toString());
			}
		}
	}

	/**
	 * Loads and displays the image from the selected image file.
	 * 
	 * @param event
	 */
	private void onImageFileListChange(ListSelectionEvent event) {
		if (event.getValueIsAdjusting() == false) {
			String filename = imageFileList.getSelectedValue();
			updateMeanColorAndImage(this.imageFeatures.get(filename));
		}
	}
	
	/**
	 * Display the mean color and mean image
	 * 
	 * @param imageFiles
	 */
	private void updateMeanColorAndImage(FeatureContainer feature) {
		updateMeanColorAndImage(feature.getMeanColor(), feature.getMeanImage());
	}
	
	/**
	 * Display the mean color and mean image
	 * 
	 * @param meanColor
	 * @param meanImage
	 */
	private void updateMeanColorAndImage(Color meanColor, BufferedImage meanImage) {
		colorDisplay.setColor(meanColor);
		imageDisplay.setImage(meanImage);
	}

	/**
	 * Opens a dialog to select a data directory. All image files inside the
	 * directory will be filtered and categorized bases on their names. The
	 * resulting categories are listed in the category list view.
	 * 
	 * @param event
	 */
	private void onOpenDirectoryClick(ActionEvent event) {

		// open the directory chooser
		JFileChooser dirChooser = new JFileChooser(new File("dataset"));
		dirChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		if (dirChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
			File dir = dirChooser.getSelectedFile();

			// abort
			if(dir == null) return;

			// read all image files from the directory
			try (DirectoryStream<Path> files = Files.newDirectoryStream(dir.toPath(), "*.{jpg,jpeg,png}")) {
				Map<String, List<File>> categories = new HashMap<>();

				for (Path imageFile : files) {
					String name = imageFile.getFileName().toString().split("_")[0];
					List<File> cat = categories.getOrDefault(name, new ArrayList<File>());
					cat.add(imageFile.toFile());
					categories.putIfAbsent(name, cat);
				}
				
				// copy over
				categoryToFileList.clear();
				categories.forEach((key, value) -> { categoryToFileList.put(key, value.toArray(new File[0])); });
				
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			
			// -------------------- Build Category List -------------------------
			// add an "All" category
			File[] all = categoryToFileList.values()
					 					   .stream()
					 					   .flatMap(files -> Arrays.stream(files))
					 					   .toArray(File[]::new);

			// list all category names
			resetAll();
			categoryListModel.addElement("All");
			categoryToFileList.keySet().stream().sorted().forEach(name -> categoryListModel.addElement(name));
			
			// now add the All category
			categoryToFileList.put("All", all);
			
			// calculate all the mean colors and mean images for all files
			precalculateFeatures(categoryToFileList);
		}
	}
	

	/**
	 * 
	 * @param categoryToFileList
	 */
	private void precalculateFeatures(Map<String, File[]> categories) {
		
		categoryFeatures.clear();
		categories.forEach((categoryName, categoryFiles) -> {
			if("All".equalsIgnoreCase(categoryName)) return;
			
			String name = categoryName;
			Color meanColor = getMeanColor(categoryFiles);
			BufferedImage meanImage = getMeanImage(categoryFiles);
			FeatureContainer feature = new FeatureContainer(name, categoryName, meanColor, meanImage);
			categoryFeatures.put(name, feature);
		});
		
		imageFeatures.clear();
		categories.forEach((categoryName, categoryFiles) -> {
			if("All".equalsIgnoreCase(categoryName)) return;
			
			for (File imageFile : categoryFiles) {	
				String name = imageFile.toPath().getFileName().toString();
				Color meanColor = getMeanColor(imageFile);
				BufferedImage meanImage = getMeanImage(imageFile);
				FeatureContainer feature = new FeatureContainer(name, categoryName, meanColor, meanImage);
				imageFeatures.put(name, feature);
			}			
		});
	}
	
	/**
	 * Clears all lists and displays
	 */
	private void resetAll() {
		categoryListModel.clear();
		imageFileListModel.clear();
		rankingListModel.clear();
		imageDisplay.setImage(null);
		colorDisplay.setColor(Color.WHITE);
		predictionText.setText("");
	}
	
	/**
	 * Calculate the mean color of all given images. Or return PINK if there are no images.
	 * 
	 * @param imageFiles
	 * @return
	 */
	public abstract Color getMeanColor(File ... imageFiles);
	
	/**
	 * Calculate the mean images of all given images. Or return NULL if there are no images.
	 * 
	 * @param imageFiles
	 * @return
	 */
	public abstract BufferedImage getMeanImage(File ... imageFiles);
	
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
	public abstract List<FeatureContainer> retrieve(FeatureContainer query, FeatureContainer[] database, FeatureType featureType);

	/**
	 * Predict the category.
	 * Make the prediction based on the sorted list of features (images or categories). 
	 * 
	 * @param sortedList
	 * @param k
	 * @return predicted category
	 */
	public abstract String classify(List<FeatureContainer> sortedList, int k);
}