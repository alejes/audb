package audb.type;

import audb.page.Page;
import audb.table.IntegerElement;
import audb.table.TableElement;

public class IntegerType extends Type {

	IntegerType(byte id) {
		super(id);
		// TODO Auto-generated constructor stub
	}

	@Override
	public int getSize() {
		return Integer.BYTES;
	}

	@Override
	public byte[] toBytes(Object o) throws Exception {
		return Page.intToBytes(((IntegerElement)o).value);
	}

	@Override
	public boolean isValid(Object o) {
		return true;
	}

	@Override
	public TableElement fromBytes(byte[] data) {
		return new IntegerElement(Page.bytesToInt(data), this);
	}

	@Override
	public TableElement fromObject(Object obj) {
		return new IntegerElement((Integer)obj, this);
	}

}
