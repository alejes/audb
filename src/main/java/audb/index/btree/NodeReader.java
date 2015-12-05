package audb.index.btree;

import java.util.ArrayList;
import java.util.List;

import audb.index.IndexKeyInstance;
import audb.index.IndexValueInstance;
import audb.index.Index.Order;
import audb.page.Page;
import audb.page.PageReader;
import audb.page.PageStructure;
import audb.table.TableElement;
import audb.type.Type;

public class NodeReader {
	List<Type> keyTypes;
	PageStructure ps;
	Order[] orders;
	final int fanout;
	
	public NodeReader(List<Type> keyTypes, Order[] orders, PageStructure ps, int fanout) {
		this.keyTypes = keyTypes;
		this.ps = ps;
		this.orders = orders.clone();
		this.fanout = fanout;
	}
	
	public List<Type> getKeyTypes() {
		return keyTypes;
	}
	
	public PageStructure getPageStructure() {
		return ps;
	}
	
	public BTreeNode readNode(int pageNumber) {
		if (pageNumber < 0)
			return null;
		
		Page p = ps.getPage(pageNumber);
		PageReader pr = new PageReader(p);
		pr.rewind(1);
		
		byte type = p.data[0];
		
		int keysNumber = (int)Page.bytesToLong(pr.read(Long.BYTES));
		if (type == BTreeInnerNode.myType)
			return readInnerNode(pr, keysNumber);
		return readLeafNode(pr, keysNumber);
	}
	
	private ArrayList<IndexKeyInstance> readNodeKeys(PageReader pr, int keysNumber) {
		ArrayList<IndexKeyInstance> keys = new ArrayList<IndexKeyInstance>(fanout - 1);
		
		for (int i = 0; i < keysNumber; i++) {
			TableElement[] keyTableElements = new TableElement[keyTypes.size()];
			int idx = 0;
			for (Type keyType : keyTypes) {
				byte[] keyBytes = pr.read(keyType.getSize());
				keyTableElements[idx++] = keyType.fromBytes(keyBytes);
			}
			keys.add(new IndexKeyInstance(orders, keyTableElements));
		}
		
		return keys;
	}
	
	private BTreeNode readLeafNode(PageReader pr, int keysNumber) {
		ArrayList<IndexKeyInstance> keys = readNodeKeys(pr, keysNumber);
		ArrayList<IndexValueInstance> values  = new ArrayList<IndexValueInstance>(fanout - 1);
		
		for (int i = 0; i < keysNumber; i++) {
			int page = (int)Page.bytesToLong(pr.read(Long.BYTES));
			int offset = Page.bytesToInt(pr.read(Integer.BYTES));
			
			values.add(new IndexValueInstance(page, offset));
		}
		int nextNodePage = Page.bytesToInt(pr.read(Integer.BYTES));
		return new BTreeLeaf(fanout, this, pr.getCurrentPageNumber(), nextNodePage, 
				keys, values);
	}
	
	
	
	private BTreeNode readInnerNode(PageReader pr, int keysNumber) {
		ArrayList<IndexKeyInstance> keys = readNodeKeys(pr, keysNumber);
		ArrayList<Integer> children = new ArrayList<Integer>(fanout);
		for (int i = 0; i <= keysNumber; i++) {
			int page = (int)Page.bytesToLong(pr.read(Long.BYTES));
			children.add(page);
		}
		return new BTreeInnerNode(keysNumber, this, pr.getCurrentPageNumber(), 
				keys, children);
	}
}
