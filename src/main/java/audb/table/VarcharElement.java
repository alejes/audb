package audb.table;

import audb.type.Type;
import audb.type.VarcharType;

public class VarcharElement implements TableElement {
	public final String value;
	public final VarcharType type;
	
	public VarcharElement(String s, VarcharType t) {
		value = s;
		type = t;
		assert (type.isValid(s));
	}
	
	public String toString() {
		return "VARCHAR (" + type.getSize() + ") " + value;
	}
	
	public int getSizeInBytes() {
		return type.getSize();
	}

	public Object getObject() {
		return value;
	}
	
    public byte[] toBytes() {
    	// TODO
    	try {
        return type.toBytes(value);
    	} catch (Exception e) {
    		return null;
    	}
    }

	public String showString() {
		return value;
	}

	@Override
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
