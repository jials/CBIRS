package learning;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class VisualConceptLearner {
	
	private static final String PATH_PYTHON_LEARNER = "VisualConceptLearner.py";
	private static final String PATH_PYTHON_FPGENERATOR = "src/learning/FilepathsGenerator.py";
	private static final String PATH_TRAIN = "ImageData/train/data";
	private static final String PATH_OUTPUT_DICT = "ImageData/train/dictionary.txt";
	private static final String PATH_OUTPUT_FPATHS = "semanticFeature/data.txt";
	
	public void classifyDatabase() {
		generateFilepaths();
		try {			
			Process classification = Runtime.getRuntime().exec("semanticFeature/image_classification.exe data.txt", null, 
					new File("semanticFeature/"));
			
			InputStream is = classification.getInputStream();
			InputStreamReader isr = new InputStreamReader(is);
			BufferedReader br = new BufferedReader(isr);
			String line;
			
			while ((line = br.readLine()) != null) {
				System.out.println(line);
			}
			
			generateVisualConceptDict();

		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("Error calling image_classification.exe");
		}
	}

	private void generateFilepaths() {
		try {
			Process process = Runtime.getRuntime().exec("python " + PATH_PYTHON_FPGENERATOR + " -t " + PATH_TRAIN + " -c " + PATH_OUTPUT_FPATHS);
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

	/**
	 * Go through the results generated by image_classification.exe (txt files
	 * containing 1000 integers per image) and extract out index of concepts
	 * with positive score. Also generate compiled.txt which has all the scores for each
	 * img
	 */
	private void generateVisualConceptDict() {
		try {
			Process process = Runtime.getRuntime().exec("python " + PATH_PYTHON_LEARNER + " -t " + PATH_TRAIN + " -c " + PATH_OUTPUT_DICT);
			InputStream is = process.getInputStream();
			InputStreamReader isr = new InputStreamReader(is);
			BufferedReader br = new BufferedReader(isr);
			String line;
			
			while ((line = br.readLine()) != null) {
				System.out.println(line);
			}
		} catch (IOException e) {
			System.out.println("Error calling Python VisualConceptLearner script!");
		}
	}
	
	public static void main(String[] args) {
		VisualConceptLearner visual = new VisualConceptLearner();
		visual.classifyDatabase();
	}
}
