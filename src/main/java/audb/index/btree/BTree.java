package audb.index.btree;

import java.util.List;

import audb.index.IndexKeyInstance;
import audb.index.IndexValueInstance;
import audb.index.Index.Order;
import audb.page.PageStructure;
import audb.type.Type;
import audb.util.Pair;



public class BTree {
	BTreeNode root;
	PageStructure pageStructure;
	NodeReader nr;
	int fanout;

	public BTree(int fanout, PageStructure ps, List<Type> keyTypes, Order[] orders) {
		nr = new NodeReader(keyTypes, orders, ps, fanout);
		root = new BTreeLeaf(fanout, nr, -1, -1, null, null);
		this.fanout = fanout;
		pageStructure = ps;
	}
	
	public BTree(int fanout, PageStructure ps, List<Type> keyTypes, Order[] orders, int rootPage) {
		nr = new NodeReader(keyTypes, orders, ps, fanout);
		root = nr.readNode(rootPage);
		this.fanout = fanout;
		pageStructure = ps;
	}
	
	public int getRootPage() {
		return root.getMyPage();
	}
	
	public int getFanout() {
		return fanout;
	}

	public boolean insert(IndexKeyInstance k, IndexValueInstance v) {
		return insert(Pair.newPair(k, v));
	}

	/**
	 * 
	 * @param p
	 * @return true if root has changed (so we need to update rootPage in index main page)
	 */
	public boolean insert(Pair<IndexKeyInstance, IndexValueInstance> p) {
		BTreeNode tmp = root.insert(p);
		if (null == tmp) {
			return false;
		}
		
		root = new BTreeInnerNode(fanout, nr, root, tmp);
		return true;
		//System.out.println("now root is " + Integer.toString(root.pageNumber));
	}

	public void remove(IndexKeyInstance key) {
		root = root.remove(key);
	}

	public List<IndexValueInstance> find(IndexKeyInstance key) {
		return root.find(key);
	}

	public List<IndexValueInstance> findAll(IndexKeyInstance bottomKey, 
			IndexKeyInstance topKey, List<IndexKeyInstance> excludeKeys) {
		return root.findAll(bottomKey, topKey, excludeKeys);
	}
}
