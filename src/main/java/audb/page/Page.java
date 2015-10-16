package audb.page;

import audb.util.ByteUtils;


public class Page {

    private int pinCount;
    private long pageNumber;
    private boolean isDirty;

    public byte[] data;


    public Page(byte[] arr, long number) {
        data = arr;
        pageNumber = number;
        pinCount = 0;
        isDirty = false;
    }

    public void write() {
        isDirty = true;
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
        return ByteUtils.bytesToLong(bytes);
    }

    public void writeLong(int offset, long pageNum) {
        byte[] bytes = ByteUtils.longToBytes(pageNum);
        System.arraycopy(bytes, 0, data, offset, bytes.length);
    }

}