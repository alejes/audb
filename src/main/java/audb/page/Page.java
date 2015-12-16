package audb.page;

import java.nio.ByteBuffer;


public class Page {

    private int pinCount;
    private int pageNumber;
    private boolean isDirty;

    public byte[] data;
    private PageManager pageManager;
    private long lastAccessTime = -1;

    public Page(PageManager pm, byte[] arr, int number) {
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

    public int getPageNumber() {
        return pageNumber;
    }

    public String getFileName() {
        return pageManager.getFileName();
    }


    public long readLong(int offset) {
        byte[] bytes = new byte[Long.BYTES];
        System.arraycopy(data, offset, bytes, 0, bytes.length);
        return bytesToLong(bytes);
    }

    public int readInteger(int offset) {
        byte[] bytes = new byte[Integer.BYTES];
        System.arraycopy(data, offset, bytes, 0, bytes.length);
        return bytesToInt(bytes);
    }
    
    public void writeInteger(int offset, int value) {
    	byte[] bytes = intToBytes(value);
    	System.arraycopy(bytes, 0, data, offset, bytes.length);
    }
    
    public void writeLong(int offset, long value) {
        byte[] bytes = longToBytes(value);
        System.arraycopy(bytes, 0, data, offset, bytes.length);
    }
    
    public void writeByte(int offset, byte value) {
    	data[offset] = value;
    }
    
    public void writeData(byte[] dataBytes, int offset) {
    	System.arraycopy(dataBytes, 0, data, offset, dataBytes.length);
    }


    private static ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
    private static ByteBuffer intBuffer = ByteBuffer.allocate(Integer.BYTES);
    private static ByteBuffer doubleBuffer = ByteBuffer.allocate(Double.BYTES);
    
    public static byte[] longToBytes(long x) {
        buffer.clear();
        buffer.putLong(0, x);
        return buffer.array();
    }
    
    public static byte[] intToBytes(int x) {
    	intBuffer.clear();
    	intBuffer.putInt(0, x);
        return intBuffer.array();
    }
    
    public static byte[] doubleToBytes(double x) {
    	doubleBuffer.clear();
    	doubleBuffer.putDouble(0, x);
        return doubleBuffer.array();
    }
    
    public static long bytesToLong(byte[] bytes) {
        buffer.clear();
        buffer.put(bytes, 0, Long.BYTES);
        buffer.flip(); 
        return buffer.getLong();
    }
    
    public static int bytesToInt(byte[] bytes) {
    	intBuffer.clear();
    	intBuffer.put(bytes, 0, Integer.BYTES);
    	intBuffer.flip(); 
        return intBuffer.getInt();
    }
    
    public static double bytesToDouble(byte[] bytes) {
    	doubleBuffer.clear();
    	doubleBuffer.put(bytes, 0, Double.BYTES);
    	doubleBuffer.flip(); 
        return doubleBuffer.getDouble();
    }

	public long getLastAccessTime() {
		return lastAccessTime;
	}

	public void setLastAccessTime(long lastAccessTime) {
		this.lastAccessTime = lastAccessTime;
	}

}