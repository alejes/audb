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
        Type type = null;
        if(id == INT) {
            throw new Exception("Wrong type.");
        } else if(id == DOUBLE) {
            throw new Exception("Wrong type.");
        } else if(id < 100 && id > 0) {
            type = new VarcharType(id);
        } else {
            throw new Exception("Wrong type.");
        }
        return type;
    }
}
