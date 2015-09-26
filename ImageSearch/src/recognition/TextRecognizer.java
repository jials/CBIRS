package recognition;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Vector;

public class TextRecognizer {
	private static final String PATH_DB_TEXT = "ImageData/train/";
	private static final String PATH_TEST_TEXT = "ImageData/test/";
	private static final String TEXT_INVERTED_DB_NANE = "inverted_train_tags.txt";
	private static final String TEXT_TEST_NAME = "test_tags.txt";
	
	TreeMap <String, TreeSet<String> > _invertedTextDb = null;
	TreeMap <String, Vector<String> > _textDb = null;
	
	private TextRecognizer() {
		_invertedTextDb = new TreeMap <String, TreeSet<String> >();
		_textDb = new TreeMap <String, Vector<String>>();
		readDbText(PATH_DB_TEXT + TEXT_TEST_NAME);
		readInvertedDbText(PATH_DB_TEXT + TEXT_INVERTED_DB_NANE);
	}
	
	private boolean readInvertedDbText(String filepath) {
		try {
			// Open the file
			FileInputStream fstream = new FileInputStream(filepath);
			BufferedReader br = new BufferedReader(new InputStreamReader(fstream));

			String strLine;

			//Read File Line By Line
			while ((strLine = br.readLine()) != null)   {
			  // Print the content on the console
				if (!retrieveInvertedTextDb(strLine)) {
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
	
	private boolean retrieveInvertedTextDb(String line) {
		String[] lineTokens = line.split(" ");
		String imageName = lineTokens[0].trim();
		
		TreeSet <String> texts = new TreeSet <String>();
		for (int i = 1; i < lineTokens.length; i++) {
			String text = lineTokens[i].trim();
			
			if (text == null || "".equals(text)) {
				continue;
			}
			
			texts.add(text);
		}
		
		_invertedTextDb.put(imageName, texts);
		
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
				if (!retrieveTextDb(strLine)) {
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
	
	private boolean retrieveTextDb(String line) {
		String[] lineTokens = line.split(" ");
		String imageName = lineTokens[0].trim();
		
		Vector <String> texts = new Vector <String>();
		for (int i = 1; i < lineTokens.length; i++) {
			String text = lineTokens[i].trim();
			
			if (text == null || "".equals(text)) {
				continue;
			}
			
			texts.add(text);
		}
		
		_textDb.put(imageName, texts);
		
		return true;
	}
}
