package org.portland.sqltorest;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

public class Util {
	public String getFile(String fileName) {
    	//Get file from resources folder
    	InputStream is = getClass().getResourceAsStream(fileName);
    	String val = "";
    	try {
            BufferedReader r = new BufferedReader(new InputStreamReader(is));

            // reads each line
            String l;
            while((l = r.readLine()) != null) {
               val = val + l + "\r\n";
            } 
            is.close();
         } 
         catch(Exception e) {
            System.out.println(e);
         }
         return val;
 	}
}
