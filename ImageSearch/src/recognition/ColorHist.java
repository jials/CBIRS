package recognition;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.io.File;
import java.io.IOException;
import java.util.TreeSet;
import java.util.Vector;

import javax.imageio.ImageIO;

public class ColorHist {
	int dim = 64;

	private static final String[] KEYWORDS = {"bear", "birds", "boats",
	 	  "cars", "cat", "computer", "coral", 
	 	  "dog",
	 	  "fish", "flags", "flowers", 
	 	  "horses", 
	 	  "leaf", 
	 	  "plane",
	 	  "rainbow", "rocks",
	 	  "sign", "snow",
	 	  "tiger", "tower", "train", "tree", 
	 	  "whales", "window",
	 	  "zebra"};
	private static final int TOPN = 20;
	
	private static ColorHist _colorHist = null;
	
	private String _datasetpath = "ImageData/train/data/";
	
	private ColorHist() {
	}
	
	public static ColorHist getObject() {
		if (_colorHist == null) {
			_colorHist = new ColorHist();
		}
		return _colorHist;
	}
	
	public BufferedImage[] searchByColorHistogram(File file) throws IOException {
		BufferedImage bufferedimage = ImageIO.read(file);
		BufferedImage[] imgs = null;

		//imgs = colorhist.search(datasetpath + KEYWORDS[0], bufferedimage, resultsize);
		Vector <File> fileVector = new Vector <File>();
		for (int i = 0; i < KEYWORDS.length; i++) {
			String datasetpath = _datasetpath + KEYWORDS[i];
			File dir = new File(datasetpath);  //path of the dataset
			File [] files = dir.listFiles();
			for (int j = 0; j < files.length; j++) {
				fileVector.add(files[j]);
			}
		}
		
		imgs = search(fileVector, bufferedimage, TOPN);

		return imgs;
	}
	
	public TreeSet<String> getTreeSetResultImageList(Vector <File> fileVector, File file) {
		BufferedImage bufferedimage;
		try {
			bufferedimage = ImageIO.read(file);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			return null;
		}
		
		double[] hist = getHist(bufferedimage);
    	
    	
		
		double[] sims = new double [fileVector.size()];
		int [] indexes = new int [fileVector.size()];
		
		
		/*ranking the search results*/
		for (int count=0; count < fileVector.size() ;count++){
			BufferedImage i = null;
			try {
				i = ImageIO.read(fileVector.get(count));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				continue;
			}
			
			if (i == null) {
				continue;
			}
			
			double[] h = getHist(i);
			double sim = computeSimilarity (hist, h);
			if (count == 0){
				sims[count] = sim;
				indexes [count] = count;
			}
			else {
				int index;
				for (index =0; index < count; index ++){
					if (sim > sims[index])
						break;
				}
				for (int j = count - 1; j > index - 1; j--){
					sims [j+1] = sims [j];
					indexes [j+1] = indexes[j];
				}
				sims[index] = sim;
				indexes[index] = count;
			}
		}
		
		TreeSet <String> resultSet = new TreeSet <String>();
		for (int i = 0; i < Math.min(TOPN, indexes.length); i++) {
			int index = indexes[i];
			String filePath = fileVector.get(index).getAbsolutePath();
			resultSet.add(filePath);
		}
		return resultSet;
	}

	
	
	public TreeSet<String> getTreeSetResultImageList(File file) {
		

		//imgs = colorhist.search(datasetpath + KEYWORDS[0], bufferedimage, resultsize);
		Vector <File> fileVector = new Vector <File>();
		for (int i = 0; i < KEYWORDS.length; i++) {
			String datasetpath = _datasetpath + KEYWORDS[i];
			File dir = new File(datasetpath);  //path of the dataset
			File [] files = dir.listFiles();
			for (int j = 0; j < files.length; j++) {
				fileVector.add(files[j]);
			}
		}
		
		
		return getTreeSetResultImageList(fileVector, file);
	}
	
	public BufferedImage[] search(Vector<File> files, File bufferedimageFile) throws IOException{
		return search(files, bufferedimageFile, TOPN);
	}
	
	
	public BufferedImage[] search(Vector<File> files, File bufferedimageFile, int resultsize) throws IOException{
		BufferedImage bufferedimage;
		try {
			bufferedimage = ImageIO.read(bufferedimageFile);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			return null;
		}
		return search(files, bufferedimage, resultsize);
	}
	
	public BufferedImage[] search(Vector<File> files, BufferedImage bufferedimage, int resultsize) throws IOException{
    	double[] hist = getHist(bufferedimage);
    	
    	
		
		double[] sims = new double [files.size()];
		int [] indexes = new int [files.size()];
		
		
		/*ranking the search results*/
		for (int count=0; count < files.size() ;count++){
			BufferedImage i = ImageIO.read(files.get(count));
			
			if (i == null) {
				continue;
			}
			
			double[] h = getHist(i);
			double sim = computeSimilarity (hist, h);
			if (count == 0){
				sims[count] = sim;
				indexes [count] = count;
			}
			else {
				int index;
				for (index =0; index < count; index ++){
					if (sim > sims[index])
						break;
				}
				for (int j = count - 1; j > index - 1; j--){
					sims [j+1] = sims [j];
					indexes [j+1] = indexes[j];
				}
				sims[index] = sim;
				indexes[index] = count;
			}
		}
		    	
    	BufferedImage[] imgs = new BufferedImage[resultsize];
		for (int i=0; i<resultsize;i++){
			imgs [i]=ImageIO.read(files.get(indexes[i]));
		}
		
    	return imgs;
    }
    
    public double computeSimilarity(double [] hist1, double [] hist2) {
		
		double distance = calculateDistance(hist1, hist2);
		return 1-distance;
	}
	
	public double[] getHist(BufferedImage image) {
		int imHeight = image.getHeight();
        int imWidth = image.getWidth();
        double[] bins = new double[dim*dim*dim];
        int step = 256 / dim;
        Raster raster = image.getRaster();
        for(int i = 0; i < imWidth; i++)
        {
            for(int j = 0; j < imHeight; j++)
            {
            	// rgb->ycrcb
            	int r = raster.getSample(i,j,0);
            	int g = raster.getSample(i,j,1);
            	int b = raster.getSample(i,j,2);
            	
            	//Changed Codes. 
            	int y  = (int)( 0 + 0.299   * r + 0.587   * g + 0.114   * b);
        		int cb = (int)(128 -0.16874 * r - 0.33126 * g + 0.50000 * b);
        		int cr = (int)(128 + 0.50000 * r - 0.41869 * g - 0.08131 * b);
        		
        		int ybin = y / step;
        		int cbbin = cb / step;
        		int crbin = cr / step;

        		//Changed Codes. 
                bins[ybin*dim*dim+cbbin*dim+crbin] ++;
            }
        }
        
        //Changed Codes. 
        for(int i = 0; i < dim*dim*dim; i++) {
        	bins[i] = bins[i]/(imHeight*imWidth);
        }
        
        return bins;
	}
	
	public double calculateDistance(double[] array1, double[] array2)
    {
		// Euclidean distance
		/*
        double Sum = 0.0;
        for(int i = 0; i < array1.length; i++) {
           Sum = Sum + Math.pow((array1[i]-array2[i]),2.0);
        }
        return Math.sqrt(Sum);
        */
        
        // Bhattacharyya distance
		double h1 = 0.0;
		double h2 = 0.0;
		int N = array1.length;
        for(int i = 0; i < N; i++) {
        	h1 = h1 + array1[i];
        	h2 = h2 + array2[i];
        }

        double Sum = 0.0;
        for(int i = 0; i < N; i++) {
           Sum = Sum + Math.sqrt(array1[i]*array2[i]);
        }
        double dist = Math.sqrt( 1 - Sum / Math.sqrt(h1*h2));
        return dist;
    }
}
