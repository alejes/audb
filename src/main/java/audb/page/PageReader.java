package audb.page;

import java.util.Arrays;
import java.util.List;

public class PageReader {
	int offset = 0;
	final Page page;
	
	public PageReader(Page page) {
		this.page = page;
	}
	
	public int getCurrentPageNumber() {
		return page.getPageNumber();
	}
	
	public void rewind(int newOffset) {
		offset = newOffset;
	}
	
	public byte[] read(int size) {
		byte[] b = Arrays.copyOfRange(page.data, offset, offset + size);
		offset += size;
		return b;
	}
	
}
