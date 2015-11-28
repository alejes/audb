package audb.index;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

abstract class BTreeNode<K extends Comparable<K>, V> {
	final int fanout;
	final int maxSize;
	final int minSize;
	
	List<K> keys;
	BTreeNode<K, V> parent;
	
	public BTreeNode(int fanout, BTreeNode<K, V> parent) {
		this.fanout = fanout;
		this.parent = parent;
		maxSize = fanout - 1;
		keys = new ArrayList<K>(maxSize);
		minSize = (fanout + 1) / 2;
	}
	
	public BTreeNode(int fanout, BTreeNode<K, V> child1, BTreeNode<K, V> child2) {
		maxSize = fanout - 1;
		minSize = (fanout + 1) / 2;
		this.fanout = fanout;
		parent = null;
		keys = new ArrayList<K>(maxSize);
		keys.add(child1.getMaxKey());
		
		child1.parent = this;
		child2.parent = this;
	}
	
	abstract BTreeNode<K, V> insert(Pair<K, V> p);

	protected abstract int remove(K key, BTreeNode<K, V> leftSibling, 
			BTreeNode<K, V> rightSibling);
	
	public abstract BTreeNode<K, V> remove(K key);
	
	protected void splitNode(BTreeNode<K, V> emptyNode) {
		emptyNode.keys.addAll(keys.subList(fanout / 2, maxSize));
		keys.subList(fanout / 2, maxSize).clear();
	}
	
	protected int findChildIndex(K key) {
		int result = keys.size();
		
		for (int i = 0; i < keys.size(); i++) {
			if (keys.get(i).compareTo(key) >= 0) {
				result = i;
				break;
			}
		}
		
		return result;
	}
	
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
		int insertIndex = findChildIndex(p.first);
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
		if (keys.size() < maxSize) {
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

	public BTreeInnerNode<K, V> remove(K key) {
		assert(parent == null);
		remove(key, null, null);
		
		if (keys.size() == 0 && !(childrenNodes.get(0) instanceof BTreeLeaf)) {
			return (BTreeInnerNode<K, V>)childrenNodes.get(0);
		}
		return this;
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
	protected int remove(K key, BTreeNode<K, V> leftSibling, BTreeNode<K, V> rightSibling) {
		int insertIndex = findChildIndex(key);
		
		BTreeNode<K, V> left = insertIndex > 0 ? childrenNodes.get(insertIndex - 1) : null;
		BTreeNode<K, V> right = insertIndex < keys.size() ? childrenNodes.get(insertIndex + 1) : null;
		BTreeNode<K, V> target = childrenNodes.get(insertIndex);
		
		int result = target.remove(key, left, right);
		
		switch (result) {
		case 0:
			if (null != left) {
				keys.set(insertIndex - 1, left.getMaxKey());
			}
			if (null != right) {
				keys.set(insertIndex, target.getMaxKey());
			}
			return 0;
		case -1: // left now contains all the keys
			keys.remove(insertIndex - 1);
			childrenNodes.remove(insertIndex - 1);
			break;
		case 1:
			keys.remove(insertIndex);
			childrenNodes.remove(insertIndex);
		}
		
		if (keys.size() >= minSize) {
			return 0;
		}
		
		if (null != rightSibling && rightSibling.keys.size() >= minSize) {
			takeFirstChild((BTreeInnerNode<K, V>)rightSibling);
			return 0;
		} 
		
		if (null != leftSibling && leftSibling.keys.size() >= minSize) {
			takeLastChild((BTreeInnerNode<K, V>)leftSibling);
			return 0;
		}
		
		// merge them!
		if (null != rightSibling) {
			mergeWithRight((BTreeInnerNode<K, V>)rightSibling);
			return 1;
		}
		
		if (null != leftSibling) {
			((BTreeInnerNode<K, V>)leftSibling).mergeWithRight(this);
			return -1;
		}
		
		return 0;
	}
	
	protected K getMaxKey() {
		return childrenNodes.get(childrenNodes.size() - 1).getMaxKey();
	}

	@Override
	public List<V> find(K key) {
		return childrenNodes.get(findChildIndex(key)).find(key);
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
		
		((BTreeLeaf<K, V>)emptyNode).data.addAll(data.subList(fanout / 2, maxSize));
		
		data.subList(fanout / 2, maxSize).clear();
	}
	
	@Override
	BTreeNode<K, V> insert(Pair<K, V> p) {
		if (data.size() < maxSize) {
			int index = findChildIndex(p.first);
			data.add(index, p.second);
			keys.add(index, p.first);
			return null;
		}
		
		BTreeLeaf<K, V> newLeaf = new BTreeLeaf<K, V>(fanout, parent, next);
		next = newLeaf;
		splitNode(newLeaf);
		
		if (newLeaf.keys.get(0).compareTo(p.first) <= 0) {
			newLeaf.insert(p);
		} else {
			insert(p);
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
	protected int remove(K key, BTreeNode<K, V> leftSibling, BTreeNode<K, V> rightSibling) {
		int idx = findChildIndex(key);
		
		if (keys.get(idx).compareTo(key) != 0) {
			return 0;
		}
		
		keys.remove(idx);
		data.remove(idx);
		if (keys.size() >= minSize) {
			return 0;
		}
		
		if (null != rightSibling && rightSibling.keys.size() >= minSize) {
			takeFirstChild((BTreeLeaf<K, V>)rightSibling);
			return 0;
		} 
		
		if (null != leftSibling && leftSibling.keys.size() >= minSize) {
			takeLastChild((BTreeLeaf<K, V>)leftSibling);
			return 0;
		}
		
		// merge them!
		if (null != rightSibling) {
			mergeWithRight((BTreeLeaf<K, V>)rightSibling);
			return 1;
		}
		
		if (null != leftSibling) {
			((BTreeLeaf<K, V>)leftSibling).mergeWithRight(this);
			return -1;
		}
		return 0;
	}

	@Override
	protected K getMaxKey() {
		return keys.get(keys.size() - 1);
	}

	@Override
	public List<V> find(K key) {
		int index = findChildIndex(key);
		if (keys.get(index).compareTo(key) != 0)
			return null;
		 
		List<V> result = new LinkedList<V>();
		
		BTreeLeaf<K, V> cur = this;
		while (true) {
			result.add(data.get(index));
			
			if (++index == data.size()) {
				index = 0;
				cur = cur.next;
				if (null == cur) {
					return result;
				}
			}
			
			if (keys.get(index).compareTo(key) != 0)
				return result;
		}
	}

	@Override
	public BTreeNode<K, V> remove(K key) {
		// TODO Auto-generated method stub
		// TODO NOT IMPLEMENTED!
		return null;
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
		throw new UnsupportedOperationException("remove is not implemented yet");
		//root = root.remove(key);
	}
	
	public List<V> find(K key) {
		return root.find(key);
	}
}
