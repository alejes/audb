package audb.type;


public abstract class Type {

    public abstract byte getId();
    public abstract int getSize();
    public abstract byte[] toBytes(Object o) throws Exception;
    public abstract Object fromBytes(byte[] bytes); 
    public abstract boolean isValid(Object o);

    public final static byte INT    = 100;
    public final static byte DOUBLE = 101;

    public static Type makeType(byte id) throws Exception {
        if(id >= 100 || id <= 0) {
            throw new Exception("Wrong type.");
        } 
        
        return new VarcharType(id);
    }
}
