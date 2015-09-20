import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Vector;


public class SiftFeatureComparer {
	
	private static final String PATH_SIFT_FILES = "ImageData/train/";
	private static final String FILE_CODEBOOK_B = "codebook_b.file";
	private static final String FILE_SIFT_BINARY = "generate.py";
	
	private static final int ERROR_NUM_OF_VISUAL_WORDS = -1;
	private static final double NO_FREQUENCY = 0.000000;
	
	private static SiftFeatureComparer _sift = null;
	
	private Vector <Double> _histogramOfCurrentImage;
	
	private int _numOfVisualWords;
	
	private SiftFeatureComparer() {
		_numOfVisualWords = ERROR_NUM_OF_VISUAL_WORDS;
	}
	
	public static SiftFeatureComparer getObject() {
		if (_sift == null) {
			_sift = new SiftFeatureComparer();
		}
		return _sift;
	}
	
	public void searchBySift(File file) {
		File currentDirectory = new File(new File(".").getAbsolutePath());
		String currentPath = null;
		
		try {
			currentPath = currentDirectory.getCanonicalPath();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			return;
		}
		
		
		String option = " -c ";
		String codebookBPath = currentPath + "\\" + PATH_SIFT_FILES + FILE_CODEBOOK_B;
		
		String imagePath = file.getAbsolutePath();
		try {
			Process process = Runtime.getRuntime().exec("python " + FILE_SIFT_BINARY + option + codebookBPath + " " + imagePath);
			InputStream is = process.getInputStream();
			InputStreamReader isr = new InputStreamReader(is);
			BufferedReader br = new BufferedReader(isr);
			String line;

			while ((line = br.readLine()) != null) {
			  System.out.println(line);
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	private boolean extractWordHistograms(String fileVisualWordHistogram) {
		try {
			// Open the file
			FileInputStream fstream = new FileInputStream(fileVisualWordHistogram);
			BufferedReader br = new BufferedReader(new InputStreamReader(fstream));

			String strLine;

			//Read File Line By Line
			while ((strLine = br.readLine()) != null)   {
			  // Print the content on the console
				if (!processLine(strLine)) {
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
	
	private boolean processLine(String line) {
		String[] visualWords = line.split(" ");
		
		if (_numOfVisualWords == ERROR_NUM_OF_VISUAL_WORDS) {
			_numOfVisualWords = visualWords.length;
		} else if (_numOfVisualWords != visualWords.length) {
			return false;
		} else {
			//ntg happened
		}
		
		_histogramOfCurrentImage = new Vector <Double>();
		for (int i = 0; i < visualWords.length; i++) {
			visualWords[i] = visualWords[i].split(":")[1].trim();
			Double frequency = Double.parseDouble(visualWords[i]);
			_histogramOfCurrentImage.add(frequency);
		}
		
		
		return true;
	}
}
