package audb.index.btree;

import java.util.List;

import audb.page.PageStructure;
import audb.util.Pair;



public class BTree<K extends Comparable<K>, V> {
	BTreeNodeReference<K, V> root;
	PageStructure pageStructure;
	int fanout;

	public BTree(int fanout, PageStructure ps) {
		BTreeLeaf<K, V> leaf = new BTreeLeaf<K, V>(fanout, pageStructure, null);
		root = new BTreeNodeReference<K, V>(leaf);
		this.fanout = fanout;
		pageStructure = ps;
	}

	public void insert(K k, V v) {
		insert(Pair.newPair(k, v));
	}

	public void insert(Pair<K, V> p) {
		BTreeNodeReference<K, V> tmp = root.getValue().insert(p);
		if (null == tmp) {
			return;
		}

		BTreeInnerNode<K,V> node = new BTreeInnerNode<K, V>(fanout, pageStructure, root, tmp);
		root = new BTreeNodeReference<K, V>(node);
	}

	public void remove(K key) {
		root = root.getValue().remove(key);
	}

	public List<V> find(K key) {
		return root.getValue().find(key);
	}

	public List<V> findAll(K bottomKey, K topKey, List<K> excludeKeys) {
		return root.getValue().findAll(bottomKey, topKey, excludeKeys);
	}
}
