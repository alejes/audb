package audb.type;


public interface Type {

    public byte getId();
    public int getSize();
    public byte[] toBytes(Object o) throws Exception;
    public Object fromBytes(byte[] bytes); 
    public boolean isValid(Object o);

}
