package analysis;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Vector;

public class AverageAndSdCalculator {
	private final static String PREFIX = "Test";
	private static final String RESULT_FILE_NAME = "ResultAnalysis.txt";
	
	public static String getStatisticResult(Vector <StatisticObject> statisticObjects) {
		AverageAndSdCalculator calculator = new AverageAndSdCalculator();
		return calculator.calculateStatisticResult(statisticObjects);
	}
	
	private AverageAndSdCalculator() {
	}
	
	public void start(String fileName) {
		Vector <StatisticObject> statisticObjects = getStatisticObjects(RESULT_FILE_NAME);
		
		String outputString = calculateStatisticResult(statisticObjects);
		writeToResultFile(outputString);
	}
	
	private String calculateStatisticResult(Vector <StatisticObject> statisticObjects) {
		double avgPrecision = 0, 
			   avgRecall = 0, 
			   avgF1 = 0, 
			   avgN = 0, 
			   avgMapN = 0,
			   varPrecision = 0,
			   varRecall = 0,
			   varF1 = 0,
			   varN = 0,
			   varMapN = 0;
		
		int size = statisticObjects.size();
		
		for (int i = 0; i < size; i++) {
			StatisticObject object = statisticObjects.get(i);
			
			if (object == null) {
				continue;
			}
			
			double precision = object.getPrecision();
			avgPrecision += precision;
			
			double recall = object.getRecall();
			avgRecall += recall;
			
			double f1 = object.getF1();
			avgF1 += f1;
			
			double n = (double) object.getN();
			avgN += n;
			
			double mapN = object.getMapN();
			avgMapN += mapN;
		}
		
		avgPrecision /= (double) size;
		avgRecall /= (double) size;
		avgF1 /= (double) size;
		avgN /= (double) size;
		avgMapN /= (double)size;
		
		for (int i = 0; i < size; i++) {
			StatisticObject object = statisticObjects.get(i);
			
			if (object == null) {
				continue;
			}
			
			double precision = object.getPrecision();
			
			double precisionVarToken =  precision - avgPrecision;
			precisionVarToken = precisionVarToken * precisionVarToken;
			varPrecision += precisionVarToken;
			
			double recall = object.getRecall();
			double recallVarToken =  recall - avgRecall;
			recallVarToken = recallVarToken * recallVarToken;
			varRecall += recallVarToken;
			
			double f1 = object.getF1();
			double f1VarToken =  f1 - avgF1;
			f1VarToken = f1VarToken * f1VarToken;
			varF1 += f1VarToken;
			
			double n = (double) object.getN();
			double nVarToken =  n - avgN;
			nVarToken = nVarToken * nVarToken;
			varN += nVarToken;
			
			double mapN = object.getMapN();
			double mapNVarToken =  mapN - avgMapN;
			mapNVarToken = mapNVarToken * mapNVarToken;
			varMapN += mapNVarToken;
		}
		
		varPrecision /= (double) size;
		varRecall /= (double) size;
		varF1 /= (double) size;
		varN /= (double) size;
		varMapN /= (double) size;
		
		String outputLine = "Analysis:\n" +
							"Precision: (Average) " + avgPrecision + " (Variance) " + varPrecision + "\n" +
							"Recall: (Average) " + avgRecall + " (Variance) " + varRecall + "\n" +
							"F1: (Average) " + avgF1 + " (Variance) " + varF1 + "\n" +
							"N: (Average) " + avgN + " (Variance) " + varN + "\n" +
							"MapN: (Average) " + avgMapN + " (Variance) " + varMapN + "\n";
		System.out.println(outputLine);
		return outputLine;
	}
	
	private Vector <StatisticObject> getStatisticObjects(String filepath) {
		Vector <StatisticObject> statisticObjects = null;
		try {
			// Open the file
			FileInputStream fstream = new FileInputStream(filepath);
			BufferedReader br = new BufferedReader(new InputStreamReader(fstream));

			String line;
			statisticObjects = new Vector <StatisticObject>();

			//Read File Line By Line
			while ((line = br.readLine()) != null)   {
			  // Print the content on the console
				line = line.trim();
				StatisticObject object = processLine(line);
				if (object == null) {
					continue;
				} else {
					statisticObjects.add(object);
				}
			}
			
			//Close the input stream
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		
		return statisticObjects;
	}
	
	private StatisticObject processLine(String line) {
		StatisticObject object = null;
		
		if (line.startsWith(PREFIX)) {
			String[] lineTokens = line.split(" ");
			object = new StatisticObject();
			for (int i = 0; i < lineTokens.length; i++) {
				String token = lineTokens[i].trim();
				if ("Precision:".equals(token)) {
					i++;
					String precisionString = lineTokens[i].trim();
					double precision = Double.parseDouble(precisionString);
					object.setPrecision(precision);
				} else if ("Recall:".equals(token)) {
					i++;
					String recallString = lineTokens[i].trim();
					double recall = Double.parseDouble(recallString);
					object.setRecall(recall);
				} else if ("F1:".equals(token)) {
					i++;
					String f1String = lineTokens[i].trim();
					double f1 = Double.parseDouble(f1String);
					object.setF1(f1);
				} else if ("N:".equals(token)) {
					i++;
					String nString = lineTokens[i].trim();
					int n = Integer.parseInt(nString);
					object.setN(n);
				} else if ("MAP@N:".equals(token)) {
					i++;
					String mapNString = lineTokens[i].trim();
					double mapN = Double.parseDouble(mapNString);
					object.setMapN(mapN);
				}
			}
			if (object.isNotReady()) {
				System.err.println("There are problems!");
				return null;
			}
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
	
	
	public static void main(String[] args) {
		AverageAndSdCalculator calculator = new AverageAndSdCalculator();
		calculator.start(RESULT_FILE_NAME);
	}
}
