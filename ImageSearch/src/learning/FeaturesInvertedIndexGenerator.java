package learning;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Vector;

public class FeaturesInvertedIndexGenerator {
	private static final String PATH_FILES_GENERATED_SIFT = "ImageData/train/";
	private static final String FILE_VISUAL_WORD_HISTOGRAM = "visual_words_histogram_for_training_data";
	private static final String FILE_INVERTED_WORD_IMAGE = "inverted_visual_words_histogram_for_training_data";
	
	private static final int ERROR_NUM_OF_VISUAL_WORDS = -1;
	private static final double NO_FREQUENCY = 0.000000;
	
	private int _numOfVisualWords;
	
	private Vector <Vector <Double>> _visualWordHistogramOfImages;
	private Vector <Vector <Integer>> _invertedFileDb;
	
	
	private FeaturesInvertedIndexGenerator() {
		_numOfVisualWords = ERROR_NUM_OF_VISUAL_WORDS;
		_visualWordHistogramOfImages = new Vector < Vector <Double> >();
		_invertedFileDb = new Vector < Vector <Integer> >();
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
	
	private void computeInvertedIndex() {
		for (int i = 0; i < _numOfVisualWords; i++) {
			Vector <Integer> visualWordNumI = new Vector<Integer>();
			for (int j = 0; j < _visualWordHistogramOfImages.size(); j++) {
				if (_visualWordHistogramOfImages.get(j).get(i) > NO_FREQUENCY) {
					visualWordNumI.add(j);
				}
			}
			_invertedFileDb.add(visualWordNumI);
		}
	}
	
	private String convertToString (Vector<Integer> visualWord) {
		StringBuffer resultString = new StringBuffer("");
		for (int i = 0; i < visualWord.size(); i++) {
			resultString.append(visualWord.get(i).toString());
			resultString.append(" ");
		}
		return resultString.toString().trim();
	}
	
	private boolean saveInvertedFileIndexToFile(String filename) {
		FileWriter fw;
		try {
			fw = new FileWriter(filename);
			for (int i = 0; i < _invertedFileDb.size(); i++) {
				String line = convertToString(_invertedFileDb.get(i));
				fw.write(line + "\n");
			}
		 
			fw.close();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		 
		return true;
	}
	
	public static void main(String[] Args) {
		FeaturesInvertedIndexGenerator generator = new FeaturesInvertedIndexGenerator();
		
		boolean isSuccess = generator.extractWordHistograms(PATH_FILES_GENERATED_SIFT + FILE_VISUAL_WORD_HISTOGRAM);
		
		if (!isSuccess) {
			System.err.println("got problem in reading visual_words!");
			return;
		}
		generator.computeInvertedIndex();
		isSuccess = generator.saveInvertedFileIndexToFile(PATH_FILES_GENERATED_SIFT + FILE_INVERTED_WORD_IMAGE);
		
		if (!isSuccess) {
			System.err.println("got problem in saving inverted_visual_words!");
			return;
		}
	}
}
