package analysis;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Vector;

import recognition.ColorHist;
import recognition.SiftFeatureComparer;
import recognition.TextRecognizer;
import recognition.VisualConceptGenerator;

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
	private static final String groundTruthFileExtension = ".txt";
	
	private static final String RESULT_FILE_NAME = "ResultAnalysis.txt";
	
	private static final String PATH_PYTHON_FPGENERATOR = "src/learning/FilepathsGenerator.py";
	private static final String PATH_TEST_IMAGES = "ImageData/test/data";
	private static final String PATH_OUTPUT_FPATHS = "ImageData/test/ImagePaths.txt";

	
	private static final int CHILD_OF_CATEGORY = 1;
	private static final int TOPN = 20;
	
	private Vector <String> _groundTruthFileNames = null;
	private Vector <String> _imageListOfTest = null;
	private Vector <String> _imageListOfTrain = null;
	private TreeMap <String, TreeSet <String>> _categoriesOfImagesTrain = null;
	
	
	//it is an inverted list of category-image
	private TreeMap <String, Vector <String>> _categoriesOfImagesTest = null;

	
	private static void generateTestImageFilepaths(String filepath) {
		try {
			Process process = Runtime.getRuntime().exec("python " + PATH_PYTHON_FPGENERATOR + " -t " + 
														PATH_TEST_IMAGES + " -c " + filepath);
			InputStream is = process.getInputStream();
			InputStreamReader isr = new InputStreamReader(is);
			BufferedReader br = new BufferedReader(isr);
			String line;
			
			while ((line = br.readLine()) != null) {
				System.out.println(line);
			}
		} catch (IOException e) {
			System.out.println("Error calling Python FilepathsGenerator script!");
		}
	}
	
	public Vector <String> getTestImageFilePaths(String filepath) {
		Vector <String> imageList = null;
		try {
			// Open the file
			FileInputStream fstream = new FileInputStream(filepath);
			BufferedReader br = new BufferedReader(new InputStreamReader(fstream));

			String image;
			imageList = new Vector <String>();
			TreeSet <String> imageNames = new TreeSet<String>();

			//Read File Line By Line
			while ((image = br.readLine()) != null)   {
			  // Print the content on the console
				image = image.trim();
				
				String imageName = extractImageName(image);
				if (!imageNames.contains(imageName)) {
					imageNames.add(imageName.trim());
					imageList.add(image);
				} 
			}
			
			//Close the input stream
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		
		return imageList;
	}

	
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

	public StatisticObject generateAnalysisResult(File file, TreeSet <String> resultImageSetInitial) {
		String testImageRelativePath = null;
		try {
			testImageRelativePath = file.getCanonicalPath();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}

		String testImageName = extractImageName(testImageRelativePath).trim();
		
		TreeSet <String> resultImageSet = new TreeSet <String>();
		while (!resultImageSetInitial.isEmpty()) {
			String imagePath = resultImageSetInitial.pollFirst();
			String imageName = extractImageName(imagePath).trim();
			resultImageSet.add(imageName);
		}
		
		
		Vector <String> categories = _categoriesOfImagesTest.get(testImageName);
		TreeSet <String> correctImageSet = new TreeSet <String>();
	
		
		if (categories == null) {
			return null;
		}
		
		for (int i = 0; i < categories.size(); i++) {
			String categoryName = categories.get(i);
			TreeSet <String> imagesFromCategory = _categoriesOfImagesTrain.get(categoryName);
			correctImageSet.addAll(imagesFromCategory);
		}
		
		System.out.println(resultImageSet);
		System.out.println(correctImageSet);
		
		TreeSet <String> tpSet = new TreeSet <String>();
				
		tpSet.addAll(resultImageSet);
		
		tpSet.retainAll(correctImageSet);		
		
		double tp = (double) tpSet.size();
		double fp = (double) resultImageSet.size() - tp;
		double fn = (double) correctImageSet.size() - tp;
		
		
		double precision = tp / (tp + fp);
		double recall = tp / (tp + fn);
		
		//2PR/ (P+R)
		double f1 = 2 * precision * recall / (precision + recall);
		
		//MAP = sum of i= 1:x of (precision at i * change in recall)
		/*
		 * Precision at i is a percentage of correct items among first i recommendations.

Change in recall is 1/x if item at i is correct (for every correct item), 
otherwise zero. Let’s assume that the number of relevant items is bigger or 
equal to x: r >= x. If not, change in recall is 1/r for each correct i instead of 1/x.
		 */
		int numOfCorrect = 0;

		String resultImagesString = " Result Images:";
		int size = Math.min(TOPN, resultImageSet.size());
		int x = Math.min(size, correctImageSet.size());
		double avgPrecisions = 0;
		for (int i = 0; i < size; i++) {
			String imageName = resultImageSet.pollFirst();
			resultImagesString += imageName + " ";
			double changeInRecall = 0;
			if (correctImageSet.contains(imageName)) {
				numOfCorrect++;
				changeInRecall = 1 / (double) (x);
			}
			double curPrecision = (double) numOfCorrect / (i + 1);
			double avgPrecision = curPrecision * changeInRecall;
			avgPrecisions += avgPrecision;
		}

		double mapAtTOPN = avgPrecisions;
		
		StatisticObject object = new StatisticObject(precision, 
													 recall,
													 f1,
													 Math.min(TOPN, resultImageSet.size()),
													 mapAtTOPN);
		
		String outputLine = "Test Image: " + testImageRelativePath +
				            " Precision: " + precision +
				            " Recall: " + recall +
				            " F1: " + f1 +
				            " N: " + Math.min(TOPN, resultImageSet.size()) + 
				            " MAP@N: " + mapAtTOPN +
				            resultImagesString + "\n";
				            
		if (!writeToResultFile(outputLine)) {
			return null;
		}
		
		return object;
	}

	/**
	 * @param line
	 * @return
	 */
	public boolean writeToResultFile(String line) {
		return writeToResultFile(line, true);
	}

	public boolean writeToResultFile(String line, boolean isAppend) {
		FileWriter fw;
		try {
			fw = new FileWriter(RESULT_FILE_NAME, isAppend);
			fw.write(line);
		 
			fw.close();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		 
		return true;
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
		String fileName = groundTruthFileNamesPrefix + categoryName + groundTruthFileExtension;
		_groundTruthFileNames.add(fileName);
		return true;
	}

	public static void main(String[] args) {
		ResultAnalyzer analyzer = new ResultAnalyzer();
		analyzer.writeToResultFile("", false);
		
		generateTestImageFilepaths(PATH_OUTPUT_FPATHS);
		
		
		Vector <String> testImages = analyzer.getTestImageFilePaths(PATH_OUTPUT_FPATHS);
		
				
		
		/*
		VisualConceptGenerator concept = VisualConceptGenerator.getObject();

		analyzer.writeToResultFile("#analyzing using concepts only\n\n");
		
		for (int i = 0; i < testImages.size(); i++) {
			String testImage = testImages.get(i);
			File file = new File(testImage);
			TreeSet<String> resultImageSet = concept.getTreeSetResultImageList(file);
			if (resultImageSet == null) {
				analyzer.writeToResultFile("error in reading file\n");
				continue;
			}
			
			StatisticObject object = analyzer.generateAnalysisResult(file, resultImageSet);
		}
		*/

		/*
		
		TextRecognizer text = TextRecognizer.getObject();

		Vector <StatisticObject> statisticalObjectsText = new Vector <StatisticObject>();

		analyzer.writeToResultFile("\n\n\n#analyzing using text only\n\n");
		
		for (int i = 0; i < testImages.size(); i++) {
			String testImage = testImages.get(i);
			File file = new File(testImage);
			TreeSet<String> resultImageSet = text.getTreeSetResultImageList(file);
			if (resultImageSet == null) {
				analyzer.writeToResultFile("error in reading file\n");
				continue;
			}
			
			StatisticObject object = analyzer.generateAnalysisResult(file, resultImageSet);
			statisticalObjectsText.add(object);
		}
		String outputLine = AverageAndSdCalculator.getStatisticResult(statisticalObjectsText);
		analyzer.writeToResultFile(outputLine);
		*/
		

		/*
		SiftFeatureComparer sift = SiftFeatureComparer.getObject();
		Vector <StatisticObject> statisticalObjectsSift = new Vector <StatisticObject>();

		
		analyzer.writeToResultFile("\n\n\n#analyzing using visual words only\n\n");
		
		for (int i = 0; i < testImages.size(); i++) {
			String testImage = testImages.get(i);
			File file = new File(testImage);
			TreeSet<String> resultImageSet = sift.getTreeSetResultImageList(file);
			if (resultImageSet == null) {
				analyzer.writeToResultFile("error in reading file\n");
				continue;
			}
			
			StatisticObject object = analyzer.generateAnalysisResult(file, resultImageSet);
			statisticalObjectsSift.add(object);
		}
		String outputLine = AverageAndSdCalculator.getStatisticResult(statisticalObjectsSift);
		analyzer.writeToResultFile(outputLine);
		*/
		
		ColorHist colorHist = ColorHist.getObject();
		Vector <StatisticObject> statisticalObjectsColor = new Vector <StatisticObject>();

		
		analyzer.writeToResultFile("\n\n\n#analyzing using color histograms only\n\n");
		
		for (int i = 0; i < testImages.size(); i++) {
			String testImage = testImages.get(i);
			File file = new File(testImage);
			TreeSet<String> resultImageSet = colorHist.getTreeSetResultImageList(file);
			if (resultImageSet == null) {
				analyzer.writeToResultFile("error in reading file\n");
				continue;
			}
			
			StatisticObject object = analyzer.generateAnalysisResult(file, resultImageSet);
			statisticalObjectsColor.add(object);
		}
		String outputLine = AverageAndSdCalculator.getStatisticResult(statisticalObjectsColor);
		analyzer.writeToResultFile(outputLine);
	}
	
}
