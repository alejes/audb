package audb.page;

import java.nio.ByteBuffer;


public class Page {

    private int pinCount;
    private long pageNumber;
    private boolean isDirty;

    public byte[] data;
    private PageManager pageManager;
    private long lastAccessTime = -1;

    public Page(PageManager pm, byte[] arr, long number) {
        data = arr;
        pageNumber = number;
        pinCount = 0;
        isDirty = false;
        pageManager = pm;
    }

    public void write() {
        isDirty = true;
    }

    public void flush() {
        pageManager.writePage(this);
    }

    public void pin() {
        pinCount += 1;
    }

    public boolean unpin() {
        if(pinCount == 0) return false;
        pinCount -= 1;
        return true;
    }

    public boolean isUnpinned() {
        return pinCount == 0;
    }

    public boolean isDirty() {
        return isDirty;
    }

    public long getPageNumber() {
        return pageNumber;
    }


    public long readLong(int offset) {
        byte[] bytes = new byte[Long.BYTES];
        System.arraycopy(data, offset, bytes, 0, bytes.length);
        return bytesToLong(bytes);
    }

    public void writeLong(int offset, long value) {
        byte[] bytes = longToBytes(value);
        System.arraycopy(bytes, 0, data, offset, bytes.length);
    }


    private static ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);

    public static byte[] longToBytes(long x) {
        buffer.clear();
        buffer.putLong(0, x);
        return buffer.array();
    }

    public static long bytesToLong(byte[] bytes) {
        buffer.clear();
        buffer.put(bytes, 0, bytes.length);
        buffer.flip(); 
        return buffer.getLong();
    }

	public long getLastAccessTime() {
		return lastAccessTime;
	}

	public void setLastAccessTime(long lastAccessTime) {
		this.lastAccessTime = lastAccessTime;
	}

}