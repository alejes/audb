package audb.result;

import java.util.HashMap;
import java.util.List;

import audb.command.Constraint;
import audb.index.IndexValueInstance;
import audb.table.Table;
import audb.table.TableElement;
import audb.util.Pair;

public class IndexedConditionalTableIterator implements TableIterator {
	List<IndexValueInstance> pagesAndOffsets;
	List<Pair<String, Constraint>> nonIndexConstraints;
	Table table;
	
	public IndexedConditionalTableIterator(Table table, List<IndexValueInstance> pagesAndOffsets,
			List<Pair<String, Constraint>> nonIndexConstraints) {
		this.pagesAndOffsets = pagesAndOffsets;
		this.nonIndexConstraints = nonIndexConstraints;
		this.table = table;
	}
	
	public boolean hasNext() {
		// TODO Auto-generated method stub
		return false;
	}

	public HashMap<String, TableElement> next() {
		// TODO Auto-generated method stub
		return null;
	}

}
