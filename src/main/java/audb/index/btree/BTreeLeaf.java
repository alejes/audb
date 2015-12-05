package audb.index.btree;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import audb.page.PageStructure;
import audb.util.Pair;

public class BTreeLeaf<K extends Comparable<K>, V> extends BTreeNode<K, V> {
	List<V> data;
	BTreeNodeReference<K, V> next;

	public BTreeLeaf(int fanout, PageStructure ps, BTreeNodeReference<K, V> next) {
		super(fanout, ps);
		data = new ArrayList<V>(fanout);
		this.next = next;
	}

	@Override
	protected void splitNode(audb.index.btree.BTreeNode<K,V> emptyNode) {
		emptyNode.keys.addAll(keys.subList(fanout / 2, fanout));
		keys.subList(fanout / 2, keys.size()).clear();

		((BTreeLeaf<K, V>)emptyNode).data.addAll(data.subList(fanout / 2, fanout));

		data.subList(fanout / 2, fanout).clear();
		emptyNode.writeDown();
	}

	@Override
	BTreeNodeReference<K, V> insert(Pair<K, V> p) {
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

		BTreeLeaf<K, V> newLeaf = new BTreeLeaf<K, V>(fanout, pageStructure, next);
		next = new BTreeNodeReference<K, V>(newLeaf);
		splitNode(newLeaf);

		writeDown();
		return next;
	}

	private void takeFirstChild(BTreeLeaf<K, V> rightSibling) {
		V child = rightSibling.data.get(0);
		K key = rightSibling.keys.get(0);
		rightSibling.keys.subList(0, 1).clear();
		rightSibling.data.subList(0, 1).clear();
		keys.add(key);
		data.add(child);
		rightSibling.writeDown();
	}

	private void takeLastChild(BTreeLeaf<K, V> leftSibling) {
		int leftSize = leftSibling.data.size();
		V child = leftSibling.data.get(leftSize - 1);
		K key = leftSibling.keys.get(leftSize - 1);
		leftSibling.keys.subList(leftSize - 1, leftSize).clear();
		leftSibling.data.subList(leftSize - 1, leftSize).clear();
		keys.add(0, key);
		data.add(0, child);
		leftSibling.writeDown();
	}

	private void mergeWithRight(BTreeLeaf<K, V> rightSibling) {
		keys.addAll(rightSibling.keys);
		data.addAll(rightSibling.data);
		next = rightSibling.next;
	}

	@Override
	protected RemoveResult remove(K key, BTreeNodeReference<K, V> leftSibling, BTreeNodeReference<K, V> rightSibling) {
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

		if (null != rightSibling && rightSibling.getValue().keys.size() >= minChildrenNumber) {
			takeFirstChild((BTreeLeaf<K, V>)rightSibling.getValue());
			writeDown();
			return RemoveResult.REMOVED_NO_MERGE;
		} 

		if (null != leftSibling && leftSibling.getValue().keys.size() >= minChildrenNumber) {
			takeLastChild((BTreeLeaf<K, V>)leftSibling.getValue());
			writeDown();
			return RemoveResult.REMOVED_NO_MERGE;
		}

		// merge them!
		if (null != rightSibling) {
			mergeWithRight((BTreeLeaf<K, V>)rightSibling.getValue());
			return RemoveResult.REMOVED_MERGED_RIGHT;
		}

		if (null != leftSibling) {
			((BTreeLeaf<K, V>)leftSibling.getValue()).mergeWithRight(this);
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
				BTreeNodeReference<K, V> tmp = cur.next;
				if (null == tmp) {
					return result;
				}
				cur = (BTreeLeaf<K, V>)tmp.getValue();
			}
		}

		return result;
	}

	@Override
	public BTreeNodeReference<K, V> remove(K key) {
		while (RemoveResult.ELEMENT_NOT_FOUND != remove(key, null, null));

		return new BTreeNodeReference<K, V>(this);
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
				BTreeNodeReference<K, V> tmp = cur.next;
				if (null == tmp) {
					return result;
				}
				cur = (BTreeLeaf<K, V>)tmp.getValue();
			}
		}
		return result;
	}

}

