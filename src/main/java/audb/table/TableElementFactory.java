package audb.table;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import audb.type.Type;

public class TableElementFactory {
	public static TableElement makeElement(byte id, byte[] data) {
		if (id == Type.INT) {
			ByteBuffer wrapped = ByteBuffer.wrap(data); // big-endian by default
			return new IntegerElement(wrapped.getInt());
		}
		
		if (id == Type.DOUBLE) {
			ByteBuffer wrapped = ByteBuffer.wrap(data); // big-endian by default
			return new DoubleElement(wrapped.getDouble());
		}
		
		return new VarcharElement(new String(data, StandardCharsets.US_ASCII), id);
	}
}
