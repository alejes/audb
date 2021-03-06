package audb.type;

import audb.page.Page;
import audb.table.TableElement;
import audb.table.VarcharElement;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;


public class VarcharType extends Type {

    private final int length;

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
    	return length + Integer.BYTES;
    }
    
    public static byte[] concat(byte[] first, byte[] second) {
    	  byte[] result = Arrays.copyOf(first, first.length + second.length);
    	  System.arraycopy(second, 0, result, first.length, second.length);
    	  return result;
    	}

    public byte[] toBytes(Object o) throws Exception {
    	String s = (String)o;
    	byte[] size = Page.intToBytes(s.length()); 
        byte[] bytes = Arrays.copyOf(((String)o).getBytes(StandardCharsets.US_ASCII), length);
        
        byte[] result = concat(size, bytes);
        
        assert(result.length == getSize());
        for (int i = 0; i < Integer.BYTES; i++) {
        	assert(result[i] == size[i]);
        }
        return result;
    }

    public boolean isValid(Object o) {
        return (o instanceof String && ((String)o).length() <= length);
    }
    
    public TableElement fromBytes(byte[] data) {
    	int length = Page.bytesToInt(data);
    	
    	if (length > this.length)
    	{
    		// System.out.println("length read is " + Integer.toString(length) + " and actual length should be <= " 
    		// 		+ Integer.toString(this.length));
    		// new Exception().printStackTrace();
    		length = this.length;
    	}
    		
    	byte[] bytes = new byte[length];
    	System.arraycopy(data, Integer.BYTES, bytes, 0, length);
		return new VarcharElement(new String(bytes, StandardCharsets.US_ASCII), this);
	}
    
    public TableElement fromObject(Object obj) {
    	return new VarcharElement((String)obj, this);
    }


    @Override
    public String toString() {
        return "VARCHAR " + this.length;
    }
}
