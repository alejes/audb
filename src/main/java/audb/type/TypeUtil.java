package audb.type;


public class TypeUtil {


	public final static byte INT = 100;
	public final static byte DOUBLE = 101;
	
	public static boolean isInt(Type t) {
		return t.getId() == INT;
	}

	public static boolean isDouble(Type t) {
		return t.getId() == DOUBLE;
	}

	public static boolean isVarchar(Type t) {
		return t.getId() < 100;
	}

	public static Type makeType(byte id) throws Exception {
		Type type = null;
		if(id == INT) {
			throw new Exception("Wrong type.");
		} else if(id == DOUBLE) {
			throw new Exception("Wrong type.");
		} else if(id < 100) {
			type = new VarcharType(id);
		} else {
			throw new Exception("Wrong type.");
		}
		return type;
	}

}
