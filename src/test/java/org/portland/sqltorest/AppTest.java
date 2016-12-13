package org.portland.sqltorest;

import java.io.IOException;
import java.util.Map.Entry;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.net.*;
import java.io.*;

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

    public void setUp() throws Exception {
        super.setUp();
        InputStream in = SqlToRest.class.getClassLoader().getResourceAsStream("sqltorest.properties");
        System.getProperties().load(in);
        in.close();
    }

    public void testApi() {
		try {
			TestServer server = new TestServer();
            server.start();
			URL apiUrl = new URL("http://localhost:8062/api/test");
			URLConnection yc = apiUrl.openConnection();
			BufferedReader in = new BufferedReader(
					new InputStreamReader(
							yc.getInputStream()));
			String inputLine;

			while ((inputLine = in.readLine()) != null)
				System.out.println(inputLine);
			in.close();
			server.stop();
		}
		catch (Throwable t) {
			t.printStackTrace();
		}
	}
    /**
     * Rigourous Test :-)
     */
    public void testParser()
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
		
		for (Entry<String, String> param : parser.getParameters().entrySet()) {
			Entry<String, String> entry = param;
			System.out.println(entry.getKey() + "," + entry.getValue());
		}
		System.out.println("\r\nPage Size: " + parser.getPageSize());
		System.out.println("Page Start: " + parser.getPageStart());
    	assertTrue( true );
    }

	protected class TestServer implements Runnable {

        public void start() throws InterruptedException {
            Thread api = new Thread(this);
            api.start();
            Thread.sleep(5000);
        }

        @Override
		public void run() {
			try {
				System.setProperty("app.path", "c:/Users/pbcjohnk/Documents/workspace/sqltorest/src/test/resources");
				SqlToRest.main(new String[]{});
			}
			catch (Throwable t) {
				t.printStackTrace();
			}
		}

		public void stop() {
			try {
				SqlToRest.shutdown();
			}
			catch (Throwable t) {
				t.printStackTrace();
			}
		}
	}
}
