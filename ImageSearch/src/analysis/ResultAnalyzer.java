package analysis;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Vector;

/************
 * 
 * 
 * This clsss will analyze the imagelist results generated
 * by the search using different combination of features
 * 
 * We will calculate the Precision, Recall, F1 and MAP
 * 
 * Define 2 sets:
 * resultImageSet : the result image list generated
 * correctImageSet : the correct image list expected
 * 
 * Retrieve and relevant (tp) = |correctImageSet n resultImageSet|
 * Retrieve and non-relevant (fp) = |resultImageSet| - |correctImageSet n resultImageSet|
 * Not Retrieved but relevant (fn) = |correctImageSet| - |correctImageSet n resultImageSet|
 * 
 * Precision P = tp/(tp + fp)
 * Recall R = tp/(tp + fn)
 * F1 = 2PR/ (P+R)
 * 
 * 
 * MAP@TOPN = sigma avg(Precision) / TOPN
 * 
 * @author Jun
 *
 */
public class ResultAnalyzer {
	private static final String TEST_IMAGE_LIST = "ImageList/test/TestImagelist.txt";
	private static final String TEST_GROUNDTRUTH_DIRECTORY = "Groundtruth/test/";
	
	private static final String TRAIN_IMAGE_LIST = "ImageList/train/TrainImagelist.txt";
	private static final String TRAIN_GROUNDTRUTH_DIRECTORY = "Groundtruth/train/";
	
	private static final String CATEGORY_NAMES = "ImageData/category_names.txt";
	private static final String groundTruthFileNamesPrefix = "Labels_";
	
	private static final String RESULT_FILE_NAME = "ResultAnalysis.txt";
	
	private static final int CHILD_OF_CATEGORY = 1;
	private static final int TOPN = 20;
	
	private Vector <String> _groundTruthFileNames = null;
	private Vector <String> _imageListOfTest = null;
	private Vector <String> _imageListOfTrain = null;
	private TreeMap <String, TreeSet <String>> _categoriesOfImagesTrain = null;
	
	//it is an inverted list of category-image
	private TreeMap <String, Vector <String>> _categoriesOfImagesTest = null;

	
	private ResultAnalyzer() {
		_groundTruthFileNames = new Vector<String>();
		
		getGroundTruthFileNames(CATEGORY_NAMES);
		_imageListOfTest = getImageList(TEST_IMAGE_LIST);
		_imageListOfTrain = getImageList(TRAIN_IMAGE_LIST);
		
		if (_imageListOfTest == null || _imageListOfTrain == null) {
			System.err.println("error in retrieving image list!");
			System.exit(-1);
		}
		
		initializeCategoriesOfImagesTest();
		initializeCategoriesOfImagesTrain();
	}

	public boolean generateAnalysisResult(String testImagePath, TreeSet <String> resultImageSet) {
		String testImageName = extractImageName(testImagePath).trim();
		String testImageRelativePath = getRelativePath(testImagePath);
		
		Vector <String> categories = _categoriesOfImagesTest.get(testImageName);
		TreeSet <String> correctImageSet = new TreeSet <String>();
		
		for (int i = 0; i < categories.size(); i++) {
			String categoryName = categories.get(i);
			TreeSet <String> imagesFromCategory = _categoriesOfImagesTrain.get(categoryName);
			correctImageSet.addAll(imagesFromCategory);
		}
		
		TreeSet <String> tpSet = new TreeSet <String>();
		tpSet.addAll(resultImageSet);
		
		tpSet.retainAll(correctImageSet);
		
		double tp = (double) tpSet.size();
		double fp = (double) resultImageSet.size() - tp;
		double fn = (double) correctImageSet.size() - tp;
		
		double precission = tp / (tp + fp);
		double recall = tp / (tp + fn);
		
		//2PR/ (P+R)
		double f1 = 2 * precission * recall / (precission + recall);
		
		//Pre = top/down
		double avgPrecissions = 0;
		double top = 0;
		double down = 0;
		String resultImagesString = " Result Images:";
		for (int i = 0; i < Math.min(TOPN, resultImageSet.size()); i++) {
			String imageName = resultImageSet.pollFirst();
			resultImagesString += imageName;
			if (correctImageSet.contains(imageName)) {
				top += 1;
				down += 1;
			} else {
				down += 1;
			}
			double avgPrecission = top/down;
			avgPrecissions += avgPrecission;
		}
		double mapAtTOPN = avgPrecissions / Math.min(TOPN, resultImageSet.size());
		
		String outputLine = "Test Image: " + testImageRelativePath +
				            " Precission: " + precission +
				            " Recall: " + recall +
				            " F1: " + f1 +
				            " N: " + Math.min(TOPN, resultImageSet.size()) + 
				            " MAP@N: " + mapAtTOPN +
				            resultImagesString + "\n";
				            
		
		FileWriter fw;
		try {
			fw = new FileWriter(RESULT_FILE_NAME);
			fw.write(outputLine);
		 
			fw.close();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		 
		return true;
		
	}

	
	private String getRelativePath (String absolutePath) {
		String currentDirectory = getCurrentDirectory();
		String relativePath = absolutePath.replaceFirst(currentDirectory, "");
		return relativePath;
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
	private String extractImageName(String imagePath) {
		int imageNameStart = imagePath.lastIndexOf("\\");
		imageNameStart++;
		String imageName = imagePath.substring(imageNameStart);
		return imageName;
	}
	
	
	/**
	 * 
	 */
	private void initializeCategoriesOfImagesTrain() {
		_categoriesOfImagesTrain = new TreeMap <String, TreeSet <String>>();

		for (int i = 0; i < _groundTruthFileNames.size(); i++) {
			String fileName = _groundTruthFileNames.get(i);			
			String filePathForTrain = TRAIN_GROUNDTRUTH_DIRECTORY + fileName;
			Vector <Integer> imagesTrainInThisCategory = retrieveGroundTruth(filePathForTrain);
			TreeSet <String> imageNames = new TreeSet <String>();
			for (int j = 0; j < imagesTrainInThisCategory.size(); j++) {
				int index = imagesTrainInThisCategory.get(j).intValue();
				String imageName = _imageListOfTrain.get(index);
				imageNames.add(imageName);
			}
			_categoriesOfImagesTrain.put(fileName, imageNames);
		}
	}

	/**
	 * 
	 */
	private void initializeCategoriesOfImagesTest() {
		_categoriesOfImagesTest = new TreeMap <String, Vector <String>>();
		for (int i = 0; i < _groundTruthFileNames.size(); i++) {
			String fileName = _groundTruthFileNames.get(i);
			
			String filePathForTest = TEST_GROUNDTRUTH_DIRECTORY + fileName;
			Vector <Integer> imagesTestInThisCategory = retrieveGroundTruth(filePathForTest);

			for (int j = 0; j < imagesTestInThisCategory.size(); j++) {
				int index = imagesTestInThisCategory.get(j).intValue();
				String imageName = _imageListOfTest.get(index);
				
				Vector <String> categories = null;
				if (_categoriesOfImagesTest.containsKey(imageName)) {
					categories = _categoriesOfImagesTest.get(imageName);
				} else {
					categories = new Vector <String>();
				}
				categories.add(fileName);
				_categoriesOfImagesTest.put(imageName, categories);
			}
		}
	}
	
	private Vector <Integer> retrieveGroundTruth(String filepath) {
		Vector <Integer> groundTruthList = null;
		try {
			// Open the file
			FileInputStream fstream = new FileInputStream(filepath);
			BufferedReader br = new BufferedReader(new InputStreamReader(fstream));

			String line;
			int groundTruthValue;
			groundTruthList = new Vector <Integer>();

			//Read File Line By Line
			int index = 0;
			while ((line = br.readLine()) != null)   {
			  // Print the content on the console
				line = line.trim();
				groundTruthValue = Integer.parseInt(line);
				if (groundTruthValue == CHILD_OF_CATEGORY) {
					groundTruthList.add(index);
				}
				index++;
			}
			
			//Close the input stream
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		
		return groundTruthList;
	}
	
	private Vector <String> getImageList(String filepath) {
		Vector <String> imageList = null;
		try {
			// Open the file
			FileInputStream fstream = new FileInputStream(filepath);
			BufferedReader br = new BufferedReader(new InputStreamReader(fstream));

			String image;
			imageList = new Vector <String>();

			//Read File Line By Line
			while ((image = br.readLine()) != null)   {
			  // Print the content on the console
				image = image.trim();
				imageList.add(image);
			}
			
			//Close the input stream
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		
		return imageList;
	}

	
	private boolean getGroundTruthFileNames(String filepath) {
		try {
			// Open the file
			FileInputStream fstream = new FileInputStream(filepath);
			BufferedReader br = new BufferedReader(new InputStreamReader(fstream));

			String strLine;

			//Read File Line By Line
			while ((strLine = br.readLine()) != null)   {
			  // Print the content on the console
				if (!processCategoryName(strLine)) {
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
	
	private boolean processCategoryName(String categoryName) {
		categoryName = categoryName.trim().toLowerCase();
		String fileName = groundTruthFileNamesPrefix + categoryName;
		_groundTruthFileNames.add(fileName);
		return true;
	}
	
}