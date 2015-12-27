package audb.result;

import audb.table.TableElement;
import audb.type.Type;

import java.util.HashMap;
import java.util.Iterator;

public interface TableIterator extends Iterator<HashMap<String, TableElement>> {
	String[] getNames();

	Type[] getTypes();
}
