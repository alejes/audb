package audb.table;

import audb.page.Page;
import audb.page.PageCache;
import audb.page.PageManager;
import audb.table.Table;
import audb.type.Type;

import java.io.Closeable;


public class PageFullScan implements Closeable {

    public static final long INFO_PAGE = Table.INFO_PAGE;
    public static final int NEXT_PAGE = Table.NEXT_PAGE;
    public static final int PREV_PAGE = Table.PREV_PAGE;

    public static final int FIRST_EMPTY = Table.FIRST_EMPTY;
    public static final int FIRST_FULL = Table.FIRST_FULL;
    public static final long EMPTY_END = Table.EMPTY_END;
    public static final long FULL_END = Table.FULL_END;

    private PageCache pageCache;
    private PageManager pageManager;

    long firstFullPage;
    long firstEmptyPage;

    private Page curPage = null;
    private long nextPage;

    private boolean isPagePinned;


    public PageFullScan(Table table) {
        this.pageCache = table.getPageCache();
        this.pageManager = table.getPageManager();
        
        curPage = pageCache.getPage(pageManager, INFO_PAGE);
        curPage.pin();
        isPagePinned = true;

        firstFullPage = curPage.readLong(FIRST_FULL);
        firstEmptyPage = curPage.readLong(FIRST_EMPTY);

        nextPage = INFO_PAGE;
        if(firstFullPage != FULL_END)
            nextPage = firstFullPage;
        else if(firstEmptyPage != EMPTY_END)
            nextPage = firstEmptyPage;
    }

    public Page getNext() {
        if(nextPage == INFO_PAGE)
            return null;

        curPage.unpin();
        curPage = pageCache.getPage(pageManager, nextPage);
        curPage.pin();
        nextPage = curPage.readLong(NEXT_PAGE);
        if(nextPage == FULL_END)
            nextPage = firstEmptyPage;
        if(nextPage == EMPTY_END)
            nextPage = INFO_PAGE;

        return curPage;
    }

    public boolean hasNext() {
        if(nextPage == INFO_PAGE && isPagePinned) {
            isPagePinned = false;
            curPage.unpin();
        }
        return nextPage != INFO_PAGE;
    }

    private void findNextPage() {

    }


    public void close() {
        if(isPagePinned) {
            isPagePinned = false;
            curPage.unpin();
        }
    }
}
