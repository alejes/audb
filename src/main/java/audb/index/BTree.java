package audb.index;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

abstract class BTreeNode<K extends Comparable<K>, V> {
	enum RemoveResult {
		REMOVED_MERGED_LEFT,
		REMOVED_MERGED_RIGHT,
		REMOVED_NO_MERGE,
		ELEMENT_NOT_FOUND,
	}
	
	final int fanout;
	final int maxKeysNumber;
	final int minChildrenNumber;
	
	List<K> keys;
	BTreeNode<K, V> parent;
	
	public BTreeNode(int fanout, BTreeNode<K, V> parent) {
		this.fanout = fanout;
		this.parent = parent;
		maxKeysNumber = fanout - 1;
		keys = new ArrayList<K>(maxKeysNumber);
		minChildrenNumber = (fanout + 1) / 2;
	}
	
	public BTreeNode(int fanout, BTreeNode<K, V> child1, BTreeNode<K, V> child2) {
		maxKeysNumber = fanout - 1;
		minChildrenNumber = (fanout + 1) / 2;
		this.fanout = fanout;
		parent = null;
		keys = new ArrayList<K>(maxKeysNumber);
		keys.add(child1.getMaxKey());
		
		child1.parent = this;
		child2.parent = this;
	}
	
	abstract BTreeNode<K, V> insert(Pair<K, V> p);

	protected abstract RemoveResult remove(K key, BTreeNode<K, V> leftSibling, 
			BTreeNode<K, V> rightSibling);
	
	public abstract BTreeNode<K, V> remove(K key);
	
	protected void splitNode(BTreeNode<K, V> emptyNode) {
		emptyNode.keys.addAll(keys.subList(fanout / 2, maxKeysNumber));
		keys.subList(fanout / 2, maxKeysNumber).clear();
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
	
	protected abstract int getChildrenNumber();
	protected abstract K getMaxKey();
	public abstract List<V> find(K key);
}

class BTreeInnerNode<K extends Comparable<K>, V> extends BTreeNode<K, V> {
	ArrayList<BTreeNode<K, V>> childrenNodes;
	
	public BTreeInnerNode(int fanout, BTreeNode<K, V> parent) {
		super(fanout, parent);
		childrenNodes = new ArrayList<BTreeNode<K,V>>(fanout);
	}
	
	public BTreeInnerNode(int fanout, BTreeNode<K, V> child1, BTreeNode<K, V> child2) {
		super(fanout, child1, child2);
		childrenNodes = new ArrayList<BTreeNode<K,V>>(fanout);
		
		childrenNodes.add(child1);
		childrenNodes.add(child2);
	}

	@Override
	protected void splitNode(audb.index.BTreeNode<K,V> emptyNode) {
		super.splitNode(emptyNode);
		BTreeInnerNode<K, V> node = (BTreeInnerNode<K, V>) emptyNode;
		
		node.childrenNodes.addAll(childrenNodes.subList(fanout / 2, fanout));
		
		childrenNodes.subList(fanout / 2, fanout).clear();
		
	}
	
	@Override
	BTreeNode<K, V> insert(Pair<K, V> p) {
		int insertIndex = findChildIndex(p.first, keys.size());
		BTreeNode<K, V> result = null;
		
		if (childrenNodes.size() > insertIndex) {
			result = childrenNodes.get(insertIndex).insert(p);
		} else {
			BTreeLeaf<K, V> child = new BTreeLeaf<K, V>(fanout, parent, null);
			childrenNodes.add(child);
			result = childrenNodes.get(insertIndex).insert(p);
		}
		
		
		if (null == result)
			return null;
		
		BTreeNode<K, V> tmp = childrenNodes.get(insertIndex);
		childrenNodes.set(insertIndex, result);
		
		K newKey = tmp.keys.get(fanout / 2 - 1);
		if (keys.size() < maxKeysNumber) {
			keys.add(insertIndex, newKey);
			childrenNodes.add(insertIndex, tmp);
			return null;
		}
		
		BTreeInnerNode<K, V> newNode = new BTreeInnerNode<K, V>(fanout, parent);
		
		splitNode(newNode);
		if (insertIndex > fanout / 2 - 1) {
			newNode.keys.add(insertIndex - fanout / 2, newKey);
			newNode.childrenNodes.add(insertIndex - fanout / 2, tmp);
		} else {
			keys.add(insertIndex, newKey);
			childrenNodes.add(insertIndex, tmp);
		}
		
		return newNode;
	}

	@Override
	public BTreeNode<K, V> remove(K key) {
		assert(parent == null);
		
		BTreeNode<K, V> cur = this;
		boolean needRemove = true;
		while (needRemove) {
			needRemove = (RemoveResult.ELEMENT_NOT_FOUND != cur.remove(key, null, null));
			if (keys.size() == 0 && !(childrenNodes.get(0) instanceof BTreeLeaf)) {
				cur = childrenNodes.get(0);
				cur.parent = null;
			}
		}
		
		return cur;
	}

	private void takeFirstChild(BTreeInnerNode<K, V> rightSibling) {
		BTreeNode<K, V> child = rightSibling.childrenNodes.get(0);
		rightSibling.keys.subList(0, 1).clear();
		rightSibling.childrenNodes.subList(0, 1).clear();
		keys.add(getMaxKey());
		childrenNodes.add(child);
	}
	
	private void takeLastChild(BTreeInnerNode<K, V> leftSibling) {
		int leftSize = leftSibling.childrenNodes.size();
		BTreeNode<K, V> child = leftSibling.childrenNodes.get(leftSize - 1);
		leftSibling.keys.subList(leftSize - 1, leftSize).clear();
		leftSibling.childrenNodes.subList(leftSize - 1, leftSize).clear();
		keys.add(0, child.getMaxKey());
		childrenNodes.add(0, child);
	}
	
	private void mergeWithRight(BTreeInnerNode<K, V> rightSibling) {
		keys.addAll(rightSibling.keys);
		childrenNodes.addAll(rightSibling.childrenNodes);
		
	}
	
	@Override
	protected RemoveResult remove(K key, BTreeNode<K, V> leftSibling, BTreeNode<K, V> rightSibling) {
		int insertIndex = findChildIndex(key, keys.size());
		
		if (0 == childrenNodes.size() || insertIndex >= childrenNodes.size()) {
			return RemoveResult.ELEMENT_NOT_FOUND;
		}
		
		BTreeNode<K, V> left = insertIndex > 0 ? childrenNodes.get(insertIndex - 1) : null;
		BTreeNode<K, V> right = (insertIndex < childrenNodes.size() - 1) ? childrenNodes.get(insertIndex + 1) : null;
		BTreeNode<K, V> target = childrenNodes.get(insertIndex);
		
		RemoveResult result = target.remove(key, left, right);
		
		switch (result) {
		case REMOVED_NO_MERGE:
			if (null != left) {
				keys.set(insertIndex - 1, left.getMaxKey());
			}
			if (null != right) {
				keys.set(insertIndex, target.getMaxKey());
			}
			return RemoveResult.REMOVED_NO_MERGE;
		case ELEMENT_NOT_FOUND:
			return RemoveResult.ELEMENT_NOT_FOUND;
		case REMOVED_MERGED_LEFT: // left now contains all the keys
			keys.remove(insertIndex - 1);
			childrenNodes.remove(insertIndex - 1);
			break;
		case REMOVED_MERGED_RIGHT:
			keys.remove(insertIndex);
			childrenNodes.remove(insertIndex);
		}
		
		if (keys.size() >= minChildrenNumber) {
			return RemoveResult.REMOVED_NO_MERGE;
		}
		
		if (null != rightSibling && rightSibling.keys.size() >= minChildrenNumber) {
			takeFirstChild((BTreeInnerNode<K, V>)rightSibling);
			return RemoveResult.REMOVED_NO_MERGE;
		} 
		
		if (null != leftSibling && leftSibling.keys.size() >= minChildrenNumber) {
			takeLastChild((BTreeInnerNode<K, V>)leftSibling);
			return RemoveResult.REMOVED_NO_MERGE;
		}
		
		// merge them!
		if (null != rightSibling) {
			mergeWithRight((BTreeInnerNode<K, V>)rightSibling);
			return RemoveResult.REMOVED_MERGED_RIGHT;
		}
		
		if (null != leftSibling) {
			((BTreeInnerNode<K, V>)leftSibling).mergeWithRight(this);
			return RemoveResult.REMOVED_MERGED_LEFT;
		}
		
		return RemoveResult.REMOVED_NO_MERGE;
	}
	
	protected K getMaxKey() {
		return childrenNodes.get(childrenNodes.size() - 1).getMaxKey();
	}

	@Override
	public List<V> find(K key) {
		return childrenNodes.get(findChildIndex(key, keys.size())).find(key);
	}

	@Override
	protected int getChildrenNumber() {
		return childrenNodes.size();
	}
}

class BTreeLeaf<K extends Comparable<K>, V> extends BTreeNode<K, V> {
	List<V> data;
	BTreeLeaf<K, V> next;
	
	public BTreeLeaf(int fanout, BTreeNode<K, V> parent, BTreeLeaf<K, V> next) {
		super(fanout, parent);
		data = new ArrayList<V>(fanout);
		this.next = next;
	}

	@Override
	protected void splitNode(audb.index.BTreeNode<K,V> emptyNode) {
		super.splitNode(emptyNode);
		
		((BTreeLeaf<K, V>)emptyNode).data.addAll(data.subList(fanout / 2, maxKeysNumber));
		
		data.subList(fanout / 2, maxKeysNumber).clear();
	}
	
	@Override
	BTreeNode<K, V> insert(Pair<K, V> p) {
		if (data.size() < maxKeysNumber) {
			int index = findChildIndex(p.first, data.size());
			data.add(index, p.second);
			keys.add(index, p.first);
			return null;
		}
		
		BTreeLeaf<K, V> newLeaf = new BTreeLeaf<K, V>(fanout, parent, next);
		next = newLeaf;
		splitNode(newLeaf);
		
		if (keys.get(keys.size() - 1).compareTo(p.first) >= 0) {
			insert(p);
		} else {
			newLeaf.insert(p);
		}
		
		return newLeaf;
	}
	
	private void takeFirstChild(BTreeLeaf<K, V> rightSibling) {
		V child = rightSibling.data.get(0);
		K key = rightSibling.keys.get(0);
		rightSibling.keys.subList(0, 1).clear();
		rightSibling.data.subList(0, 1).clear();
		keys.add(key);
		data.add(child);
	}
	
	private void takeLastChild(BTreeLeaf<K, V> leftSibling) {
		int leftSize = leftSibling.data.size();
		V child = leftSibling.data.get(leftSize - 1);
		K key = leftSibling.keys.get(leftSize - 1);
		leftSibling.keys.subList(leftSize - 1, leftSize).clear();
		leftSibling.data.subList(leftSize - 1, leftSize).clear();
		keys.add(0, key);
		data.add(0, child);
	}
	
	private void mergeWithRight(BTreeLeaf<K, V> rightSibling) {
		keys.addAll(rightSibling.keys);
		data.addAll(rightSibling.data);
		next = rightSibling.next;
	}
	
	@Override
	protected RemoveResult remove(K key, BTreeNode<K, V> leftSibling, BTreeNode<K, V> rightSibling) {
		if (data.size() == 0) 
			return RemoveResult.ELEMENT_NOT_FOUND;
			
		int idx = findChildIndex(key, data.size() - 1);
		
		if (keys.get(idx).compareTo(key) != 0) {
			return RemoveResult.ELEMENT_NOT_FOUND;
		}
		
		keys.remove(idx);
		data.remove(idx);
		if (keys.size() >= minChildrenNumber) {
			return RemoveResult.REMOVED_NO_MERGE;
		}
		
		if (null != rightSibling && rightSibling.keys.size() >= minChildrenNumber) {
			takeFirstChild((BTreeLeaf<K, V>)rightSibling);
			return RemoveResult.REMOVED_NO_MERGE;
		} 
		
		if (null != leftSibling && leftSibling.keys.size() >= minChildrenNumber) {
			takeLastChild((BTreeLeaf<K, V>)leftSibling);
			return RemoveResult.REMOVED_NO_MERGE;
		}
		
		// merge them!
		if (null != rightSibling) {
			mergeWithRight((BTreeLeaf<K, V>)rightSibling);
			return RemoveResult.REMOVED_MERGED_RIGHT;
		}
		
		if (null != leftSibling) {
			((BTreeLeaf<K, V>)leftSibling).mergeWithRight(this);
			return RemoveResult.REMOVED_MERGED_LEFT;
		}
		
		return RemoveResult.REMOVED_NO_MERGE;
	}

	@Override
	protected K getMaxKey() {
		return keys.get(keys.size() - 1);
	}

	@Override
	public List<V> find(K key) {
		int index = findChildIndex(key, data.size() - 1);
		List<V> result = new LinkedList<V>();
		
		if (index == -1) {
			return result;
		}
		
		BTreeLeaf<K, V> cur = this;
		while (cur.keys.get(index).compareTo(key) == 0) {
			result.add(data.get(index));
			
			if (++index == data.size()) {
				index = 0;
				cur = cur.next;
				if (null == cur) {
					return result;
				}
			}
		}
		
		return result;
	}

	@Override
	public BTreeNode<K, V> remove(K key) {
		while (RemoveResult.ELEMENT_NOT_FOUND != remove(key, null, null));
		
		return this;
	}

	@Override
	protected int getChildrenNumber() {
		return data.size();
	}
	
}

public class BTree<K extends Comparable<K>, V> {
	BTreeNode<K, V> root;
	
	int fanout;
	
	public BTree(int fanout) {
		root = new BTreeLeaf<K, V>(fanout, null, null);
		this.fanout = fanout;
	}
	
	public void insert(K k, V v) {
		insert(Pair.newPair(k, v));
	}
	
	public void insert(Pair<K, V> p) {
		BTreeNode<K, V> tmp = root.insert(p);
		if (null == tmp) {
			return;
		}
		
		root = new BTreeInnerNode<K, V>(fanout, root, tmp);
	}
	
	public void remove(K key) {
		//throw new UnsupportedOperationException("remove is not implemented yet");
		root = root.remove(key);
	}
	
	public List<V> find(K key) {
		return root.find(key);
	}
}
