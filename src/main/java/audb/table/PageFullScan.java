package audb.table;

import audb.page.Page;
import audb.page.PageCache;
import audb.page.PageManager;
import audb.table.Table;
import audb.type.Type;

import java.io.Closeable;


public class PageFullScan implements Closeable {

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
        
        curPage = pageCache.getPage(pageManager, Table.INFO_PAGE);
        curPage.pin();
        isPagePinned = true;

        firstFullPage = curPage.readLong(Table.FIRST_FULL);
        firstEmptyPage = curPage.readLong(Table.FIRST_EMPTY);

        nextPage = Table.INFO_PAGE;
        if(firstFullPage != Table.FULL_END)
            nextPage = firstFullPage;
        else if(firstEmptyPage != Table.EMPTY_END)
            nextPage = firstEmptyPage;
    }

    public Page getNext() {
        if(nextPage == Table.INFO_PAGE)
            return null;

        curPage.unpin();
        curPage = pageCache.getPage(pageManager, nextPage);
        isPagePinned = true;
        curPage.pin();
        nextPage = curPage.readLong(Table.NEXT_PAGE);
        if(nextPage == Table.FULL_END)
            nextPage = firstEmptyPage;
        if(nextPage == Table.EMPTY_END)
            nextPage = Table.INFO_PAGE;

        return curPage;
    }

    public boolean hasNext() {
        if(nextPage == Table.INFO_PAGE && isPagePinned) {
            isPagePinned = false;
            curPage.unpin();
        }
        return nextPage != Table.INFO_PAGE;
    }

    public void close() {
        if(isPagePinned) {
            isPagePinned = false;
            curPage.unpin();
        }
    }
}
