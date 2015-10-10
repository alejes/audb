package com.audatabases.data;

public class VarcharTypeFixedSize extends ConstSizeType {
	VarcharTypeFixedSize(int size){
		this.size = size;
	}
	
	public Integer getSize() {
		return size;
	}	
	
	@Override
	public String getName() {
		return type.toString() + "(" + Integer.toString(size) + ")";
	}
	
	private int size;
}
