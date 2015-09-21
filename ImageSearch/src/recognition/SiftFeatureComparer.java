package recognition;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.TreeSet;
import java.util.Vector;

import javax.imageio.ImageIO;


public class SiftFeatureComparer {
	
	private static final String PATH_DB = "ImageData";
	private static final String PATH_SIFT_FILES = "ImageData/train/";
	private static final String FILE_CODEBOOK_B = "codebook_b.file";
	private static final String FILE_SIFT_BINARY = "generate.py";
	private static final String FILE_GENERATED = "visual_words_for_test_data";
	private static final String FILE_INVERTED = "inverted_visual_words_histogram_for_training_data";
	private static final String FILE_VISUAL_WORD_HISTOGRAM = "visual_words_histogram_for_training_data";
	private static final String FILE_IMAGE_NAMES = "image_indexes_train";

	private static final int TOP_KNN = 20;
	private static final int ERROR_NUM_OF_VISUAL_WORDS = -1;
	private static final double ERROR_DOUBLE = -0.1;
	private static final double NO_FREQUENCY = 0.000000;
	
	private static SiftFeatureComparer _sift = null;
	
	private Vector <Double> _histogramOfCurrentImage;
	private Vector <String> _imageNames;
	
	private Vector <Vector <Double>> _visualWordHistogramOfImages;
	private Vector <TreeSet<Integer> > _setImageOfVisualWords;
	
	private int _numOfVisualWords;
	
	private SiftFeatureComparer() {
		_numOfVisualWords = ERROR_NUM_OF_VISUAL_WORDS;
		
		_setImageOfVisualWords = new Vector <TreeSet<Integer> >();
		extractImageSets(PATH_SIFT_FILES + FILE_INVERTED);
		
		_visualWordHistogramOfImages = new Vector < Vector <Double> >();
		extractWordHistogramsOfImagesInDb(PATH_SIFT_FILES + FILE_VISUAL_WORD_HISTOGRAM);
		
		_imageNames = new Vector <String>();
		extractImageNamesInDb(PATH_SIFT_FILES + FILE_IMAGE_NAMES);
	}
	
	private boolean extractImageNamesInDb(String fileImageNames) {
		try {
			// Open the file
			FileInputStream fstream = new FileInputStream(fileImageNames);
			BufferedReader br = new BufferedReader(new InputStreamReader(fstream));

			String strLine;
			String curPath = getCurrentDirectory();
			
			//Read File Line By Line
			while ((strLine = br.readLine()) != null)   {
			  // Print the content on the console
				String image = curPath + "\\" + PATH_DB + strLine;
				_imageNames.add(image);
			}
			
			//Close the input stream
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		
		return true;		
	}

	private boolean extractWordHistogramsOfImagesInDb(String fileVisualWordHistogram) {
		try {
			// Open the file
			FileInputStream fstream = new FileInputStream(fileVisualWordHistogram);
			BufferedReader br = new BufferedReader(new InputStreamReader(fstream));

			String strLine;

			//Read File Line By Line
			while ((strLine = br.readLine()) != null)   {
			  // Print the content on the console
				if (!processLine(strLine)) {
					br.close();
					return false;
				}
			  
			}
			
			//Close the input stream
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		
		return true;
	}
	
	private boolean processLine(String line) {
		String[] visualWords = line.split(" ");
		
		if (_numOfVisualWords == ERROR_NUM_OF_VISUAL_WORDS) {
			_numOfVisualWords = visualWords.length;
		} else if (_numOfVisualWords != visualWords.length) {
			return false;
		} else {
			//ntg happened
		}
		
		Vector <Double> histogramOfCurrentImage = new Vector <Double>();
		for (int i = 0; i < visualWords.length; i++) {
			visualWords[i] = visualWords[i].split(":")[1].trim();
			Double frequency = Double.parseDouble(visualWords[i]);
			histogramOfCurrentImage.add(frequency);
		}
		
		_visualWordHistogramOfImages.add(histogramOfCurrentImage);
		
		return true;
	}
	
	private boolean extractImageSets(String fileInverted) {
		try {
			// Open the file
			FileInputStream fstream = new FileInputStream(fileInverted);
			BufferedReader br = new BufferedReader(new InputStreamReader(fstream));

			String strLine;

			//Read File Line By Line
			while ((strLine = br.readLine()) != null)   {
			  // Print the content on the console
				if (!processImageSet(strLine)) {
					br.close();
					return false;
				}
			  
			}
			
			//Close the input stream
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		
		return true;
	}
	
	private boolean processImageSet(String line) {
		String[] imageIndex = line.split(" ");
		
		TreeSet <Integer> imageSet = new TreeSet <Integer>();
		for (int i = 0; i < imageIndex.length; i++) {
			Integer index = Integer.parseInt(imageIndex[i]);
			imageSet.add(index);
		}
		_setImageOfVisualWords.add(imageSet);
		
		return true;
	}
	
	public static SiftFeatureComparer getObject() {
		if (_sift == null) {
			_sift = new SiftFeatureComparer();
		}
		return _sift;
	}
	
	public BufferedImage[] searchBySift(File file) throws IOException {
		runSiftBinary(file);
		
		if (!extractWordHistograms(FILE_GENERATED)){
			System.err.println("failed to extract word histogram");	
		}
		
		TreeSet <Integer> curImageSet = generateCurImageSet();
		
		System.out.println(curImageSet.size());

		Vector <Integer> topKnnImages = findNearestImages(curImageSet);
		
		BufferedImage[] imgs = new BufferedImage[TOP_KNN];
		for (int i = 0; i < topKnnImages.size(); i++) {
			int imgIndex = topKnnImages.get(i).intValue();
			String imgPath = _imageNames.get(imgIndex);
			File imgFile = new File(imgPath);
			imgs[i] = ImageIO.read(imgFile);
		}
		return imgs;
	}

	/**
	 * @param curImageSet
	 */
	private Vector <Integer> findNearestImages(TreeSet<Integer> curImageSet) {
		Vector <DoubleIntegerPair> doubleIntegerPairs = new Vector <DoubleIntegerPair>();
		
		while (!curImageSet.isEmpty()) {
			int second = curImageSet.pollFirst().intValue();
			double first = calculateEuclideanDistance(_histogramOfCurrentImage, _visualWordHistogramOfImages.get(second));
			
			DoubleIntegerPair doubleIntegerPair = new DoubleIntegerPair(first, second);
			doubleIntegerPairs.add(doubleIntegerPair);
		}
		Collections.sort(doubleIntegerPairs, new DoubleIntegerPair.SortFirstDouble());
		
		
		Vector <Integer> topKnnImages = new Vector <Integer>();
		for (int i = 0; i < Math.min(TOP_KNN, doubleIntegerPairs.size()); i++) {
			topKnnImages.add(doubleIntegerPairs.get(i).getSecond());
		}
		return topKnnImages;
	}
	
	private double calculateEuclideanDistance(Vector <Double> firstSet, Vector <Double> secondSet) {
		if (firstSet.size() != secondSet.size()) {
			return ERROR_DOUBLE;
		} else {
			//ntg
		}
		
		double ans = 0;
		for (int i = 0; i < firstSet.size(); i++) {
			double first = firstSet.get(i).doubleValue();
			double second = secondSet.get(i).doubleValue();
			double dif = first - second;
			dif *= dif;
			ans += dif;
		}
		ans = Math.sqrt(ans);
		return ans;
	}
	
	private TreeSet <Integer> generateCurImageSet() {
		
		TreeSet <Integer> curImageSet = new TreeSet<Integer>();
		for (int i = 0; i < _histogramOfCurrentImage.size(); i++) {
			if (_histogramOfCurrentImage.get(i) > NO_FREQUENCY) {
				if (curImageSet.isEmpty()) {
					curImageSet.addAll(_setImageOfVisualWords.get(i));
				} else {
					curImageSet.retainAll(_setImageOfVisualWords.get(i));
				}
			}
		}
		return curImageSet;
	}

	/**
	 * @param file
	 */
	private void runSiftBinary(File file) {
		String currentPath = getCurrentDirectory();
		if (currentPath == null) {
			return;
		}
		
		String option = " -c ";
		String codebookBPath = currentPath + "\\" + PATH_SIFT_FILES + FILE_CODEBOOK_B;
		
		String imagePath = file.getAbsolutePath();
		try {
			Process process = Runtime.getRuntime().exec("python " + FILE_SIFT_BINARY + option + codebookBPath + " " + imagePath);
			InputStream is = process.getInputStream();
			InputStreamReader isr = new InputStreamReader(is);
			BufferedReader br = new BufferedReader(isr);
			String line;

			while ((line = br.readLine()) != null) {
			  System.out.println(line);
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}
	}

	/**
	 * @return
	 */
	private String getCurrentDirectory() {
		File currentDirectory = new File(new File(".").getAbsolutePath());
		String currentPath = null;
		
		try {
			currentPath = currentDirectory.getCanonicalPath();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			return null;
		}
		return currentPath;
	}
	
	
	private boolean extractWordHistograms(String fileVisualWordHistogram) {
		try {
			// Open the file
			FileInputStream fstream = new FileInputStream(fileVisualWordHistogram);
			BufferedReader br = new BufferedReader(new InputStreamReader(fstream));

			String strLine;

			//Read File Line By Line
			while ((strLine = br.readLine()) != null)   {
			  // Print the content on the console
				if (!processGeneratedFileLine(strLine)) {
					br.close();
					return false;
				}
			  
			}
			
			//Close the input stream
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		
		return true;
	}
	
	private boolean processGeneratedFileLine(String line) {
		String[] visualWords = line.split(" ");
		
		if (_numOfVisualWords == ERROR_NUM_OF_VISUAL_WORDS) {
			_numOfVisualWords = visualWords.length;
		} else if (_numOfVisualWords != visualWords.length) {
			return false;
		} else {
			//ntg happened
		}
		
		_histogramOfCurrentImage = new Vector <Double>();
		for (int i = 0; i < visualWords.length; i++) {
			visualWords[i] = visualWords[i].split(":")[1].trim();
			Double frequency = Double.parseDouble(visualWords[i]);
			_histogramOfCurrentImage.add(frequency);
		}
		
		
		return true;
	}
}
