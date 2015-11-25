package audb.page;

import java.util.concurrent.atomic.AtomicLong;
import audb.type.MutableLong;


public class PageStructure {

    private static PageCache pageCache = PageCache.getInstance();
    private PageManager pageManager;
    
    public static final long INFO_PAGE     = 0;
    
    public static final int INFO_END       = 3 * Long.BYTES;
    public static final int FIRST_EMPTY    = PageManager.PAGE_SIZE - 1 * Long.BYTES;
    public static final int LAST_EMPTY     = PageManager.PAGE_SIZE - 2 * Long.BYTES;
    public static final int COUNT_OF_PAGES = PageManager.PAGE_SIZE - 3 * Long.BYTES;
    public static final long EMPTY_END     = -1;
    
    public static final int NEXT_PAGE      = PageManager.PAGE_SIZE - 1 * Long.BYTES;
    public static final int PREV_PAGE      = PageManager.PAGE_SIZE - 2 * Long.BYTES;
    

    public PageStructure(PageManager pm) {
        pageManager = pm;
    }

    public PageManager getPageManager() {
        return pageManager;
    }

    public PageCache getPageCache() {
        return pageCache;
    }

    public void clear() {
        Page page = getPage(INFO_PAGE);
        page.writeLong(COUNT_OF_PAGES, 0l);
        page.writeLong(FIRST_EMPTY, EMPTY_END);
        page.writeLong(LAST_EMPTY, EMPTY_END);
    }

    public static void flush() {
        pageCache.flush();
    }

    public Page getPage(long pageNum) {
        return pageCache.getPage(pageManager, pageNum);
    }

    public void releasePage(long pageNum) {
        Page page = getPage(INFO_PAGE);
        MutableLong firstPage = new MutableLong(page.readLong(FIRST_EMPTY));
        MutableLong lastPage = new MutableLong(page.readLong(LAST_EMPTY));
        pushBack(pageNum, firstPage, lastPage, EMPTY_END);
        page.writeLong(FIRST_EMPTY, firstPage.get());
        page.writeLong(LAST_EMPTY, lastPage.get());
        page.write();
    }

    public long getEmptyPage() {
        Page page = getPage(INFO_PAGE);
        long pageNum = page.readLong(FIRST_EMPTY);
        if(pageNum != EMPTY_END) {
            MutableLong firstPage = new MutableLong(page.readLong(FIRST_EMPTY));
            MutableLong lastPage = new MutableLong(page.readLong(LAST_EMPTY));
            removePage(pageNum, firstPage, lastPage, EMPTY_END);
            page.writeLong(FIRST_EMPTY, firstPage.get());
            page.writeLong(LAST_EMPTY, lastPage.get());
            page.write();
        } else
            pageNum = getNewPage();
        return pageNum;
    }

    private long getCountOfPages() {
        Page page = getPage(INFO_PAGE);
        return page.readLong(COUNT_OF_PAGES);
    }

    private void setCountOfPages(long value) {
        Page page = getPage(INFO_PAGE);
        page.writeLong(COUNT_OF_PAGES, value);
        page.write();
    }

    private long getNewPage() {
        long countOfPages = getCountOfPages();
        countOfPages += 1;
        setCountOfPages(countOfPages);
        return countOfPages;
    }

    public void pushFront(long pageNum, MutableLong firstPage, MutableLong lastPage, long limiter) {

        Page page = getPage(pageNum);
        page.writeLong(PREV_PAGE, limiter);
        page.writeLong(NEXT_PAGE, firstPage.get());
        page.write();

        if(firstPage.get() != limiter) {
            page = getPage(firstPage.get());
            page.writeLong(PREV_PAGE, pageNum);
            page.write();
        } else
            lastPage.set(pageNum);

        firstPage.set(pageNum);
    }

    public void pushBack(long pageNum, MutableLong firstPage, MutableLong lastPage, long limiter) {

        Page page = getPage(pageNum);
        page.writeLong(PREV_PAGE, lastPage.get());
        page.writeLong(NEXT_PAGE, limiter);
        page.write();

        if(lastPage.get() != limiter) {
            page = getPage(lastPage.get());
            page.writeLong(NEXT_PAGE, pageNum);
            page.write();
        } else
            firstPage.set(pageNum);

        lastPage.set(pageNum);
    }

    public void removePage(long pageNum, MutableLong firstPage, MutableLong lastPage, long limiter) {
        Page page = getPage(pageNum);
        long prevPage = page.readLong(PREV_PAGE);
        long nextPage = page.readLong(NEXT_PAGE);

        if(prevPage != limiter) {
            page = getPage(prevPage);
            page.writeLong(NEXT_PAGE, nextPage);
            page.write();
        } else
            firstPage.set(nextPage);

        if(nextPage != limiter) {
            page = getPage(nextPage);
            page.writeLong(PREV_PAGE, prevPage);
            page.write();
        } else
            lastPage.set(prevPage);
    }

}
