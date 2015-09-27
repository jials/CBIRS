package analysis;

public class StatisticObject {
	private double _precision = -1;
	private double _recall = -1;
	private double _f1 = -1;
	private int _n = -1;
	private double _mapN = -1;
	
	public StatisticObject() {
	}
	
	public StatisticObject(double precision,
						   double recall,
						   double f1,
						   int n,
						   double mapN) {
		setPrecision(precision);
		setRecall(recall);
		setF1(f1);
		setN(n);
		setMapN(mapN);
	}
	
	public boolean isNotReady() {
		boolean isNotPrecision = _precision < 0;
		boolean isNotRecall = _recall < 0;
		boolean isNotF1 = _f1 < 0;
		boolean isNotN = _n < 0;
		boolean isNotMapN = _mapN < 0;
		return isNotPrecision || isNotRecall || isNotF1 || isNotN || isNotMapN;
	}
	
	public void setPrecision(double precision) {
		_precision = precision;
	}
	
	public void setRecall(double recall) {
		_recall = recall;
	}
	
	public void setF1(double f1) {
		_f1 = f1;
	}
	
	public void setN(int n) {
		_n = n;
	}
	
	public void setMapN(double mapN) {
		_mapN = mapN;
	}
	
	public double getPrecision() {
		return _precision;
	}
	
	public double getRecall() {
		return _recall;
	}
	
	public double getF1() {
		return _f1;
	}
	
	public int getN() {
		return _n;
	}
	
	public double getMapN() {
		return _mapN;
	}
}
