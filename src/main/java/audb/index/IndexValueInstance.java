package audb.index;

public class IndexValueInstance implements Comparable<IndexValueInstance> {
	public final int page;
	public final int offset;
	
	public IndexValueInstance(int page, int offset) {
		this.page = page;
		this.offset = offset;
	}

	// dummy method
	public int compareTo(IndexValueInstance o) {
		return Integer.compare(page, o.page);
	}
}