package audb.table;

public class VarcharElement implements TableElement {
	private final String value;
	private final int length;
	
	public VarcharElement(String s, int length) {
		value = s;
		this.length = length;
	}
	
	public String toString() {
		return value;
	}
	
	public int getLength() {
		return length;
	}
	
	public int compareTo(TableElement other) {
		if (!(other instanceof VarcharElement)) {
			throw new ClassCastException("Can not cast not VarcharElement to VarcharElement");
		}
		
		String tmp = ((VarcharElement)other).value;
		return value.compareTo(tmp);
	}
	
}
