package recognition;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

public class VisualConceptGenerator {
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
			Process classification = new ProcessBuilder(
					"SemanticFeature/image_classification.exe", "input.txt")
					.start();
		} catch (IOException e) {
			System.out.println("Error calling image_classification.exe");
		}
		
	}
	
	
}
