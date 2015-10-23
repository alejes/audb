package audb.table;

import audb.page.Page;
import audb.page.PageCache;
import audb.page.PageManager;
import audb.page.PageStructure;
import audb.table.Table;
import audb.type.Type;


public class PageFullScan {

    private PageCache pageCache;
    private PageStructure pageStructure;

    long firstFullPage;
    long currentPage;

    private Page curPage = null;
    private long nextPage;

    private boolean isPagePinned;


    public PageFullScan(Table table) {
        this.pageStructure = table.getPageStructure();
        
        curPage = pageStructure.getPage(Table.INFO_PAGE);
        curPage.pin();
        isPagePinned = true;

        firstFullPage = curPage.readLong(Table.FIRST_FULL);
        currentPage = curPage.readLong(Table.CURRENT_PAGE);

        nextPage = currentPage;
        if(firstFullPage != Table.FULL_END)
            nextPage = firstFullPage;
    
    }

    public Page getNext() {
        if(nextPage == Table.INFO_PAGE)
            return null;

        curPage.unpin();
        curPage = pageStructure.getPage(nextPage);
        isPagePinned = true;
        curPage.pin();

        if(nextPage == currentPage)
            nextPage = Table.INFO_PAGE;
        else
            nextPage = curPage.readLong(Table.NEXT_PAGE);

        if(nextPage == Table.FULL_END)
            nextPage = currentPage;

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
