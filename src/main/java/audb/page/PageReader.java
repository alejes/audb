package audb.page;

import java.util.Arrays;
import java.util.List;

public class PageReader {
	int offset = 0;
	Page page;
	
	public PageReader(Page page) {
		this.page = page;
	}
	
	public int getCurrentPageNumber() {
		return (int)page.getPageNumber();
	}
	
	public void rewind(int newOffset) {
		offset = newOffset;
	}
	
	public byte[] read(int size) {
		try{
		byte[] b = Arrays.copyOfRange(page.data, offset, offset + size);
		offset += size;
		return b;
		} catch(ArrayIndexOutOfBoundsException e) {
			e.printStackTrace();
		}
		return null;
	}
	
}
