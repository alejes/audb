package audb.table;

public class IntegerElement implements TableElement {

	final Integer value;
	
	public IntegerElement(int value) {
		this.value = value;
	}
	
	public String toString() {
		return value.toString();
	}
	
	public int compareTo(TableElement other) {
		// TODO Auto-generated method stub
		return 0;
	}

}
