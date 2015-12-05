package audb.type;

import audb.page.Page;
import audb.table.DoubleElement;
import audb.table.TableElement;

public class DoubleType extends Type {

	DoubleType(byte id) {
		super(id);
		// TODO Auto-generated constructor stub
	}

	@Override
	public int getSize() {
		return Double.BYTES;
	}

	@Override
	public byte[] toBytes(Object o) throws Exception {
		return Page.doubleToBytes(((DoubleElement)o).value);
	}

	@Override
	public boolean isValid(Object o) {
		return true;
	}

	@Override
	public TableElement fromBytes(byte[] data) {
		return new DoubleElement(Page.bytesToDouble(data), this);
	}

	@Override
	public TableElement fromObject(Object obj) {
		return new DoubleElement((Double)obj, this);
	}

}
