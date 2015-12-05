package audb.page;

public class PageWriter {
	Page page;
	int offset = 0;
	
	public PageWriter(Page page) {
		this.page = page;
	}
	
	void rewind(int newOffset) {
		offset = newOffset;
	}
	
	public void writeData(byte[] data) {
		page.writeData(data, offset);
		offset += data.length;
	}
	
	public void writeLong(long value) {
		page.writeLong(offset, value);
		offset += Long.BYTES;
	}
	
	public void writeInteger(int value) {
		page.writeInteger(offset, value);
		offset += Integer.BYTES;
	}
	
	public void writeByte(byte value) {
		page.writeByte(offset, value);
		offset += 1;
	}
}
