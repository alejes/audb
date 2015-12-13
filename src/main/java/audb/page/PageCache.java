package audb.page;

import java.util.HashMap;
import java.io.IOException;


public class PageCache {
	private static final PageCache instance = new PageCache();
	
    private boolean DEBUG = false;
    private int MAX_SIZE = 100;

    private long timer;
    private HashMap<String, Page> hashMap;

    private PageCache() {
        timer = 0;
        hashMap = new HashMap<String, Page>();
    }
    
    public static PageCache getInstance() {
    	return instance;
    }

    public Page getPage(PageManager pm, int number) {
        Page page = null;
        String key = pm.getFileName() + "/" + number;
        if(hashMap.containsKey(key)) {
            page = hashMap.get(key);
        } else {
            if(hashMap.size() >= MAX_SIZE) {
                Page minPage = null;
                Page somePage = null;
                long minTime = Long.MAX_VALUE;
                for(String name: hashMap.keySet()) {
                    somePage = hashMap.get(name);
                    if(somePage.getLastAccessTime() < minTime && somePage.isUnpinned()) {
                        minTime = somePage.getLastAccessTime();
                        minPage = somePage;
                    }
                }
                if(minTime == Long.MAX_VALUE)
                    minPage = somePage;
                removePage(pm, minPage);
            }
            page = pm.readPage(number);
            hashMap.put(key, page);
        }
        page.setLastAccessTime(timer++);
        
        if(DEBUG) {
            for (String name: hashMap.keySet()){
                String keyName = name.toString();
                Long value = hashMap.get(name).getLastAccessTime();  
                System.out.println(keyName + " " + value);  
            } 
            System.out.println();
        }

        return page;
    }

    public void flush() {
        for(Page page: hashMap.values()) {
            if(DEBUG) System.out.println("Page closed: " + page.getPageNumber());
            page.flush();
        }
        hashMap.clear();
        System.err.println("Cache flushed.");
    }

    private void removePage(PageManager pm, Page page) { 
        if(DEBUG) System.out.println("Page closed: " + page.getPageNumber());
        page.flush();
        String key = pm.getFileName() + "/" + page.getPageNumber();
        hashMap.remove(key);
    }

}
