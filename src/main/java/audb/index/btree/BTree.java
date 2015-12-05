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

	public void insert(IndexKeyInstance k, IndexValueInstance v) {
		insert(Pair.newPair(k, v));
	}

	public void insert(Pair<IndexKeyInstance, IndexValueInstance> p) {
		BTreeNode tmp = root.insert(p);
		if (null == tmp) {
			return;
		}

		root = new BTreeInnerNode(fanout, nr, root, tmp);
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
