package audb.table;

import audb.type.Type;

public class TableElement {
	final public Type type;
	final public Object value;
	
	public TableElement(Type type, Object value) {
		this.type = type;
		this.value = value;
	}
}
