package audb;

import java.util.HashMap;

import audb.index.BTree;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class BTreeTest extends TestCase {
	public BTreeTest(String testName) {
        super(testName);
    }
	
    public static Test suite() {
        return new TestSuite(BTreeTest.class);
    }
    
    public void testInsert() {
        BTree<Integer, String> t = new BTree<Integer, String>(5);
        
        HashMap<Integer, String> hm = new HashMap<Integer, String>();
        for (int i = 0; i < 100; i++) {
        	hm.put(i, Integer.toString(i));
        }
        
        for (int i = 0; i < 2; i++)
        	t.insert(i, hm.get(i));
        
        assertEquals(1, t.find(0).size());
        assertEquals(hm.get(0), t.find(0).get(0));
        
        for (int i = 2; i < 13; i++)
        	t.insert(i, hm.get(i));
        
        for (int i = 0; i < 13; i++) {
	        assertEquals(1, t.find(i).size());
	        assertEquals(hm.get(i), t.find(i).get(0));
        }
        
        // now 2 
        t.insert(3, "test");
        assertEquals(2, t.find(3).size());
        assertEquals(true, t.find(3).contains("3"));
        assertEquals(true, t.find(3).contains("test"));
        
        for (int i = 13; i < 100; i++) {
        	t.insert(i, hm.get(i));
        }
        
        assertEquals(1, t.find(21).size());
        assertEquals("18", t.find(18).get(0));
        assertEquals("67", t.find(67).get(0));
    }
    
    public void testRemove() {
        BTree<Integer, String> t = new BTree<Integer, String>(5);
        
        HashMap<Integer, String> hm = new HashMap<Integer, String>();
        for (int i = 0; i < 100; i++) {
        	hm.put(i, Integer.toString(i));
        }
        
        for (int i = 0; i < 100; i++)
        	t.insert(i, hm.get(i));
        
        assertTrue(t.find(67).size() == 1);
        t.remove(67);
        assertTrue(t.find(67).size() == 0);
        for (int i = 0; i < 100; i++)
        	t.remove(i);
        
        for (int i = 0; i < 100; i++)
        	assertEquals(0, t.find(i).size());
    }
}
