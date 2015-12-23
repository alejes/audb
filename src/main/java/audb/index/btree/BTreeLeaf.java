package audb.index.btree;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import audb.index.IndexKeyInstance;
import audb.index.IndexValueInstance;
import audb.page.PageManager;
import audb.page.PageStructure;
import audb.page.PageWriter;
import audb.util.Pair;

public class BTreeLeaf extends BTreeNode {
	List<IndexValueInstance> data;
	int nextNodePage;
	static final byte myType = 1;
	
	public BTreeLeaf(int fanout, NodeReader nr, int pageNumber, int next, 
			List<IndexKeyInstance> keys, List<IndexValueInstance> values) {
		super(fanout, nr, pageNumber, keys);
		this.nextNodePage = next;
		this.data = values == null ? new ArrayList<IndexValueInstance>(maxKeysNumber) : values;
	}
	
	@Override
	protected void splitNode(BTreeNode emptyNode) {
		emptyNode.keys.addAll(keys.subList(fanout / 2, fanout));
		keys.subList(fanout / 2, keys.size()).clear();

		((BTreeLeaf)emptyNode).data.addAll(data.subList(fanout / 2, fanout));

		data.subList(fanout / 2, fanout).clear();
		emptyNode.writeDown();
	}

	@Override
	BTreeNode insert(Pair<IndexKeyInstance, IndexValueInstance> p) {
		int index = findChildIndex(p.first, data.size());
		if (index < data.size()) {
			data.add(index, p.second);
			keys.add(index, p.first);
		} else {
			data.add(p.second);
			keys.add(p.first);
		}

		if (data.size() <= maxKeysNumber) {
			writeDown();
			return null;
		}

		BTreeLeaf newLeaf = new BTreeLeaf(fanout, nodeReader, -1, nextNodePage, null, null);
		nextNodePage = newLeaf.pageNumber;
		splitNode(newLeaf);

		writeDown();
		return newLeaf;
	}

	private void takeFirstChild(BTreeLeaf rightSibling) {
		IndexValueInstance child = rightSibling.data.get(0);
		IndexKeyInstance key = rightSibling.keys.get(0);
		rightSibling.keys.subList(0, 1).clear();
		rightSibling.data.subList(0, 1).clear();
		keys.add(key);
		data.add(child);
		rightSibling.writeDown();
	}

	private void takeLastChild(BTreeLeaf leftSibling) {
		int leftSize = leftSibling.data.size();
		IndexValueInstance child = leftSibling.data.get(leftSize - 1);
		IndexKeyInstance key = leftSibling.keys.get(leftSize - 1);
		leftSibling.keys.subList(leftSize - 1, leftSize).clear();
		leftSibling.data.subList(leftSize - 1, leftSize).clear();
		keys.add(0, key);
		data.add(0, child);
		leftSibling.writeDown();
	}

	private void mergeWithRight(BTreeLeaf rightSibling) {
		keys.addAll(rightSibling.keys);
		data.addAll(rightSibling.data);
		nextNodePage = rightSibling.nextNodePage;
	}

	@Override
	protected RemoveResult remove(IndexKeyInstance key, BTreeNode leftSibling, BTreeNode rightSibling) {
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
			takeFirstChild((BTreeLeaf)rightSibling);
			writeDown();
			return RemoveResult.REMOVED_NO_MERGE;
		} 

		if (null != leftSibling && leftSibling.keys.size() >= minChildrenNumber) {
			takeLastChild((BTreeLeaf)leftSibling);
			writeDown();
			return RemoveResult.REMOVED_NO_MERGE;
		}

		// merge them!
		if (null != rightSibling) {
			mergeWithRight((BTreeLeaf)rightSibling);
			writeDown();
			return RemoveResult.REMOVED_MERGED_RIGHT;
		}

		if (null != leftSibling) {
			((BTreeLeaf)leftSibling).mergeWithRight(this);
			leftSibling.writeDown();
			return RemoveResult.REMOVED_MERGED_LEFT;
		}

		return RemoveResult.REMOVED_NO_MERGE;
	}

	@Override
	protected IndexKeyInstance getMaxKey() {
		return keys.get(keys.size() - 1);
	}

	@Override
	public List<IndexValueInstance> find(IndexKeyInstance key) {
		int index = findChildIndex(key, data.size() - 1);
		List<IndexValueInstance> result = new LinkedList<IndexValueInstance>();

		if (index == -1) {
			return result;
		}

		BTreeLeaf cur = this;
		while (cur.keys.get(index).compareTo(key) == 0) {
			result.add(data.get(index));

			if (++index == data.size()) {
				index = 0;
				BTreeNode tmp = nodeReader.readNode(cur.nextNodePage);
				if (null == tmp) {
					return result;
				}
				cur = (BTreeLeaf)tmp;
			}
		}

		return result;
	}

	@Override
	public BTreeNode remove(IndexKeyInstance key) {
		while (RemoveResult.ELEMENT_NOT_FOUND != remove(key, null, null));

		return this;
	}

	@Override
	protected int getChildrenNumber() {
		return data.size();
	}

	// TODO make sure excluded keys are sorted up to here
	@Override
	public List<IndexValueInstance> findAll(IndexKeyInstance bottomKey, 
			IndexKeyInstance topKey, List<IndexKeyInstance> excludeKeys) {	
		int index = findChildIndex(bottomKey, data.size() - 1);
		List<IndexValueInstance> result = new LinkedList<IndexValueInstance>();

		if (data.size() == 0) {
			return result;
		}

		BTreeLeaf cur = this;
		Iterator<IndexKeyInstance> exIter = excludeKeys.iterator();
		IndexKeyInstance exKey = exIter.hasNext() ? exIter.next() : null;
		
		while (true) {
			if (null != topKey && cur.keys.get(index).compareTo(topKey) > 0)
				break;
			
			boolean skipValue = false;
			IndexKeyInstance currentKey = cur.keys.get(index);
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
				result.add(cur.data.get(index));
			}
			
			if (++index == cur.data.size()) {
				index = 0;
				BTreeNode tmp = nodeReader.readNode(cur.nextNodePage);
				if (null == tmp) {
					return result;
				}
				cur = (BTreeLeaf)tmp;
			}
		}
		return result;
	}
	
	@Override 
	protected PageWriter writeDown() {
		PageWriter pw = super.writeDown();
		for (IndexValueInstance value: data) {
			pw.writeInteger(value.page);
			pw.writeInteger(value.offset);
		}
		pw.writeInteger(nextNodePage);
		return pw;
	}
	
	@Override
	protected byte getMyType() {
		return myType;
	}

}

