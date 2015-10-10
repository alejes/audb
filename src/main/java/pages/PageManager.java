package pages;

import java.io.File;
import java.io.RandomAccessFile;


public class PageManager {

    private File file;
    private RandomAccessFile raf;
    private long pageCount;
    private static final int PAGE_SIZE = 4096;
    private static final long START_SIZE = 512;


    public PageManager(String fileName) throws Exception {
        file = new File(fileName);
        if (!file.exists()) {
            file.getParentFile().mkdirs();
            file.createNewFile();
        }
        raf = new RandomAccessFile(file, "rw");
        pageCount = file.length() / PAGE_SIZE + 1;
        for (long i = pageCount; i < START_SIZE; i++) 
            addPage();
    }

    public Page readPage(long number) {
        if (number >= pageCount)
            return null;
        byte[] arr = new byte[PAGE_SIZE];
        try {
            raf.seek(number * PAGE_SIZE);
            raf.read(arr);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new Page(arr, number);
    }

    public void writePage(Page p) {
        if(!p.isDirty())
            return;
        try {
            raf.seek(p.getPageNumber() * PAGE_SIZE);
            raf.write(p.data);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    private Page addPage() throws Exception {
        raf.seek(pageCount * PAGE_SIZE);
        byte[] arr = new byte[PAGE_SIZE];
        raf.write(arr);
        return new Page(arr, pageCount++);
    }

}
