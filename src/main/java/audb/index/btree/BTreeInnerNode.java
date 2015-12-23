package audb.index.btree;

import java.util.ArrayList;
import java.util.List;

import audb.index.IndexKeyInstance;
import audb.index.IndexValueInstance;
import audb.page.PageWriter;
import audb.page.Page;
import audb.util.Pair;

public class BTreeInnerNode extends BTreeNode {
	static final byte myType = 0;
	
	List<Integer> childrenNodes;
	
	public BTreeInnerNode(int fanout, NodeReader nr, int pageNumber,
			List<IndexKeyInstance> keys, List<Integer> childrenPages) {
		super(fanout, nr, pageNumber, keys);
		childrenNodes = childrenPages == null ? new ArrayList<Integer>(fanout) : childrenPages;
	}

	public BTreeInnerNode(int fanout, NodeReader nr, BTreeNode child1, BTreeNode child2) {
		super(fanout, nr, child1, child2);
		childrenNodes = new ArrayList<Integer>(fanout);

		childrenNodes.add(child1.pageNumber);
		childrenNodes.add(child2.pageNumber);
	}

	@Override
	protected void splitNode(BTreeNode emptyNode) {
		super.splitNode(emptyNode);
		BTreeInnerNode node = (BTreeInnerNode) emptyNode;

		node.childrenNodes.addAll(childrenNodes.subList(minChildrenNumber, fanout + 1));

		childrenNodes.subList(minChildrenNumber, childrenNodes.size()).clear();
		emptyNode.writeDown();
	}

	@Override
	BTreeNode insert(Pair<IndexKeyInstance, IndexValueInstance> p) {
		int insertIndex = findChildIndex(p.first, childrenNodes.size() - 1);
		BTreeNode result = null;

		BTreeNode child = nodeReader.readNode(childrenNodes.get(insertIndex));
		
		result = child.insert(p);
		
		if (null == result)
			return null;

		IndexKeyInstance newKey = child.getMaxKey();
		childrenNodes.set(insertIndex, result.pageNumber);
		if (insertIndex < keys.size()) {
			keys.set(insertIndex, result.getMaxKey());
			keys.add(insertIndex, newKey);
		} else {
			keys.add(newKey);
		}
		
		
		childrenNodes.add(insertIndex, child.pageNumber);

		if (keys.size() <= maxKeysNumber) {
			writeDown();
			return null;
		}

		BTreeInnerNode newNode = new BTreeInnerNode(fanout, nodeReader, -1, null, null);
		
		splitNode(newNode);
		writeDown();
		return newNode;
	}

	@Override
	public BTreeNode remove(IndexKeyInstance key) {
		BTreeNode cur = this;
		boolean needRemove = true;
		while (needRemove) {
			needRemove = (RemoveResult.ELEMENT_NOT_FOUND != cur.remove(key, null, null));
			BTreeNode child = nodeReader.readNode(childrenNodes.get(0));
			if (keys.size() == 0) {
				cur = child;
			}
		}

		return cur;
	}

	private void takeFirstChild(BTreeNode rightSibling) {
		BTreeInnerNode rs = (BTreeInnerNode)(rightSibling);
		int child = rs.childrenNodes.get(0);
		rs.keys.subList(0, 1).clear();
		rs.childrenNodes.subList(0, 1).clear();
		keys.add(getMaxKey());
		childrenNodes.add(child);
		rs.writeDown();
	}

	private void takeLastChild(BTreeNode leftSibling) {
		BTreeInnerNode ls = (BTreeInnerNode)(leftSibling);
		int leftSize = ls.childrenNodes.size();
		int child = ls.childrenNodes.get(leftSize - 1);
		ls.keys.subList(leftSize - 1, leftSize).clear();
		ls.childrenNodes.subList(leftSize - 1, leftSize).clear();
		keys.add(0, nodeReader.readNode(child).getMaxKey());
		childrenNodes.add(0, child);
		ls.writeDown();
	}

	private void mergeWithRight(BTreeNode rightSibling) {
		mergeWithRight((BTreeInnerNode)rightSibling);
	}
	
	private void mergeWithRight(BTreeInnerNode rightSibling) {
		keys.addAll(rightSibling.keys);
		childrenNodes.addAll(rightSibling.childrenNodes);
		nodeReader.getPageStructure().releasePage(rightSibling.pageNumber);
	}

	@Override
	protected RemoveResult remove(IndexKeyInstance key, BTreeNode leftSibling, BTreeNode rightSibling) {
		int insertIndex = findChildIndex(key, keys.size());

		if (0 == childrenNodes.size() || insertIndex >= childrenNodes.size()) {
			return RemoveResult.ELEMENT_NOT_FOUND;
		}

		BTreeNode left = insertIndex > 0 ? nodeReader.readNode(childrenNodes.get(insertIndex - 1)) : null;
		BTreeNode right = insertIndex < childrenNodes.size() - 1 ? 
				nodeReader.readNode(childrenNodes.get(insertIndex + 1)) : null;
		BTreeNode target = nodeReader.readNode(childrenNodes.get(insertIndex));

		RemoveResult result = target.remove(key, left, right);

		switch (result) {
		case REMOVED_NO_MERGE:
			if (null != left) {
				keys.set(insertIndex - 1, left.getMaxKey());
			}
			if (null != right) {
				keys.set(insertIndex, target.getMaxKey());
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

		if (null != rightSibling && rightSibling.keys.size() >= minChildrenNumber) {
			takeFirstChild(rightSibling);
			writeDown();
			return RemoveResult.REMOVED_NO_MERGE;
		} 

		if (null != leftSibling && leftSibling.keys.size() >= minChildrenNumber) {
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
			((BTreeInnerNode)leftSibling).mergeWithRight(this);
			return RemoveResult.REMOVED_MERGED_LEFT;
		}

		return RemoveResult.REMOVED_NO_MERGE;
	}
	
	@Override 
	protected PageWriter writeDown() {
		PageWriter pw = super.writeDown();
		for (int child: childrenNodes) {
			pw.writeData(Page.intToBytes(child));
		}
		return pw;
	}

	protected IndexKeyInstance getMaxKey() {
		int pageNum = childrenNodes.get(childrenNodes.size() - 1);
		assert(pageNum > 0);
		return nodeReader.readNode(pageNum).getMaxKey();
	}

	@Override
	public List<IndexValueInstance> find(IndexKeyInstance key) {
		return nodeReader.readNode(childrenNodes.get(findChildIndex(key, keys.size()))).find(key);
	}

	@Override
	protected int getChildrenNumber() {
		return childrenNodes.size();
	}

	@Override
	public List<IndexValueInstance> findAll(IndexKeyInstance bottomKey, IndexKeyInstance topKey,
			List<IndexKeyInstance> excludeKeys) {
		return nodeReader.readNode(childrenNodes.get(findChildIndex(bottomKey, keys.size()))).findAll(
				bottomKey, topKey, excludeKeys);
	}
	
	@Override
	protected byte getMyType() {
		return myType;
	}
}