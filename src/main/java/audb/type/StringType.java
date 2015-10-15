package audb.type;


public class StringType implements Type {

	private static final int STRING_SIZE = 20;

    public int getType() {
    	return STRING;
    }

    public int getSize() {
    	return STRING_SIZE;
    }

    public Object read(byte[] data, int offset) {

    	return null;
    }

    public void write(byte[] data, int offset, Object o) {

    }

}
