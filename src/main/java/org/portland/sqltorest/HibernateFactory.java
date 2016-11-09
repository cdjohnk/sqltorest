package org.portland.sqltorest;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

public class HibernateFactory {

	private static SessionFactory sessionFactory = null;
	
	public static SessionFactory getSessionFactory() throws IOException {
		if (sessionFactory == null) {
			Configuration config = new Configuration();
			List<String> hbmFiles = new ResourceLoader().loadResource("/");
			for(int i=0; i<hbmFiles.size(); i++) {
				config.addResource(hbmFiles.get(i));
			}
			sessionFactory = config.buildSessionFactory();
		}
		return sessionFactory;
	}
}

class ResourceLoader {
	List<String> loadResource(String resource) throws IOException {
		List<String> hbmFiles = new ArrayList<String>();
		
		final File jarFile = new File(getClass().getProtectionDomain().getCodeSource().getLocation().getPath());
		if(jarFile.isFile()) {  // Run with JAR file
		    final JarFile jar = new JarFile(jarFile);
		    final Enumeration<JarEntry> entries = jar.entries(); //gives ALL entries in jar
		    while(entries.hasMoreElements()) {
		        String name = entries.nextElement().getName();
		        if (name.endsWith(".hbm.xml")) { //filter according to the path
		            hbmFiles.add(name);
		        	System.out.println(name);
		        }
		    }
		    jar.close();
		} else {
			InputStream is = getClass().getResourceAsStream(resource);
			BufferedReader br = new BufferedReader( new InputStreamReader(is));
			String name = "";
			while( (name = br.readLine()) != null ) {
				if (name.endsWith(".hbm.xml")) {
					hbmFiles.add(name);
					System.out.println(resource);
				}
			}
		}
		
		return hbmFiles;
	}
}
