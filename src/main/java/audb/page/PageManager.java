package audb.page;

import java.io.File;
import java.io.RandomAccessFile;


public class PageManager {
    
    public static final int PAGE_SIZE   = 4096;
    public static final int START_SIZE = 512;

    private File file;
    private RandomAccessFile raf;
    private String fileName;
    private int pageCount;


    public PageManager(String fileName) throws Exception {
        this.fileName = fileName;
        file = new File(fileName);
        if(!file.exists()) {
            file.getParentFile().mkdirs();
            file.createNewFile();
        }
        raf = new RandomAccessFile(file, "rw");
        pageCount = (int) (file.length() / PAGE_SIZE + 1);
        // for(int i = pageCount; i < START_SIZE; i++) 
        if (pageCount < START_SIZE)
            addPage();
    }

    public Page readPage(int number) {
        if(number >= pageCount) {
            try {
                addPage();
            } catch(Exception e) {
                System.out.println("Can't add pages");
                return null;
            }
        }

        byte[] arr = new byte[PAGE_SIZE];
        try{
            raf.seek(number * PAGE_SIZE);
            raf.read(arr);
        } catch(Exception e) {
            e.printStackTrace();
        }
        return new Page(this, arr, number);
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

    private void addPage() throws Exception {
        raf.seek(pageCount * PAGE_SIZE);
        byte[] arr = new byte[PAGE_SIZE * START_SIZE];
        raf.write(arr);
        pageCount += START_SIZE;
        // return new Page(arr, pageCount - 1);
    }

    public String getFileName() {
        return fileName;
    }

}
