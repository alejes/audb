package com.database.util;

import com.database.data.ConstSizeType;


public class TableColumn {
	public TableColumn(ConstSizeType type, String name) {
		this.name = name;
		this.type = type;
	}
	
	public String getFullName() {
		return name + " (" + type.getName() + ")";
	}
	
	private ConstSizeType type;
	private String name;
}
