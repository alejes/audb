package audb.table;

import java.nio.charset.StandardCharsets;

public class VarcharElement implements TableElement {
	private final String value;
	
	public VarcharElement(String s) {
		value = s;
	}
	
	public String toString() {
		return value;
	}
	
	
    public byte[] toBytes() throws Exception {
        return value.getBytes(StandardCharsets.US_ASCII);
    }
	
	public int compareTo(TableElement other) {
		if (!(other instanceof VarcharElement)) {
			throw new ClassCastException("Can not cast not VarcharElement to VarcharElement");
		}
		
		String tmp = ((VarcharElement)other).value;
		return value.compareTo(tmp);
	}
	
}
