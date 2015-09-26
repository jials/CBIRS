package recognition;

import java.util.Comparator;

public class StringIntegerPair {
	public static class SortInteger implements Comparator <StringIntegerPair> { 

		@Override
		public int compare(StringIntegerPair o1, StringIntegerPair o2) {
			return o2.getInteger() - o1.getInteger();
		}
	}
	
	
	private String _string = null;
	private int _integer = 0;
	
	public StringIntegerPair(String string) {
		setString (string);
	}
	
	public void setString (String string) {
		_string = string;
	}
	
	public void setInteger (int integer) {
		_integer = integer;
	}
	
	public String getString () {
		return _string;
	}
	
	public int getInteger() {
		return _integer;
	}
	
	public void incrementInteger() {
		_integer++;
	}
	
}
