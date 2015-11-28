package audb.table;


public class DoubleElement implements TableElement {
	final Double value;
	
	public DoubleElement(double v) {
		value = v;
	}
	
	public String toString() {
		return value.toString();
	}
	
	public int compareTo(TableElement other) {
		if (other instanceof VarcharElement) {
			throw new ClassCastException("Can not cast VarcharElement to numeric elemnt");
		}
		
		if (other instanceof DoubleElement) {
			return value.compareTo(((DoubleElement)other).value);
		}
		
		return value.compareTo(((IntegerElement)other).value.doubleValue());
	}

	public int getSizeInBytes() {
		return Double.BYTES;
	}
}
