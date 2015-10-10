package com.audatabases.app;

import pages.Page;
import pages.PageCache;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit test for simple App.
 */
public class AppTest 
    extends TestCase
{
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public AppTest( String testName )
    {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( AppTest.class );
    }

    /**
     * Rigourous Test :-)
     */
    public void testApp()
    {
        assertTrue( true );
    }

    public void testAdd1Plus1() 
    {
        try {
            PageCache pc = new PageCache("db/tst");
            Page p = pc.getPage(1);
            for(byte i = 0; i < 100; i++)
                p.data[i] = i;
            p.write();
            pc.close();

            pc = new PageCache("db/tst");
            p = pc.getPage(1);
            for (byte i = 0; i < 100; i++)
                assertEquals(i, p.data[i]);
            pc.close();

        } catch(Exception e) {
            assertTrue(false);
        }
    }
}
