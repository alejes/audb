package audb.table;

import audb.page.Page;
import audb.type.Type;

public class IntegerElement implements TableElement {
	final Type type;
	public final Integer value;
	
	public IntegerElement(int value, Type type) {
		this.value = value;
		this.type = type;
	}
	
	public String toString() {
		return value.toString();
	}
	
	public int compareTo(TableElement other) {
		return value.compareTo(((IntegerElement)other).value);
	}

	public int getSizeInBytes() {
		return Integer.BYTES;
	}

	@Override
	public byte[] toBytes() {
		return Page.intToBytes(value);
	}

	@Override
	public Type getType() {
		return type;
	}
	
	

}
