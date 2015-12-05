package audb.index.btree;

import java.util.ArrayList;
import java.util.List;

import audb.index.IndexKeyInstance;
import audb.index.IndexValueInstance;
import audb.page.PageWriter;
import audb.page.Page;
import audb.page.PageStructure;
import audb.util.Pair;

public abstract class BTreeNode {
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
	NodeReader nodeReader;
	List<IndexKeyInstance> keys;
	
	public BTreeNode(int fanout, NodeReader nr, int pageNumber, List<IndexKeyInstance> keys) {
		this.fanout = fanout;
		maxKeysNumber = fanout - 1;
		minChildrenNumber = (fanout + 1) / 2;
		this.nodeReader = nr;
		this.pageNumber = pageNumber == -1 ? (int)nr.getPageStructure().getEmptyPage() : pageNumber;
	/*	if (pageNumber != -1) {
			readUp();
		}*/
		this.keys = keys == null ? new ArrayList<IndexKeyInstance>(maxKeysNumber) : keys;
	}

	public BTreeNode(int fanout, NodeReader nr, BTreeNode child1, BTreeNode child2) {
		maxKeysNumber = fanout - 1;
		minChildrenNumber = (fanout + 1) / 2;
		this.fanout = fanout;
		keys = new ArrayList<IndexKeyInstance>(maxKeysNumber);
		keys.add(child1.getMaxKey());
		this.pageNumber = (int)nr.getPageStructure().getEmptyPage();
	}

	abstract BTreeNode insert(Pair<IndexKeyInstance, IndexValueInstance> p);

	protected abstract RemoveResult remove(IndexKeyInstance key, BTreeNode leftSibling, 
			BTreeNode rightSibling);

	public abstract BTreeNode remove(IndexKeyInstance key);

	protected void splitNode(BTreeNode emptyNode) {
		emptyNode.keys.addAll(keys.subList(minChildrenNumber, fanout));
		keys.subList(minChildrenNumber - 1, keys.size()).clear();
	}

	// TODO binary search is a must here
	protected int findChildIndex(IndexKeyInstance key, int maxIndex) {
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

	protected PageWriter writeDown() {
		Page p = nodeReader.getPageStructure().getPage(pageNumber);
		PageWriter pw = new PageWriter(p);
		pw.writeByte(getMyType());
		pw.writeLong(keys.size());
		int elementsInKey = keys.get(0).elements.length; // TODO check
		for (int i = 0; i < keys.size(); i++) {
			for (int j = 0; j < elementsInKey; j++) {
				pw.writeData(keys.get(i).elements[j].toBytes());
			}
		}
		return pw;
	}
	
	protected abstract int getChildrenNumber();
	protected abstract IndexKeyInstance getMaxKey();
	protected abstract byte getMyType();
	public abstract List<IndexValueInstance> find(IndexKeyInstance key);
	public abstract List<IndexValueInstance> findAll(IndexKeyInstance bottomKey, 
			IndexKeyInstance topKey, List<IndexKeyInstance> excludeKeys);
}