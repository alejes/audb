package audb.type;

import java.nio.charset.StandardCharsets;

import audb.table.TableElement;
import audb.table.VarcharElement;


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
        if(bytes.length != length)
            throw new Exception("VarcharType.java");
        return bytes;
    }

    public boolean isValid(Object o) {
        return (o instanceof String && ((String)o).length() == length);
    }
    
    public TableElement fromBytes(byte[] data) {
		return new VarcharElement(new String(data, StandardCharsets.US_ASCII), this);
	}
    
    public TableElement fromObject(Object obj) {
    	return new VarcharElement((String)obj, this);
    }

}
