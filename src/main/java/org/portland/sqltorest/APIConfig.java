package org.portland.sqltorest;

import java.io.IOException;
import java.io.StringReader;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.model.Resource;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.portland.sqltorest.Util;

public class APIConfig extends ResourceConfig {

	public APIConfig() throws Exception {
		Document apiDoc = loadApi();
		Element root = apiDoc.getDocumentElement();

		final Resource.Builder resourceBuilder = Resource.builder();
		resourceBuilder.path(root.getAttribute("name"));

		NodeList nodes = root.getChildNodes();
		for (int i = 0; i < nodes.getLength(); i++) {
			Node setNode = nodes.item(i);
			String name = setNode.getAttributes().getNamedItem("name").getNodeValue();
			Node hqlNode = setNode.getFirstChild();
			String query = hqlNode.getTextContent();
			new ResourceHelper().addSetPath(resourceBuilder, name, query);
			
			//Map searchable nodes
			Node searchable = setNode.getAttributes().getNamedItem("searchable");
			if (searchable != null && searchable.getNodeValue().equals("true")) {
				new ResourceHelper().addSearchPath(resourceBuilder, name, query);
			}

			Node recordNode = setNode.getLastChild();
			String idField = recordNode.getAttributes().getNamedItem("idFields").getNodeValue();
			new ResourceHelper().addRecordPath(resourceBuilder, name, query, idField);
		}
		
		//TODO: Map sets under records

		final Resource resource = resourceBuilder.build();
		registerResources(resource);

	}
	
	private Document loadApi() throws ParserConfigurationException,
			SAXException, IOException {
		String apiXml = new Util().getFile("/api.xml");
		apiXml = apiXml.replaceAll("\\s+", " ").replaceAll("> <", "><");
		InputSource apiSource = new InputSource(new StringReader(apiXml));
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document apiDoc = builder.parse(apiSource);
		return apiDoc;
	}
}