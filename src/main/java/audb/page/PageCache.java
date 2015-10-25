package audb.page;

import java.util.HashMap;
import java.io.IOException;


public class PageCache {

    private boolean DEBUG = false;
    private int MAX_SIZE = 100;

    private long timer;
    private HashMap<String, Element> hashMap;

    public PageCache() {
        timer = 0;
        hashMap = new HashMap<String, Element>();
    }

    public Page getPage(PageManager pm, long number) {
        Element el;
        String key = pm.getFileName() + "/" + number;
        if(hashMap.containsKey(key)) {
            el = hashMap.get(key);
            el.time = timer;
        } else {
            if(hashMap.size() >= MAX_SIZE) {
                Page minPage = null;
                Page somePage = null;
                long minTime = Long.MAX_VALUE;
                for(String name: hashMap.keySet()) {
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
            el = new Element(timer, pm.readPage(number)); 
            hashMap.put(key, el);
        }

        timer += 1;
       
        if(DEBUG) {
            for (String name: hashMap.keySet()){
                String keyName = name.toString();
                Long value = hashMap.get(name).time;  
                System.out.println(keyName + " " + value);  
            } 
            System.out.println();
        }

        return el.page;
    }

    public void flush() {
        for(Element el: hashMap.values())
            if(el.page.isDirty()) {
                if(DEBUG) System.out.println("Page closed: " + el.page.getPageNumber());
                el.page.flush();
            }
        hashMap.clear();
        System.err.println("Cache flushed.");
    }

    private void removePage(Page page) { 
        if(page.isDirty()) {
            if(DEBUG) System.out.println("Page closed: " + page.getPageNumber());
            page.flush();
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
