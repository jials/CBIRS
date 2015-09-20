import java.util.Comparator;


public class DoubleIntegerPair {
	public static class SortFirstDouble implements Comparator <DoubleIntegerPair> { 

		@Override
		public int compare(DoubleIntegerPair o1, DoubleIntegerPair o2) {
			if (o2.getFirst() - o1.getFirst() < 0) {
				return -1;
			} 

			return 1;
		}
	}
	
	private double _first;
	private int _second;
	
	public DoubleIntegerPair(double first, int second) {
		setFirst(first);
		setSecond(second);
	}

	public double getFirst() {
		return _first;
	}

	public void setFirst(double first) {
		this._first = first;
	}

	public int getSecond() {
		return _second;
	}

	public void setSecond(int second) {
		this._second = second;
	}
}
