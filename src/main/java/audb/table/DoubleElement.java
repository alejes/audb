package audb.table;

import audb.page.Page;
import audb.type.Type;

public class DoubleElement implements TableElement {
	public final Double value;
	public final Type type;
	
	public DoubleElement(double v, Type type) {
		value = v;
		this.type = type;
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

	@Override
	public byte[] toBytes() {
		return Page.doubleToBytes(value);
	}

	@Override
	public Type getType() {
		return type;
	}
}
