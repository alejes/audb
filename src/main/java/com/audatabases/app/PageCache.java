package com.audatabases.app;

import java.util.HashMap;
import java.util.TreeSet;


public class PageCache {
    private final int MAX_SIZE = 100;
    private TreeSet<Element> treeSetFreq;
    private TreeSet<Long> treeSetPage;
    private PageManager pageManager;
    private HashMap<Long, Element> hashMap;

    public PageCache(String fileName) throws Exception {
        hashMap = new HashMap<Long, Element>();
        pageManager = new PageManager(fileName);
    }

    public Page getPage(long number) {
        Element el;
        if (hashMap.containsKey(number)) {
            el = hashMap.remove(number);
            el.freq += 1; // TODO
        } else {
            if (hashMap.size() >= MAX_SIZE) {
                Page minPage = null;
                long minFreq = Long.MAX_VALUE;
                for (Long name: hashMap.keySet()) {
                    el = hashMap.get(name);
                    if (el.page.isUnpinned() && el.freq < minFreq) {
                        minFreq = el.freq;
                        minPage = el.page;
                    }
                }
                if (minFreq == Long.MAX_VALUE)
                    return null;
                removePage(minPage);
            }
            el = new Element(1, pageManager.readPage(number));
        }
        hashMap.put(number, el);

        // for (Long name: hashMap.keySet()){
        //     String key = name.toString();
        //     Long value = hashMap.get(name).freq;  
        //     System.out.println(key + " " + value);  
        // } 
        // System.out.println();
        return el.page;
    }

    public void close() {
        for (Element el: hashMap.values()) {    
            pageManager.writePage(el.page);
        }
        hashMap.clear();
    }

    private void removePage(Page page) { 
        // System.out.println("Closed: " + page.getPageNumber());
        pageManager.writePage(page);
        hashMap.remove(page.getPageNumber());
    }

    private class Element {
        public long freq;
        public Page page;
        public Element(int freq, Page page) {
            this.freq = freq;
            this.page = page;
        }
    }


}
