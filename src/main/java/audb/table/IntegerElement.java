package audb.table;

import audb.page.Page;
import audb.type.IntegerType;
import audb.type.Type;

public class IntegerElement implements TableElement {
	public final Integer value;
	final Type type;
	public IntegerElement(int value, Type type) {
		this.value = value;
		this.type = type;
	}

	public IntegerElement(int value) {
		this.value = value;
		this.type = new IntegerType(Type.INT);
	}

	public Object getObject() {
		return value;
	}

	public String showString() {
		return value.toString();
	}

	public String toString() {
		return "INTEGER " + value.toString();
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
