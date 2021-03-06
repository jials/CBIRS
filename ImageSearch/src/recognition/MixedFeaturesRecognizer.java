package recognition;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Vector;

import javax.imageio.ImageIO;


public class MixedFeaturesRecognizer {
	private static MixedFeaturesRecognizer _mixedFeaturesRecognizer = null;
	private final int TOP_KNN = 20;
	
	private final static double CONSTANT_CONCEPT = 0.3;
	private final static double CONSTANT_TEXT = 0.2;
	private final static double CONSTANT_COLOR = 0.1;
	private final static double CONSTANT_SIFT = 0.1;
	
	private MixedFeaturesRecognizer() {
	}
	
	public static MixedFeaturesRecognizer getObject() {
		if (_mixedFeaturesRecognizer == null) {
			_mixedFeaturesRecognizer = new MixedFeaturesRecognizer();
		}
		return _mixedFeaturesRecognizer;
	}
	
	public BufferedImage[] searchByConceptsAndSift(File file) throws IOException {
		TreeSet<String> resultImageList = extractTreeSetConceptsSift(file);
		
		int size = Math.min(resultImageList.size(), TOP_KNN);
		
		BufferedImage[] imgs = new BufferedImage[TOP_KNN];
		for (int i = 0; i < size; i++) {
			String imgPath = resultImageList.pollFirst();
			File imgFile = new File(imgPath);
			imgs[i] = ImageIO.read(imgFile);
		}
		return imgs;
	}
	
	/**
	 * @param file
	 * @return
	 */
	public TreeSet<String> extractTreeSetConceptsText(File file) {
		VisualConceptGenerator concept = VisualConceptGenerator.getObject();
		TextRecognizer text = TextRecognizer.getObject();
		
		TreeSet<String> initialImageList = concept.getTreeSetResultImageList(file);
		TreeSet<String> nextImageList = text.getTreeSetResultImageList(file);

		TreeMap <String, Integer> nameIndexMap = new TreeMap<String, Integer>();
		Vector <DoubleStringPair> fileWeight = new Vector <DoubleStringPair>();
		
		int weight = initialImageList.size();
		while (!initialImageList.isEmpty()) {
			String imagePath = initialImageList.pollFirst();
			if (nameIndexMap.containsKey(imagePath)) {
				int index = nameIndexMap.get(imagePath).intValue();
				double curWeight = fileWeight.get(index).getFirst();
				curWeight += weight * CONSTANT_CONCEPT;
				fileWeight.get(index).setFirst(curWeight);
			} else {
				int index = fileWeight.size();
				String fileName = imagePath;
				double curWeight = weight * CONSTANT_CONCEPT;
				DoubleStringPair newPair = new DoubleStringPair(curWeight, fileName);
				fileWeight.add(newPair);
				nameIndexMap.put(fileName, index);
			}
			weight--;
		}
		
		if (nextImageList == null) {
			return initialImageList;
		}
		
		weight = nextImageList.size();
		while (!nextImageList.isEmpty()) {
			String imagePath = nextImageList.pollFirst();
			if (nameIndexMap.containsKey(imagePath)) {
				int index = nameIndexMap.get(imagePath).intValue();
				double curWeight = fileWeight.get(index).getFirst();
				curWeight += weight * CONSTANT_TEXT;
				fileWeight.get(index).setFirst(curWeight);
			} else {
				int index = fileWeight.size();
				String fileName = imagePath;
				double curWeight = weight * CONSTANT_TEXT;
				DoubleStringPair newPair = new DoubleStringPair(curWeight, fileName);
				fileWeight.add(newPair);
				nameIndexMap.put(fileName, index);
			}
			weight--;
		}
		Collections.sort(fileWeight, new DoubleStringPair.SortFirstDouble());
		
		TreeSet<String> resultImageList = new TreeSet<String>();
		
		for (int i = fileWeight.size() - 1; i >= fileWeight.size() - TOP_KNN; i--) {
			resultImageList.add(fileWeight.get(i).getSecond());
			System.out.println(fileWeight.get(i).getFirst());
		}
		return resultImageList;
	}

	/**
	 * @param file
	 * @return
	 */
	public TreeSet<String> extractTreeSetConceptsSift(File file) {
		VisualConceptGenerator concept = VisualConceptGenerator.getObject();
		SiftFeatureComparer sift = SiftFeatureComparer.getObject();
		
		TreeSet<String> initialImageList = concept.getTreeSetResultImageList(file);
		TreeSet<String> nextImageList = sift.getTreeSetResultImageList(file);
		
		TreeMap <String, Integer> nameIndexMap = new TreeMap<String, Integer>();
		Vector <DoubleStringPair> fileWeight = new Vector <DoubleStringPair>();
		
		int weight = initialImageList.size();
		while (!initialImageList.isEmpty()) {
			String imagePath = initialImageList.pollFirst();
			if (nameIndexMap.containsKey(imagePath)) {
				int index = nameIndexMap.get(imagePath).intValue();
				double curWeight = fileWeight.get(index).getFirst();
				curWeight += weight * CONSTANT_CONCEPT;
				fileWeight.get(index).setFirst(curWeight);
			} else {
				int index = fileWeight.size();
				String fileName = imagePath;
				double curWeight = weight * CONSTANT_CONCEPT;
				DoubleStringPair newPair = new DoubleStringPair(curWeight, fileName);
				fileWeight.add(newPair);
				nameIndexMap.put(fileName, index);
			}
			weight--;
		}
		
		weight = nextImageList.size();
		while (!nextImageList.isEmpty()) {
			String imagePath = nextImageList.pollFirst();
			if (nameIndexMap.containsKey(imagePath)) {
				int index = nameIndexMap.get(imagePath).intValue();
				double curWeight = fileWeight.get(index).getFirst();
				curWeight += weight * CONSTANT_SIFT;
				fileWeight.get(index).setFirst(curWeight);
			} else {
				int index = fileWeight.size();
				String fileName = imagePath;
				double curWeight = weight * CONSTANT_SIFT;
				DoubleStringPair newPair = new DoubleStringPair(curWeight, fileName);
				fileWeight.add(newPair);
				nameIndexMap.put(fileName, index);
			}
			weight--;
		}
		Collections.sort(fileWeight, new DoubleStringPair.SortFirstDouble());
		
		TreeSet<String> resultImageList = new TreeSet<String>();
		
		for (int i = 0; i < TOP_KNN; i++) {
			resultImageList.add(fileWeight.get(i).getSecond());
			System.out.println(fileWeight.get(i).getFirst());
		}
		
		return resultImageList;
	}
	
	public BufferedImage[] searchByConceptsAndColor(File file) throws IOException {
		TreeSet<String> resultImageList = extractTreeSetConceptsColor(file);
		
		int size = Math.min(resultImageList.size(), TOP_KNN);
		
		BufferedImage[] imgs = new BufferedImage[TOP_KNN];
		for (int i = 0; i < size; i++) {
			String imgPath = resultImageList.pollFirst();
			File imgFile = new File(imgPath);
			imgs[i] = ImageIO.read(imgFile);
		}
		return imgs;
	}

	/**
	 * @param file
	 * @return
	 */
	public TreeSet<String> extractTreeSetConceptsColor(File file) {
		VisualConceptGenerator concept = VisualConceptGenerator.getObject();
		ColorHist color = ColorHist.getObject();
		
		TreeSet<String> initialImageList = concept.getTreeSetResultImageList(file);
		
		Vector<StringIntegerPair> pairs = new Vector<StringIntegerPair>();
		TreeMap<String, Integer> hasPath = new TreeMap<String, Integer>();
		
		System.out.println(initialImageList.size());
		
		String filePath = null;
		while (!initialImageList.isEmpty()) {
			String head = initialImageList.pollFirst();
			int fileNameStart = head.lastIndexOf("\\");
			String fileInPath = head.substring(0, fileNameStart);
			
			if (hasPath.containsKey(fileInPath)) {
				int index = hasPath.get(fileInPath).intValue();
				pairs.get(index).incrementInteger();
			} else {
				StringIntegerPair pair = new StringIntegerPair(fileInPath);
				hasPath.put(fileInPath, pairs.size());
				pairs.add(pair);
			}
		}
		Collections.sort(pairs, new StringIntegerPair.SortInteger());
		
		if (pairs.size() == 0) {
			return color.getTreeSetResultImageList(file);
		}
		filePath = pairs.get(0).getString();
		
		System.out.println("2: " + filePath);
		
		File fileDir = new File(filePath);
		File[] fileArray = fileDir.listFiles();
		Vector <File> files = new Vector <File>();
		
		for (int i = 0; i < fileArray.length; i++) {
			String tempFileName = fileArray[i].getAbsolutePath();
			System.out.println("1: " + tempFileName);

			
			if (!tempFileName.endsWith(".jpg")) {
				continue;
			}
			
			
			files.add(fileArray[i]);
		}
		
		TreeSet<String> resultImageList = color.getTreeSetResultImageList(files, file);
		return resultImageList;
	}
	
}
