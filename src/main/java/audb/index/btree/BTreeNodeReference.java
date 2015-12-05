package audb.index.btree;

import java.lang.ref.SoftReference;

public class BTreeNodeReference<K extends Comparable<K>, V> {
	public BTreeNodeReference(int page) {
		this.page = page;
	}
	
	public BTreeNodeReference(BTreeNode<K, V> newNode) {
		value = new SoftReference<BTreeNode<K,V>>(newNode);
		page = newNode.getMyPage();
	}

	BTreeNode<K, V> getValue() {
		BTreeNode<K, V> result = value.get();
		if (null != result) {
			return result;
		} else {
			//result = readFormMemory(page);
			value = new SoftReference<BTreeNode<K,V>>(result);
		}
		return result;
	}
	
	public int getPage() {
		return page;
	}
	
	
	private SoftReference<BTreeNode<K, V>> value;
	private Integer page;
}
