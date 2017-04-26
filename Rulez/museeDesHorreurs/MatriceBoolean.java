package math;

public class MatriceBoolean {
	public int dimension;
	public boolean[][] values;

	public MatriceBoolean(int x) {
		if (x > 0 ) {
			dimension = x;
			
			values = new boolean[x][x];
		} else
			throw new java.lang.ArrayIndexOutOfBoundsException();
	}

	/*
	 * Values sous la forme "00111010101010101", série de valeur de la m
	 */
	public MatriceBoolean(int dimension, String valuesIn) {
		this(dimension);
		int count = 0;

		if (valuesIn.length() != dimension * dimension) {
			throw new java.lang.ArrayIndexOutOfBoundsException();
		}

		for (int j = 0; j < dimension; j++) {
			for (int k = 0; k < dimension; k++) {
				values[j][k] = (valuesIn.charAt(count) == '1' ? true : false);
			}
		}
	}
}
