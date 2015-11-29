package audb.result;

import java.util.HashMap;
import java.util.List;

import audb.command.Constraint;
import audb.table.Table;
import audb.table.TableElement;
import audb.util.Pair;

public class ConditionalTableIterator extends FullScanIterator {
	private List<Pair<String, Constraint>> constraints;
	
	public ConditionalTableIterator(Table table, List<Pair<String, Constraint>> constraints) {
		super(table);
		this.constraints = constraints;
		rewindToNextSatisfying();
	}
	
	private void rewindToNextSatisfying() {
		while (super.hasNext()) {
			HashMap<String, TableElement> row = super.next();
			boolean needNext = false;
			for (Pair<String, Constraint> p : constraints) {
				if (!p.second.elementSatisfies(row.get(p.first))) {
					needNext = true;
					break;
				}
			}
			if (!needNext)
				break;
		}
	}

	@Override
	public HashMap<String, TableElement> next() {
		HashMap<String, TableElement> row = super.next();
		rewindToNextSatisfying();
		return row;
	}

}
