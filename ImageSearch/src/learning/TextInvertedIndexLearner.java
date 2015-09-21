package learning;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Vector;

public class TextInvertedIndexLearner {
	private static final String PATH_DB_TEXT = "ImageData/train/";
	private static final String TEXT_DB_NAME = "train_tags.txt";
	private static final String TEXT_IMAGE_PATHS = "imgfilepaths.txt";
	private static final String TEXT_INVERTED_DB_NANE = "inverted_train_tags.txt";
	
	TreeMap <String, TreeSet<String> > _invertedTextDb = null;
	TreeMap <String, Vector<String> > _imagesPath = null;
	
	private TextInvertedIndexLearner() {
		_invertedTextDb = new TreeMap <String, TreeSet<String> >();
		_imagesPath = new TreeMap <String, Vector<String> >();
	}
	
	
	private boolean extractImageFilePaths(String filename) {
		try {
			// Open the file
			FileInputStream fstream = new FileInputStream(filename);
			BufferedReader br = new BufferedReader(new InputStreamReader(fstream));

			String strLine;

			//Read File Line By Line
			while ((strLine = br.readLine()) != null)   {
			  // Print the content on the console
				if (!addImagePath(strLine)) {
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
	
	private boolean addImagePath(String imagePath) {
		int unusedPrefix = 5;
		imagePath = imagePath.substring(unusedPrefix);
		int imgNameStart = imagePath.lastIndexOf("\\");
		imgNameStart++;
		String imageName = imagePath.substring(imgNameStart);
		
		Vector <String> paths = null;
		if (_imagesPath.containsKey(imageName)) {
			paths = _imagesPath.get(imageName);
		} else {
			paths = new Vector<String>();
		}
						
		paths.add(imagePath);
		_imagesPath.put(imageName, paths);
		return true;
	}
	
	private boolean readDbText(String filepath) {
		try {
			// Open the file
			FileInputStream fstream = new FileInputStream(filepath);
			BufferedReader br = new BufferedReader(new InputStreamReader(fstream));

			String strLine;

			//Read File Line By Line
			while ((strLine = br.readLine()) != null)   {
			  // Print the content on the console
				if (!invertImageText(strLine)) {
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
	
	private boolean invertImageText(String line) {
		String[] lineTokens = line.split(" ");
		String imageName = lineTokens[0].trim();
		
		for (int i = 1; i < lineTokens.length; i++) {
			String text = lineTokens[i].trim();
			
			if (text == null || "".equals(text)) {
				continue;
			}
			
			TreeSet <String> imagePaths = null;
			
			if (_invertedTextDb.containsKey(text)) {
				imagePaths = _invertedTextDb.get(text);
				
			} else {
				imagePaths = new TreeSet <String>();
			}
			imagePaths.addAll(_imagesPath.get(imageName));
			_invertedTextDb.put(text, imagePaths);
		}
		
		return true;
	}
	
	private boolean saveInvertedFilesIndexToFile(String filename) {
		FileWriter fw;
		try {
			fw = new FileWriter(filename);
			for(Map.Entry<String,TreeSet<String>> entry : _invertedTextDb.entrySet()) {
				String key = entry.getKey().trim();
				TreeSet<String> values = entry.getValue();
				
				String line = key + " :";
				while (!values.isEmpty()) {
					String value = values.pollFirst().trim();
					line += " " + value;
				}
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
	
	public static void main(String[] args) {
		TextInvertedIndexLearner learner = new TextInvertedIndexLearner();
		boolean isSuccess = learner.extractImageFilePaths(PATH_DB_TEXT + TEXT_IMAGE_PATHS);
		if (!isSuccess) {
			System.err.println("read image file paths problem!");
			return;
		}
		
		isSuccess = learner.readDbText(PATH_DB_TEXT + TEXT_DB_NAME);
		if (!isSuccess) {
			System.err.println("read db text problem!");
			return;
		}
		
		isSuccess = learner.saveInvertedFilesIndexToFile(PATH_DB_TEXT + TEXT_INVERTED_DB_NANE);
		if (!isSuccess) {
			System.err.println("save inverted db text problem!");
			return;
		}

	}
}
