package audb.table;

import java.nio.charset.StandardCharsets;

import audb.type.Type;
import audb.type.VarcharType;

public class VarcharElement implements TableElement {
	public final String value;
	public final VarcharType type;
	
	public VarcharElement(String s, VarcharType t) {
		value = s;
		type = t;
	}
	
	public String toString() {
		return value;
	}
	
	public int getSizeInBytes() {
		return type.getSize();
	}
	
    public byte[] toBytes() {
    	// TODO
        return value.getBytes(StandardCharsets.US_ASCII);
    }
	
	public int compareTo(TableElement other) {
		if (!(other instanceof VarcharElement)) {
			throw new ClassCastException("Can not cast not VarcharElement to VarcharElement");
		}
		
		String tmp = ((VarcharElement)other).value;
		return value.compareTo(tmp);
	}

	@Override
	public Type getType() {
		return type;
	}
	
}
