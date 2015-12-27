package audb.result;

import audb.command.Constraint;
import audb.table.Table;
import audb.table.TableElement;
import audb.util.Third;

import java.util.HashMap;
import java.util.List;

public class ConditionalTableIterator extends FullScanIterator {
	HashMap<String, TableElement> nextRow = null;
	private List<Third<String, Constraint, String>> constraints;

	public ConditionalTableIterator(Table table, List<Third<String, Constraint, String>> constraints) {
		super(table);
		this.constraints = constraints;
		rewindToNextSatisfying();
	}
	
	private void rewindToNextSatisfying() {
		nextRow = null;
		while (super.hasNext()) {
			HashMap<String, TableElement> row = super.next();
			boolean needNext = false;
			for (Third<String, Constraint, String> p : constraints) {
				if (table.get().getTableName().compareTo(p.third) == 0) {
					if (!p.second.elementSatisfies(row.get(p.first))) {
						needNext = true;
						break;
					}
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
