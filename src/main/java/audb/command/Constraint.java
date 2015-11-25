package audb.command;

import audb.table.TableElement;

public class Constraint {
	TableElement reference;
	ConstraintType constraintType;
	
	enum ConstraintType {
		EQUAL,
		NOT_EQUAL,
		LESS,
		GREATER,
		LESS_OR_EQUAL,
		GREATER_OR_EQUAL
	}
	
	public boolean elementSatisfies(TableElement element) {
		switch (constraintType) {
		case EQUAL:
			return (0 == element.compareTo(reference));
		case NOT_EQUAL:
			return (0 != element.compareTo(reference));
		case LESS:
			return (element.compareTo(reference) == -1);
		case GREATER:
			return (element.compareTo(reference) == 1);
		case LESS_OR_EQUAL:
			return (element.compareTo(reference) <= 0);
		case GREATER_OR_EQUAL:
			return (element.compareTo(reference) >= 0);
		}
		return false;
	}
}
