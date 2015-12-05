package audb.index;

import audb.index.Index.Order;
import audb.table.TableElement;

public class IndexKeyInstance implements Comparable<IndexKeyInstance> {
	public final Order[] orders;
	public final TableElement[] elements;
	
	public IndexKeyInstance(Order[] orders, TableElement[] elements) {
		this.orders = orders;
		this.elements = elements;
	}
	
	public int compareTo(IndexKeyInstance other) {
		for (int i = 0; i < elements.length; i++) {		
			if (null == elements[i]) {
				continue;
			}
			int result = elements[i].compareTo(other.elements[i]);
			if (result == 0)
				continue;
			
			return ((orders[i] == Order.ASC) ?  result : -result);
		}
		return 0;
	}
}
