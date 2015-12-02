package audb.table;

import audb.type.Type;

public class IntegerElement implements TableElement {
	Type type;
	final Integer value;
	
	public IntegerElement(int value, Type type) {
		this.value = value;
		this.type = type;
	}
	
	public IntegerElement(int value) {
		this.value = value;
	}
	
	public String toString() {
		return value.toString();
	}
	
	public int compareTo(TableElement other) {
		// TODO Auto-generated method stub
		return 0;
	}

	public int getSizeInBytes() {
		return Integer.BYTES;
	}

}
