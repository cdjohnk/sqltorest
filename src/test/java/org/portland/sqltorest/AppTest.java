package org.portland.sqltorest;

import java.io.IOException;
import java.util.Map.Entry;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

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
        String json = new Util().getFile("/test.json");
		String tables = "FROM Test as test";
		
		SearchParser parser = null;
		try {
			parser = new SearchParser(tables, json);
		} catch (JsonParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonMappingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		String hql = parser.getHQL();
		
		System.out.println(json);
		System.out.println(hql);
		
		for (Object param : parser.getParameters().entrySet()) {
			Entry entry = (Entry)param;
			System.out.println(entry.getKey() + "," + entry.getValue());
		}
		System.out.println("\r\nPage Size: " + parser.getPageSize());
		System.out.println("Page Start: " + parser.getPageStart());
    	assertTrue( true );
    }
}
