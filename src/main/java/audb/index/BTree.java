package audb.index;

import java.util.ArrayList;
import java.util.Iterator;
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

	public BTreeNode(int fanout) {
		this.fanout = fanout;
		maxKeysNumber = fanout - 1;
		keys = new ArrayList<K>(maxKeysNumber);
		minChildrenNumber = (fanout + 1) / 2;
	}

	public BTreeNode(int fanout, BTreeNode<K, V> child1, BTreeNode<K, V> child2) {
		maxKeysNumber = fanout - 1;
		minChildrenNumber = (fanout + 1) / 2;
		this.fanout = fanout;
		keys = new ArrayList<K>(maxKeysNumber);
		keys.add(child1.getMaxKey());
	}

	abstract BTreeNode<K, V> insert(Pair<K, V> p);

	protected abstract RemoveResult remove(K key, BTreeNode<K, V> leftSibling, 
			BTreeNode<K, V> rightSibling);

	public abstract BTreeNode<K, V> remove(K key);

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

	protected abstract int getChildrenNumber();
	protected abstract K getMaxKey();
	public abstract List<V> find(K key);
	public abstract List<V> findAll(K bottomKey, K topKey, List<K> excludeKeys);
}

class BTreeInnerNode<K extends Comparable<K>, V> extends BTreeNode<K, V> {
	ArrayList<BTreeNode<K, V>> childrenNodes;

	public BTreeInnerNode(int fanout) {
		super(fanout);
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

		try {
			node.childrenNodes.addAll(childrenNodes.subList(minChildrenNumber, fanout + 1));
		} catch(Exception e) {
			e.printStackTrace(System.out);
			System.out.println("HERE!");
			System.out.println(childrenNodes.size());
		}

		childrenNodes.subList(minChildrenNumber, childrenNodes.size()).clear();

	}

	@Override
	BTreeNode<K, V> insert(Pair<K, V> p) {
		int insertIndex = findChildIndex(p.first, childrenNodes.size() - 1);
		BTreeNode<K, V> result = null;

		result = childrenNodes.get(insertIndex).insert(p);

		if (null == result)
			return null;

		BTreeNode<K, V> tmp = childrenNodes.get(insertIndex);
		K newKey = tmp.getMaxKey();
		childrenNodes.set(insertIndex, result);
		if (insertIndex < keys.size()) {
			keys.set(insertIndex, result.getMaxKey());
			keys.add(insertIndex, newKey);
		} else {
			keys.add(newKey);
		}
		childrenNodes.add(insertIndex, tmp);

		if (keys.size() <= maxKeysNumber) {
			return null;
		}

		BTreeInnerNode<K, V> newNode = new BTreeInnerNode<K, V>(fanout);

		splitNode(newNode);
		return newNode;
	}

	@Override
	public BTreeNode<K, V> remove(K key) {
		BTreeNode<K, V> cur = this;
		boolean needRemove = true;
		while (needRemove) {
			needRemove = (RemoveResult.ELEMENT_NOT_FOUND != cur.remove(key, null, null));
			if (keys.size() == 0 && !(childrenNodes.get(0) instanceof BTreeLeaf)) {
				cur = childrenNodes.get(0);
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

	@Override
	public List<V> findAll(K bottomKey, K topKey, List<K> excludeKeys) {
		return childrenNodes.get(findChildIndex(bottomKey, keys.size())).findAll(
				bottomKey, topKey, excludeKeys);
	}
}

class BTreeLeaf<K extends Comparable<K>, V> extends BTreeNode<K, V> {
	List<V> data;
	BTreeLeaf<K, V> next;

	public BTreeLeaf(int fanout, BTreeLeaf<K, V> next) {
		super(fanout);
		data = new ArrayList<V>(fanout);
		this.next = next;
	}

	@Override
	protected void splitNode(audb.index.BTreeNode<K,V> emptyNode) {
		emptyNode.keys.addAll(keys.subList(fanout / 2, fanout));
		keys.subList(fanout / 2, keys.size()).clear();

		((BTreeLeaf<K, V>)emptyNode).data.addAll(data.subList(fanout / 2, fanout));

		data.subList(fanout / 2, fanout).clear();
	}

	@Override
	BTreeNode<K, V> insert(Pair<K, V> p) {
		int index = findChildIndex(p.first, data.size());
		if (index < data.size()) {
			data.add(index, p.second);
			keys.add(index, p.first);
		} else {
			data.add(p.second);
			keys.add(p.first);
		}

		if (data.size() <= maxKeysNumber) {
			return null;
		}

		BTreeLeaf<K, V> newLeaf = new BTreeLeaf<K, V>(fanout, next);
		next = newLeaf;
		splitNode(newLeaf);

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

	@Override
	public List<V> findAll(K bottomKey, K topKey, List<K> excludeKeys) {
		int index = findChildIndex(bottomKey, data.size() - 1);
		List<V> result = new LinkedList<V>();

		if (index == -1) {
			return result;
		}

		BTreeLeaf<K, V> cur = this;
		Iterator<K> exIter = excludeKeys.iterator();
		K exKey = exIter.hasNext() ? exIter.next() : null;
		while (cur.keys.get(index).compareTo(topKey) <= 0) {

			boolean skipValue = false;
			K currentKey = cur.keys.get(index);
			boolean needCompare = true;

			while (needCompare && null != exKey) {
				needCompare = false;
				switch (exKey.compareTo(currentKey)) {
				case -1:
					if (exIter.hasNext()) {
						exKey = exIter.next();
						needCompare = true;
					}
					else {
						exKey = null;
					}
					break;
				case 1:
					break;
				case 0:
					skipValue = true;
				}
			}
			
			if (!skipValue) {
				result.add(data.get(index));
			}
			
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

}

public class BTree<K extends Comparable<K>, V> {
	BTreeNode<K, V> root;

	int fanout;

	public BTree(int fanout) {
		root = new BTreeLeaf<K, V>(fanout, null);
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
		root = root.remove(key);
	}

	public List<V> find(K key) {
		return root.find(key);
	}

	public List<V> findAll(K bottomKey, K topKey, List<K> excludeKeys) {
		return root.findAll(bottomKey, topKey, excludeKeys);
	}
}
