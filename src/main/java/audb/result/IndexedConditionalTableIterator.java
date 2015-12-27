package audb.result;

import audb.command.Constraint;
import audb.index.IndexValueInstance;
import audb.table.Table;
import audb.table.TableElement;
import audb.type.Type;
import audb.util.Pair;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class IndexedConditionalTableIterator implements TableIterator {
	List<IndexValueInstance> pagesAndOffsets;
	List<Pair<String, Constraint>> nonIndexConstraints;
	Table table;
	Iterator<IndexValueInstance> pageOfsetIter;
	HashMap<String, TableElement> nextRow = null;

	public IndexedConditionalTableIterator(Table table, List<IndexValueInstance> pagesAndOffsets,
										   List<Pair<String, Constraint>> nonIndexConstraints) {
		this.pagesAndOffsets = pagesAndOffsets;
		this.nonIndexConstraints = nonIndexConstraints;
		this.table = table;
		pageOfsetIter = pagesAndOffsets.iterator();

		rewindToNextSatisfying();
	}
	
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
	
	public boolean hasNext() {
		return nextRow != null;
	}

	public HashMap<String, TableElement> next() {
		HashMap<String, TableElement> retRow = nextRow;
		rewindToNextSatisfying();
		return retRow;
	}

	public String[] getNames() {
		return table.getNames();
	}

	public Type[] getTypes() {
		return table.getTypes();
	}

}
