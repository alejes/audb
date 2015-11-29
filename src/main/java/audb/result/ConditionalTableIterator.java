package audb.result;

import java.util.HashMap;
import java.util.List;

import audb.command.Constraint;
import audb.table.Table;
import audb.table.TableElement;
import audb.util.Pair;

public class ConditionalTableIterator extends FullScanIterator {
	private List<Pair<String, Constraint>> constraints;
	HashMap<String, TableElement> nextRow = null;
	
	public ConditionalTableIterator(Table table, List<Pair<String, Constraint>> constraints) {
		super(table);
		this.constraints = constraints;
		rewindToNextSatisfying();
	}
	
	private void rewindToNextSatisfying() {
		nextRow = null;
		while (super.hasNext()) {
			HashMap<String, TableElement> row = super.next();
			boolean needNext = false;
			for (Pair<String, Constraint> p : constraints) {
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

	@Override
	public boolean hasNext() {
		return nextRow != null;
	}
	
	@Override
	public HashMap<String, TableElement> next() {
		HashMap<String, TableElement> retRow = nextRow;
		rewindToNextSatisfying();
		return retRow;
	}

}
