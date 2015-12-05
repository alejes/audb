package audb.table;

import audb.type.Type;

public interface TableElement extends Comparable<TableElement> {
	public String toString();
	public int compareTo(TableElement other);
	public int getSizeInBytes();
	public byte[] toBytes();
	Type getType();
}
