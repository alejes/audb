package audb.index.btree;

import java.util.ArrayList;
import java.util.List;

import audb.page.PageStructure;
import audb.util.Pair;

public abstract class BTreeNode<K extends Comparable<K>, V> {
	enum RemoveResult {
		REMOVED_MERGED_LEFT,
		REMOVED_MERGED_RIGHT,
		REMOVED_NO_MERGE,
		ELEMENT_NOT_FOUND,
	}

	final int fanout;
	final int maxKeysNumber;
	final int minChildrenNumber;
	final int pageNumber;
	final PageStructure pageStructure;
	
	List<K> keys;

	public BTreeNode(int fanout, PageStructure ps) {
		this.fanout = fanout;
		maxKeysNumber = fanout - 1;
		keys = new ArrayList<K>(maxKeysNumber);
		minChildrenNumber = (fanout + 1) / 2;
		this.pageStructure = ps;
		this.pageNumber = (int)ps.getEmptyPage();
	}

	public BTreeNode(int fanout, PageStructure ps, BTreeNodeReference<K, V> child1, BTreeNodeReference<K, V> child2) {
		maxKeysNumber = fanout - 1;
		minChildrenNumber = (fanout + 1) / 2;
		this.fanout = fanout;
		keys = new ArrayList<K>(maxKeysNumber);
		keys.add(child1.getValue().getMaxKey());
		this.pageStructure = ps;
		this.pageNumber = (int)ps.getEmptyPage();
	}

	abstract BTreeNodeReference<K, V> insert(Pair<K, V> p);

	protected abstract RemoveResult remove(K key, BTreeNodeReference<K, V> leftSibling, 
			BTreeNodeReference<K, V> rightSibling);

	public abstract BTreeNodeReference<K, V> remove(K key);

	protected void splitNode(BTreeNode<K, V> emptyNode) {
		emptyNode.keys.addAll(keys.subList(minChildrenNumber, fanout));
		keys.subList(minChildrenNumber - 1, keys.size()).clear();
	}

	// TODO binary search is a must here
	protected int findChildIndex(K key, int maxIndex) {
		int result = maxIndex;

		for (int i = 0; i < maxIndex; i++) {
			if (keys.get(i).compareTo(key) >= 0) {
				result = i;
				break;
			}
		}

		return result;
	}
	
	
	int getMyPage() {
		return pageNumber;
	}

	void writeDown() {
		
	}
	
	protected abstract int getChildrenNumber();
	protected abstract K getMaxKey();
	public abstract List<V> find(K key);
	public abstract List<V> findAll(K bottomKey, K topKey, List<K> excludeKeys);
}