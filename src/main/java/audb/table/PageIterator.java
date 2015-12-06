package audb.table;

import audb.page.Page;
import audb.page.PageCache;
import audb.page.PageManager;
import audb.page.PageStructure;
import audb.table.Table;
import audb.type.Type;


public class PageIterator {

    private PageCache pageCache;
    private PageStructure pageStructure;

    int firstFullPageNumber;
    int currentPageNumber;

    private Page curPage = null;
    private int nextPageNumber;

    private boolean isPagePinned;


    public PageIterator(Table table) {
        this.pageStructure = table.getPageStructure();
        
        curPage = pageStructure.getPage(Table.INFO_PAGE);
        curPage.pin();
        isPagePinned = true;

        firstFullPageNumber = curPage.readInteger(Table.FIRST_FULL);
        currentPageNumber = curPage.readInteger(Table.CURRENT_PAGE);

        nextPageNumber = currentPageNumber;
        if(firstFullPageNumber != Table.FULL_END)
            nextPageNumber = firstFullPageNumber;
    
    }

    public Page getNext() {
        if(nextPageNumber == Table.INFO_PAGE)
            return null;

        curPage.unpin();
        curPage = pageStructure.getPage(nextPageNumber);
        isPagePinned = true;
        curPage.pin();

        if(nextPageNumber == currentPageNumber)
            nextPageNumber = Table.INFO_PAGE;
        else
            nextPageNumber = curPage.readInteger(Table.NEXT_PAGE);

        if(nextPageNumber == Table.FULL_END)
            nextPageNumber = currentPageNumber;

        return curPage;
    }

    public boolean hasNext() {
        if(nextPageNumber == Table.INFO_PAGE && isPagePinned) {
            isPagePinned = false;
            curPage.unpin();
        }
        return nextPageNumber != Table.INFO_PAGE;
    }

    public void close() {
        if(isPagePinned) {
            isPagePinned = false;
            curPage.unpin();
        }
    }
}
