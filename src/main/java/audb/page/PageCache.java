package audb.page;

// import java.util.Arrays;
import java.util.HashMap;
import java.util.TreeSet;


public class PageCache {
    private int MAX_SIZE = 100;
    private boolean DEBUG = false;
    private long timer;
    private TreeSet<Element> treeSetFreq;
    private TreeSet<Long> treeSetPage;
    private PageManager pageManager;
    private HashMap<Long, Element> hashMap;

    public PageCache(String fileName) throws Exception {
        timer = 0;
        hashMap = new HashMap<Long, Element>();
        pageManager = new PageManager(fileName);
    }

    public Page getPage(long number) {
        Element el;
        if(hashMap.containsKey(number)) {
            el = hashMap.remove(number);
            el.time = timer;
        } else {
            if(hashMap.size() >= MAX_SIZE) {
                Page minPage = null;
                Page somePage = null;
                long minTime = Long.MAX_VALUE;
                for(Long name: hashMap.keySet()) {
                    el = hashMap.get(name);
                    somePage = el.page;
                    if(el.time < minTime && el.page.isUnpinned()) {
                        minTime = el.time;
                        minPage = el.page;
                    }
                }
                if(minTime == Long.MAX_VALUE)
                    minPage = somePage;
                removePage(minPage);
            }
            el = new Element(timer, pageManager.readPage(number));
        }

        timer += 1;
        hashMap.put(number, el);

        if(DEBUG) {
            for (Long name: hashMap.keySet()){
                String key = name.toString();
                Long value = hashMap.get(name).time;  
                System.out.println(key + " " + value);  
            } 
            System.out.println();
        }

        return el.page;
    }

    public void close() {
        for(Element el: hashMap.values())
            if(el.page.isDirty()) {
                if(DEBUG) System.out.println("Page closed: " + el.page.getPageNumber());
                pageManager.writePage(el.page);
            }
        hashMap.clear();
    }

    private void removePage(Page page) { 
        if(page.isDirty()) {
            if(DEBUG) System.out.println("Page closed: " + page.getPageNumber());
            pageManager.writePage(page);
        }
        hashMap.remove(page.getPageNumber());

    }

    private class Element {
        public long time;
        public Page page;
        public Element(long time, Page page) {
            this.time = time;
            this.page = page;
        }
    }


}
