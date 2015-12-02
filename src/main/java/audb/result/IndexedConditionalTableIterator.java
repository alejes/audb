package audb.result;

import java.util.HashMap;
import java.util.Iterator;
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
	Iterator<IndexValueInstance> pageOfsetIter;
	HashMap<String, TableElement> nextRow = null;
	
	private void rewindToNextSatisfying() {
		nextRow = null;
		while (pageOfsetIter.hasNext()) {
			IndexValueInstance dataLocation = pageOfsetIter.next();
			
			HashMap<String, TableElement> row = table.read(
					dataLocation.page, dataLocation.offset);
			
			boolean needNext = false;
			for (Pair<String, Constraint> p : nonIndexConstraints) {
				if (!p.second.elementSatisfies(row.get(p.first))) {
					needNext = true;
					break;
				}
			}
			if (!needNext) {
				nextRow = row;
				break;
			}
		}
	}
	
	
	public IndexedConditionalTableIterator(Table table, List<IndexValueInstance> pagesAndOffsets,
			List<Pair<String, Constraint>> nonIndexConstraints) {
		this.pagesAndOffsets = pagesAndOffsets;
		this.nonIndexConstraints = nonIndexConstraints;
		this.table = table;
		pageOfsetIter = pagesAndOffsets.iterator();
		
		rewindToNextSatisfying();
	}
	
	public boolean hasNext() {
		return nextRow != null;
	}

	public HashMap<String, TableElement> next() {
		HashMap<String, TableElement> retRow = nextRow;
		rewindToNextSatisfying();
		return retRow;
	}

}
