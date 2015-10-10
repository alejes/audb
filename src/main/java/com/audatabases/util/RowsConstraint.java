package com.audatabases.util;

enum ConstraintOperator {
	EQUAL,
	NOT_EQUAL,
	LESS,
	LESS_OR_EQUAL,
	MORE,
	MORE_OR_EQUAL
}

public class RowsConstraint {
	public TableColumn column;
	public String value;
}
