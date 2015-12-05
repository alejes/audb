package audb.index.btree;

import java.util.ArrayList;
import java.util.List;

import audb.index.btree.BTreeNode.RemoveResult;
import audb.page.PageStructure;
import audb.util.Pair;

public class BTreeInnerNode<K extends Comparable<K>, V> extends BTreeNode<K, V> {
	ArrayList<BTreeNodeReference<K, V>> childrenNodes;

	public BTreeInnerNode(int fanout, PageStructure ps) {
		super(fanout, ps);
		childrenNodes = new ArrayList<BTreeNodeReference<K, V>>(fanout);
	}

	public BTreeInnerNode(int fanout, PageStructure ps, BTreeNodeReference<K, V> child1, BTreeNodeReference<K, V> child2) {
		super(fanout, ps, child1, child2);
		childrenNodes = new ArrayList<BTreeNodeReference<K,V>>(fanout);

		childrenNodes.add(child1);
		childrenNodes.add(child2);
	}

	@Override
	protected void splitNode(audb.index.btree.BTreeNode<K,V> emptyNode) {
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
		emptyNode.writeDown();
	}

	@Override
	BTreeNodeReference<K, V> insert(Pair<K, V> p) {
		int insertIndex = findChildIndex(p.first, childrenNodes.size() - 1);
		BTreeNodeReference<K, V> result = null;

		result = childrenNodes.get(insertIndex).getValue().insert(p);

		if (null == result)
			return null;

		BTreeNodeReference<K, V> tmp = childrenNodes.get(insertIndex);
		K newKey = tmp.getValue().getMaxKey();
		childrenNodes.set(insertIndex, result);
		if (insertIndex < keys.size()) {
			keys.set(insertIndex, result.getValue().getMaxKey());
			keys.add(insertIndex, newKey);
		} else {
			keys.add(newKey);
		}
		childrenNodes.add(insertIndex, tmp);

		if (keys.size() <= maxKeysNumber) {
			return null;
		}

		BTreeInnerNode<K, V> newNode = new BTreeInnerNode<K, V>(fanout, pageStructure);
		
		splitNode(newNode);
		writeDown();
		return new BTreeNodeReference<K, V>(newNode);
	}

	@Override
	public BTreeNodeReference<K, V> remove(K key) {
		BTreeNode<K, V> cur = this;
		boolean needRemove = true;
		while (needRemove) {
			needRemove = (RemoveResult.ELEMENT_NOT_FOUND != cur.remove(key, null, null));
			if (keys.size() == 0 && !(childrenNodes.get(0).getValue() instanceof BTreeLeaf)) {
				cur = childrenNodes.get(0).getValue();
			}
		}

		return new BTreeNodeReference<K, V>(cur);
	}

	private void takeFirstChild(BTreeNodeReference<K, V> rightSibling) {
		BTreeInnerNode<K, V> rs = (BTreeInnerNode<K, V>)(rightSibling.getValue());
		BTreeNodeReference<K, V> child = rs.childrenNodes.get(0);
		rs.keys.subList(0, 1).clear();
		rs.childrenNodes.subList(0, 1).clear();
		keys.add(getMaxKey());
		childrenNodes.add(child);
		rs.writeDown();
	}

	private void takeLastChild(BTreeNodeReference<K, V> leftSibling) {
		BTreeInnerNode<K, V> ls = (BTreeInnerNode<K, V>)(leftSibling.getValue());
		int leftSize = ls.childrenNodes.size();
		BTreeNodeReference<K, V> child = ls.childrenNodes.get(leftSize - 1);
		ls.keys.subList(leftSize - 1, leftSize).clear();
		ls.childrenNodes.subList(leftSize - 1, leftSize).clear();
		keys.add(0, child.getValue().getMaxKey());
		childrenNodes.add(0, child);
		ls.writeDown();
	}

	private void mergeWithRight(BTreeNodeReference<K, V> rightSibling) {
		mergeWithRight((BTreeInnerNode<K, V>)rightSibling.getValue());
	}
	
	private void mergeWithRight(BTreeInnerNode<K, V> rightSibling) {
		keys.addAll(rightSibling.keys);
		childrenNodes.addAll(rightSibling.childrenNodes);
		pageStructure.releasePage(rightSibling.pageNumber);
	}

	@Override
	protected RemoveResult remove(K key, BTreeNodeReference<K, V> leftSibling, BTreeNodeReference<K, V> rightSibling) {
		int insertIndex = findChildIndex(key, keys.size());

		if (0 == childrenNodes.size() || insertIndex >= childrenNodes.size()) {
			return RemoveResult.ELEMENT_NOT_FOUND;
		}

		BTreeNodeReference<K, V> left = insertIndex > 0 ? childrenNodes.get(insertIndex - 1) : null;
		BTreeNodeReference<K, V> right = (insertIndex < childrenNodes.size() - 1) ? childrenNodes.get(insertIndex + 1) : null;
		BTreeNodeReference<K, V> target = childrenNodes.get(insertIndex);

		RemoveResult result = target.getValue().remove(key, left, right);

		switch (result) {
		case REMOVED_NO_MERGE:
			if (null != left) {
				keys.set(insertIndex - 1, left.getValue().getMaxKey());
			}
			if (null != right) {
				keys.set(insertIndex, target.getValue().getMaxKey());
			}
			writeDown();
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
			writeDown();
			return RemoveResult.REMOVED_NO_MERGE;
		}

		if (null != rightSibling && rightSibling.getValue().keys.size() >= minChildrenNumber) {
			takeFirstChild(rightSibling);
			writeDown();
			return RemoveResult.REMOVED_NO_MERGE;
		} 

		if (null != leftSibling && leftSibling.getValue().keys.size() >= minChildrenNumber) {
			takeLastChild(leftSibling);
			writeDown();
			return RemoveResult.REMOVED_NO_MERGE;
		}

		// merge them!
		if (null != rightSibling) {
			mergeWithRight(rightSibling);
			writeDown();
			return RemoveResult.REMOVED_MERGED_RIGHT;
		}

		if (null != leftSibling) {
			((BTreeInnerNode<K, V>)leftSibling.getValue()).mergeWithRight(this);
			return RemoveResult.REMOVED_MERGED_LEFT;
		}

		return RemoveResult.REMOVED_NO_MERGE;
	}

	protected K getMaxKey() {
		return childrenNodes.get(childrenNodes.size() - 1).getValue().getMaxKey();
	}

	@Override
	public List<V> find(K key) {
		return childrenNodes.get(findChildIndex(key, keys.size())).getValue().find(key);
	}

	@Override
	protected int getChildrenNumber() {
		return childrenNodes.size();
	}

	@Override
	public List<V> findAll(K bottomKey, K topKey, List<K> excludeKeys) {
		return childrenNodes.get(findChildIndex(bottomKey, keys.size())).getValue().findAll(
				bottomKey, topKey, excludeKeys);
	}
}