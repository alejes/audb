package audb.type;

import audb.table.TableElement;

public abstract class Type {

    public abstract int getSize();
    public abstract byte[] toBytes(Object o) throws Exception;
    public abstract boolean isValid(Object o);

    public final static byte INT    = 100;
    public final static byte DOUBLE = 101;
    
    private final byte id;
    
    Type(byte id) {
    	this.id = id;
    }
    
    public byte getId() {
    	return id;
    }
    
    public static Type makeType(byte id) throws Exception {
        if(id >= 100 || id <= 0) {
            throw new Exception("Wrong type.");
        } 
        
        return new VarcharType(id);
    }
    
    public abstract TableElement fromBytes(byte[] data);
}
