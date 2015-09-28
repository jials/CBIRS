package recognition;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.TreeSet;
import java.util.Vector;

import javax.imageio.ImageIO;

public class VisualConceptGenerator {
	private static final String PATH_PYTHON_RESULT = "src/recognition/Result.py";
	private static final String PATH_DICT = "ImageData/train/dictionary.txt"; 
	private static final int TOP_20_RESULTS = 20;
	
	private static VisualConceptGenerator _concepts = null;
	private Vector<String> _results;
	
	private VisualConceptGenerator() {
		_results = new Vector<String>();
	}
	
	public static VisualConceptGenerator getObject() {
		if (_concepts == null) {
			_concepts = new VisualConceptGenerator();
		}
		return _concepts;
	}
	
	public TreeSet<String> getTreeSetResultImageList(File file) {
		if (!processResult(file)) {
			return null;
		}
		
		TreeSet <String> treeSetResultImageList = new TreeSet <String>();
		for (int i = 0; i < _results.size(); i++) {
			treeSetResultImageList.add(_results.get(i));
		}
		
		return treeSetResultImageList;

	}

	/**
	 * @param file
	 */
	public boolean processResult(File file) {
		_results.clear();
		
		String filePath = file.getAbsolutePath();
		BufferedWriter bw;
		try {
			bw = new BufferedWriter(new FileWriter("semanticFeature/input.txt"));
			bw.write(filePath);
			bw.close();
		} catch (IOException e) {
			System.out.println("Error writing filepath of image into semanticFeature/input.txt");
			return false;
		}
		
		try {
			Process classification = Runtime.getRuntime().exec("semanticFeature/image_classification.exe input.txt", null, 
					new File("semanticFeature/"));
			InputStream is = classification.getInputStream();
			InputStreamReader isr = new InputStreamReader(is);
			BufferedReader br = new BufferedReader(isr);
			String line;
			
			while ((line = br.readLine()) != null) {
				System.out.println(line);
			}
			generateResult(filePath);

		} catch (IOException e) {
			System.out.println("Error calling image_classification.exe");
			return false;
		}
		
		return true;
	}
	
	/**
	 * This class will requires filePath of the provided image in order to generate a input.txt
	 * for image_classification.exe to generate list of scores of 1000-concepts. It will then run 
	 * image_classification.exe. 
	 * @throws IOException 
	 */
	public BufferedImage[] classifyInputImage(File file) throws IOException {
		if (!processResult(file)) {
			return null;
		}

		
		BufferedImage imgs[] = new BufferedImage[TOP_20_RESULTS];
		for (int i = 0; i < Math.min(_results.size(), imgs.length); i++) {
			File imgFile = new File(_results.get(i));
			imgs[i] = ImageIO.read(imgFile);			
		}
		return imgs;
	}

	/**
	 * This class will run the python script to generate a list of jpg filepaths. The order corresponds
	 * to the ranking of each images sorted by their scores by Visual Concept.
	 */
	private void generateResult(String filePath) {
		String filePathTxt = filePath.replace(".jpg", ".txt");
		System.err.println("Concept: generateResult: filePath: " + filePath);
		try {
			Process process = Runtime.getRuntime().exec("python " + PATH_PYTHON_RESULT + " -d " + PATH_DICT + " -s " + filePathTxt);
			InputStream is = process.getInputStream();
			InputStreamReader isr = new InputStreamReader(is);
			BufferedReader br = new BufferedReader(isr);
			String line;
			
			while ((line = br.readLine()) != null) {
				System.out.println(line);
				line = line.replace(".txt", ".jpg");
				_results.add(line);
			}
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("Error calling Python Result.py script!");
		}
	}
	
}
