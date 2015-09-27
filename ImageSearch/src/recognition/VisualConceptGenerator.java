package recognition;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class VisualConceptGenerator {
	private static final String PATH_PYTHON_RESULT = "Result.py";
	private static final String PATH_DICT = "ImageData/train/dictionary.txt"; 
	
	/**
	 * This class will requires filePath of the provided image in order to generate a input.txt
	 * for image_classification.exe to generate list of scores of 1000-concepts. It will then run 
	 * image_classification.exe. 
	 */
	public void classifyInputImage(String filePath) {
		BufferedWriter bw;
		try {
			bw = new BufferedWriter(new FileWriter("SemanticFeature/input.txt"));
			bw.write(filePath);
			bw.close();
		} catch (IOException e) {
			System.out.println("Error writing filepath of image into SemanticFeature/input.txt");
		}
		
		try {
			@SuppressWarnings("unused")
			Process classification = new ProcessBuilder(
					"SemanticFeature/image_classification.exe", "input.txt")
					.start();
		} catch (IOException e) {
			System.out.println("Error calling image_classification.exe");
		}
		
		generateResult(filePath);
	}

	/**
	 * This class will run the python script to generate a list of jpg filepaths. The order corresponds
	 * to the ranking of each images sorted by their scores by Visual Concept.
	 */
	private void generateResult(String filePath) {
		String filePathTxt = filePath.replace(".jpg", ".txt");
		try {
			Process process = Runtime.getRuntime().exec("python " + PATH_PYTHON_RESULT + " -d " + PATH_DICT + " -s " + filePathTxt);
			InputStream is = process.getInputStream();
			InputStreamReader isr = new InputStreamReader(is);
			BufferedReader br = new BufferedReader(isr);
			String line;
			
			while ((line = br.readLine()) != null) {
				System.out.println(line);
			}
		} catch (IOException e) {
			System.out.println("Error calling Python Result.py script!");
		}
	}
}
