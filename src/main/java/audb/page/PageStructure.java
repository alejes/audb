package audb.page;

import audb.type.MutableInt;

public class PageStructure {

    private static PageCache pageCache = PageCache.getInstance();
    private PageManager pageManager;
    
    public static final int INFO_PAGE     = 0;
    
    public static final int INFO_END       = 3 * Integer.BYTES;
    public static final int FIRST_EMPTY    = PageManager.PAGE_SIZE - 1 * Integer.BYTES;
    public static final int LAST_EMPTY     = PageManager.PAGE_SIZE - 2 * Integer.BYTES;
    public static final int COUNT_OF_PAGES = PageManager.PAGE_SIZE - 3 * Integer.BYTES;
    public static final int EMPTY_END     = -1;
    
    public static final int NEXT_PAGE      = PageManager.PAGE_SIZE - 1 * Integer.BYTES;
    public static final int PREV_PAGE      = PageManager.PAGE_SIZE - 2 * Integer.BYTES;
    

    public PageStructure(PageManager pm) {
        pageManager = pm;
    }

    public void clear() {
        Page page = getPage(INFO_PAGE);
        page.writeInteger(COUNT_OF_PAGES, 0);
        page.writeInteger(FIRST_EMPTY, EMPTY_END);
        page.writeInteger(LAST_EMPTY, EMPTY_END);
    }

    public static void flush() {
        pageCache.flush();
    }

    public Page getPage(int pageNum) {
        return pageCache.getPage(pageManager, pageNum);
    }

    public void releasePage(int pageNum) {
        Page page = getPage(INFO_PAGE);
        MutableInt firstPage = new MutableInt(page.readInteger(FIRST_EMPTY));
        MutableInt lastPage = new MutableInt(page.readInteger(LAST_EMPTY));
        pushBack(pageNum, firstPage, lastPage, EMPTY_END);
        page.writeInteger(FIRST_EMPTY, firstPage.get());
        page.writeInteger(LAST_EMPTY, lastPage.get());
        page.write();
    }

    public int getEmptyPage() {
        Page page = getPage(INFO_PAGE);
        int pageNum = page.readInteger(FIRST_EMPTY);
        if(pageNum != EMPTY_END) {
            MutableInt firstPage = new MutableInt(page.readInteger(FIRST_EMPTY));
            MutableInt lastPage = new MutableInt(page.readInteger(LAST_EMPTY));
            removePage(pageNum, firstPage, lastPage, EMPTY_END);
            page.writeInteger(FIRST_EMPTY, firstPage.get());
            page.writeInteger(LAST_EMPTY, lastPage.get());
            page.write();
        } else
            pageNum = getNewPage();
        return pageNum;
    }

    public int getCountOfPages() {
        Page page = getPage(INFO_PAGE);
        return page.readInteger(COUNT_OF_PAGES);
    }

    private void setCountOfPages(int value) {
        Page page = getPage(INFO_PAGE);
        page.writeInteger(COUNT_OF_PAGES, value);
        page.write();
    }

    private int getNewPage() {
        int countOfPages = getCountOfPages();
        countOfPages += 1;
        setCountOfPages(countOfPages);
        return countOfPages;
    }

    public void pushFront(int pageNum, MutableInt firstPage, MutableInt lastPage, int limiter) {

        Page page = getPage(pageNum);
        page.writeInteger(PREV_PAGE, limiter);
        page.writeInteger(NEXT_PAGE, firstPage.get());
        page.write();

        if(firstPage.get() != limiter) {
            page = getPage(firstPage.get());
            page.writeInteger(PREV_PAGE, pageNum);
            page.write();
        } else
            lastPage.set(pageNum);

        firstPage.set(pageNum);
    }

    public void pushBack(int pageNum, MutableInt firstPage, MutableInt lastPage, int limiter) {

        Page page = getPage(pageNum);
        page.writeInteger(PREV_PAGE, lastPage.get());
        page.writeInteger(NEXT_PAGE, limiter);
        page.write();

        if(lastPage.get() != limiter) {
            page = getPage(lastPage.get());
            page.writeInteger(NEXT_PAGE, pageNum);
            page.write();
        } else
            firstPage.set(pageNum);

        lastPage.set(pageNum);
    }

    public void removePage(int pageNum, MutableInt firstPage, MutableInt lastPage, int limiter) {
        Page page = getPage(pageNum);
        int prevPage = page.readInteger(PREV_PAGE);
        int nextPage = page.readInteger(NEXT_PAGE);

        if(prevPage != limiter) {
            page = getPage(prevPage);
            page.writeInteger(NEXT_PAGE, nextPage);
            page.write();
        } else
            firstPage.set(nextPage);

        if(nextPage != limiter) {
            page = getPage(nextPage);
            page.writeInteger(PREV_PAGE, prevPage);
            page.write();
        } else
            lastPage.set(prevPage);
    }

}
