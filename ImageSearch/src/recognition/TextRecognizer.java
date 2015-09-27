package recognition;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Vector;

import javax.imageio.ImageIO;

public class TextRecognizer {
	private static final String PATH_DB_TEXT = "ImageData/train/";
	private static final String PATH_TEST_TEXT = "ImageData/test/";
	private static final String TEXT_INVERTED_DB_NANE = "inverted_train_tags.txt";
	private static final String TEXT_DB_NAME = "train_tags.txt";
	private static final String TEXT_TEST_NAME = "test_tags.txt";
	private static final int TOP_KNN = 20;
	
	TreeMap <String, Vector<String> > _invertedTextDb = null;
	TreeMap <String, Vector<String> > _textDb = null;
	TreeMap <String, Vector<String> > _textFromTest = null;
 	
	private static TextRecognizer _textRecognizer = null;
	
	private TextRecognizer() {
		_invertedTextDb = new TreeMap <String, Vector<String> >();
		_textDb = new TreeMap <String, Vector<String>>();
		_textFromTest = new TreeMap <String, Vector<String> >();
		readDbText(PATH_DB_TEXT + TEXT_DB_NAME);
		readInvertedDbText(PATH_DB_TEXT + TEXT_INVERTED_DB_NANE);
		readTestText(PATH_TEST_TEXT + TEXT_TEST_NAME);
	}
	
	public static TextRecognizer getObject() {
		if (_textRecognizer == null) {
			_textRecognizer = new TextRecognizer();
		}
		return _textRecognizer;
	}
	
	public TreeSet<String> getTreeSetResultImageList (File file) {
		Vector<StringIntegerPair> resultImageList = getVectorResultImageList(file);
		if (resultImageList == null) {
			return null;
		}
		
		TreeSet <String> treeSetResultImageList = new TreeSet <String>();
		for (int i = 0; i < resultImageList.size(); i++) {
			String imagePath = resultImageList.get(i).getString();
			treeSetResultImageList.add(imagePath);
		}
		return treeSetResultImageList;
	}
	
	public BufferedImage[] searchByText(File file) throws IOException {
		Vector<StringIntegerPair> resultImageList = getVectorResultImageList(file);
		if (resultImageList == null) {
			return null;
		}
				
		BufferedImage[] imgs = extractBufferedImages(resultImageList);
		
		return imgs;
	}

	/**
	 * @param file
	 * @return
	 */
	private Vector<StringIntegerPair> getVectorResultImageList(File file) {
		String imageName = extractImageName(file);
		
		Vector <String> textsForThisFile = _textFromTest.get(imageName);
		
		if (textsForThisFile == null) {
			System.err.println("The file " + imageName + " is not from the test folder!");
			return null;
		}
		
		Vector <StringIntegerPair> resultImageList = extractResultImageList(textsForThisFile);
		return resultImageList;
	}
	
	public void test() {
		for(Map.Entry<String, Vector<String>> entry : _invertedTextDb.entrySet()) {
			  String key = entry.getKey();
			  Vector <String> values = entry.getValue();

			  System.out.print(key + " =>");
			  for (int i = 0; i < values.size(); i++) {
				  System.out.print(" " + values.get(i));
			  }
			  System.out.println("");
		}
		
		for(Map.Entry<String, Vector<String>> entry : _textDb.entrySet()) {
			  String key = entry.getKey();
			  Vector <String> values = entry.getValue();

			  System.out.print(key + " =>");
			  for (int i = 0; i < values.size(); i++) {
				  System.out.print(" " + values.get(i));
			  }
			  System.out.println("");
		}
		
		for(Map.Entry<String, Vector<String>> entry : _textFromTest.entrySet()) {
			  String key = entry.getKey();
			  Vector <String> values = entry.getValue();

			  System.out.print(key + " =>");
			  for (int i = 0; i < values.size(); i++) {
				  System.out.print(" " + values.get(i));
			  }
			  System.out.println("");
		}
	}

	/**
	 * @param resultImageList
	 * @return
	 * @throws IOException
	 */
	private BufferedImage[] extractBufferedImages(
			Vector<StringIntegerPair> resultImageList) throws IOException {
		BufferedImage[] imgs = new BufferedImage[TOP_KNN];
		for (int i = 0; i < Math.min(resultImageList.size(), imgs.length); i++) {
			String imgPath = resultImageList.get(i).getString();
			if (imgPath == null) {
				break;
			}
			
			File imgFile = new File(imgPath);
			imgs[i] = ImageIO.read(imgFile);
		}
		return imgs;
	}

	/**
	 * @param textsForThisFile
	 * @return
	 */
	private Vector <StringIntegerPair> extractResultImageList(
			Vector<String> textsForThisFile) {
		Vector <StringIntegerPair> resultImageList = new Vector <StringIntegerPair>();
		TreeMap <String, Integer> stringIndexMap = new TreeMap <String, Integer>();
		
		for (int i = 0; i < textsForThisFile.size(); i++) {
			String curToken = textsForThisFile.get(i).trim();
			Vector <String> imageListRetrievedForCurToken = _invertedTextDb.get(curToken);
			if (imageListRetrievedForCurToken == null) {
				continue;
			}
			
			for (int j = 0; j < imageListRetrievedForCurToken.size(); j++) {
				String imagePath = imageListRetrievedForCurToken.get(j);
				if (stringIndexMap.containsKey(imagePath)) {
					int index = stringIndexMap.get(imagePath).intValue();
					resultImageList.get(index).incrementInteger();
				} else {
					int index = resultImageList.size();
					stringIndexMap.put(imagePath, index);
					
					StringIntegerPair newPair = new StringIntegerPair(imagePath);
					resultImageList.add(newPair);
				}
			}
		}
		
		Collections.sort(resultImageList, new StringIntegerPair.SortInteger());

		return resultImageList;
	}
	
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

	/**
	 * @param file
	 * @return
	 */
	private String extractImageName(File file) {
		String imagePath = file.getAbsolutePath();
		int imageNameStart = imagePath.lastIndexOf("\\");
		imageNameStart++;
		String imageName = imagePath.substring(imageNameStart);
		return imageName;
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
		String currentPath = getCurrentDirectory();
		String[] lineTokens = line.split(":");
		String imageName = lineTokens[0].trim();
		lineTokens = lineTokens[1].trim().split(" ");
		
		Vector <String> texts = new Vector <String>();
		for (int i = 1; i < lineTokens.length; i++) {
			String text = lineTokens[i].trim();
			
			if (text == null || "".equals(text)) {
				continue;
			}
			
			texts.add(currentPath + text);
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
	
	private boolean readTestText(String filepath) {
		try {
			// Open the file
			FileInputStream fstream = new FileInputStream(filepath);
			BufferedReader br = new BufferedReader(new InputStreamReader(fstream));

			String strLine;

			//Read File Line By Line
			while ((strLine = br.readLine()) != null)   {
			  // Print the content on the console
				if (!retrieveTextForTest(strLine)) {
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
	
	private boolean retrieveTextForTest(String line) {
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
		
		_textFromTest.put(imageName, texts);
		
		return true;
	}
}
