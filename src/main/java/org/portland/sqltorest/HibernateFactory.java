package org.portland.sqltorest;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

public class HibernateFactory {

	private static SessionFactory sessionFactory = null;
	
	public static SessionFactory getSessionFactory() throws IOException {
		if (sessionFactory == null) {
			Configuration config = new Configuration();
			List<String> hbmFiles = new ArrayList<String>();
			ResourceLoader loader = new ResourceLoader();
			hbmFiles.addAll(loader.loadResource("/"));
			try {
				hbmFiles.addAll(new ResourceLoader().loadResource("/config/"));
			} catch (Throwable t) {
				System.out.println("config directory not available for loading hbm files.");
			}
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
		String appPath = System.getProperty("app.path");
		System.out.println("app.path=" + appPath);
		File configFile = new File(appPath + resource);
		File[] listOfFiles = configFile.listFiles();

		for (File file : listOfFiles) {
		    if (file.isFile() && file.getName().endsWith(".hbm.xml")) {
				hbmFiles.add(file.getName());
				System.out.println(resource);
			}
		}
		
		return hbmFiles;
	}
}
