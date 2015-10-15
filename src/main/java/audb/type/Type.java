package audb.type;


public interface Type {

	public final static int INT = 1;
	public final static int DOUBLE = 2;
	public final static int STRING = 3;

    public int getType();
    public int getSize();
    public Object read(byte[] data, int offset);
    public void write(byte[] data, int offset, Object o); 

}
