package audb.table;

import audb.type.Type;

public interface TableElement extends Comparable<TableElement> {
	String toString();

	String showString();

	int compareTo(TableElement other);

	int getSizeInBytes();

	byte[] toBytes();

	Type getType();

    Object getObject();
}
