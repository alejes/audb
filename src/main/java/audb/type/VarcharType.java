package audb.type;

import java.nio.charset.StandardCharsets;


public class VarcharType extends Type {

    private int length;

    public VarcharType(int length) throws Exception {
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

    public Object fromBytes(byte[] bytes) {
        return new String(bytes, StandardCharsets.US_ASCII);
    }

    public boolean isValid(Object o) {
        return (o instanceof String && ((String)o).length() == length);
    }

}
