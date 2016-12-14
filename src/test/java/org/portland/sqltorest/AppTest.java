package org.portland.sqltorest;

import java.util.Map.Entry;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.log4j.Logger;

import java.net.*;
import java.io.*;

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

    static final Logger logger = Logger.getLogger(AppTest.class);

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
				logger.info(inputLine);
			in.close();
			server.stop();
		}
		catch (Throwable t) {
			logger.error("Error testing basic API retrieval", t);
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
		} catch (Throwable t) {
            logger.error("Error parsing test json", t);
        }
		
		String hql = parser.getHQL();

        logger.info(json);
        logger.info(hql);
		
		for (Entry<String, String> param : parser.getParameters().entrySet()) {
			Entry<String, String> entry = param;
            logger.info(entry.getKey() + "," + entry.getValue());
		}
        logger.info("\r\nPage Size: " + parser.getPageSize());
        logger.info("Page Start: " + parser.getPageStart());
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
                logger.error("Failed to start API", t);
			}
		}

		public void stop() {
			try {
				SqlToRest.shutdown();
			}
			catch (Throwable t) {
                logger.error("Failed to cleanly shutdown API", t);
			}
		}
	}
}
