package audb.result;

import java.util.HashMap;
import java.util.Iterator;

import audb.table.TableElement;

public interface TableIterator extends Iterator<HashMap<String, TableElement>> { 
	
}
