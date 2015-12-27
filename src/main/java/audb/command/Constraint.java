package audb.command;

import audb.table.TableElement;

public class Constraint {
	public final TableElement reference;
	public final ConstraintType constraintType;
	
	public Constraint(ConstraintType c, TableElement ref) {
		constraintType = c;
		reference = ref;
		assert (reference != null);
	}

	public boolean elementSatisfies(TableElement element) {
		switch (constraintType) {
			case EQUAL:
				return (0 == element.compareTo(reference));
			case NOT_EQUAL:
				return (0 != element.compareTo(reference));
			case LESS:
				return (element.compareTo(reference) < 0);
			case GREATER:
				return (element.compareTo(reference) > 0);
			case LESS_OR_EQUAL:
				return (element.compareTo(reference) <= 0);
			case GREATER_OR_EQUAL:
				return (element.compareTo(reference) >= 0);
		}
		return false;
	}

	@Override
	public String toString() {
		return "Constraint " + reference.toString() + " " + constraintType.toString();
	}

	public enum ConstraintType {
		EQUAL,
		NOT_EQUAL,
		LESS,
		GREATER,
		LESS_OR_EQUAL,
		GREATER_OR_EQUAL
	}
}
