package audb.type;

import audb.table.TableElement;
import audb.table.VarcharElement;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;


public class VarcharType extends Type {

    private int length;

    public VarcharType(byte length) throws Exception {
    	super(length);
        if(length >= 100 || length <= 0)
            throw new Exception("VarcharType.java");
        this.length = length;
    }

    public byte getId() {
    	return (byte)length;
    }

    public int getSize() {
    	return length;
    }

    public byte[] toBytes(Object o) throws Exception {
        byte[] bytes = ((String)o).getBytes(StandardCharsets.US_ASCII);
        bytes = Arrays.copyOf(bytes, length);
        return bytes;
    }

    public boolean isValid(Object o) {
        return (o instanceof String && ((String)o).length() <= length);
    }
    
    public TableElement fromBytes(byte[] data) {
		return new VarcharElement(new String(data, StandardCharsets.US_ASCII), this);
	}
    
    public TableElement fromObject(Object obj) {
    	return new VarcharElement((String)obj, this);
    }


    @Override
    public String toString() {
        return "VARCHAR " + this.length;
    }
}
