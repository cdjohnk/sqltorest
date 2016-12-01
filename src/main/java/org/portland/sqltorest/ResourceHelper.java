package org.portland.sqltorest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import org.apache.commons.io.IOUtils;
import org.glassfish.jersey.process.Inflector;
import org.glassfish.jersey.server.model.Resource;
import org.glassfish.jersey.server.model.ResourceMethod;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import org.hibernate.type.Type;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.MediaType;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class ResourceHelper {
	public void addSetPath(Resource.Builder resourceBuilder, String path, final String query) {
		final Resource.Builder childBuilder = resourceBuilder.addChildResource(path);
		final ResourceMethod.Builder methodBuilder = childBuilder.addMethod("GET");
		methodBuilder.produces(MediaType.APPLICATION_JSON).handledBy(
			new Inflector<ContainerRequestContext, String>() {
	
				@Override
				public String apply(ContainerRequestContext containerRequestContext) {
					StringWriter stringEmp = new StringWriter();
					try {
						List<Map<String, Object>> result = getResultList(query, null);
						
						ObjectMapper objectMapper = new ObjectMapper();
						objectMapper.configure(SerializationFeature.INDENT_OUTPUT, true);
						objectMapper.writeValue(stringEmp, result);
					} catch (Throwable e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						return null;
					}
					return stringEmp.toString();
				}
			});
	}

	public void addSearchPath(Resource.Builder resourceBuilder, String path, final String query) {
		final Resource.Builder childBuilder = resourceBuilder.addChildResource(path + "/search");
		final ResourceMethod.Builder methodBuilder = childBuilder.addMethod("POST");
		methodBuilder.consumes(MediaType.APPLICATION_JSON);
		methodBuilder.produces(MediaType.APPLICATION_JSON).handledBy(
			new Inflector<ContainerRequestContext, String>() {
	
				@Override
				public String apply(ContainerRequestContext containerRequestContext) {
					StringWriter stringEmp = new StringWriter();
			        if (containerRequestContext.getMediaType().getSubtype().equals("json")) {
						try {
							InputStream is = containerRequestContext.getEntityStream();
							String json = IOUtils.toString(is);
							SearchParser parser = new SearchParser(query, json);
							System.out.println(parser.getHQL());
							List<Map<String, Object>> result = getResultList(parser.getHQL(), parser.getParameters());
							
							ObjectMapper objectMapper = new ObjectMapper();
							objectMapper.configure(SerializationFeature.INDENT_OUTPUT, true);
							objectMapper.writeValue(stringEmp, result);
						} catch (Throwable e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
							return null;
						}
			        }
					return stringEmp.toString();
				}
			});
	}

	public void addRecordPath(Resource.Builder resourceBuilder, String path, final String query, final String idField) {
		final Resource.Builder childBuilder = resourceBuilder.addChildResource(path + "/{" + idField + "}");
		final ResourceMethod.Builder methodBuilder = childBuilder.addMethod("GET");
		methodBuilder.produces(MediaType.APPLICATION_JSON).handledBy(
			new Inflector<ContainerRequestContext, String>() {
	
				@Override
				public String apply(ContainerRequestContext containerRequestContext) {
					Map<String, List<String>> params = containerRequestContext.getUriInfo().getPathParameters();
					String id = params.get(idField).get(0);
					StringWriter stringEmp = new StringWriter();
					try {
						String clauseTerm = query.indexOf(" where ") == -1 ? " where " : " and ";
						Map<String, String> parameters = new HashMap<String, String>();
						parameters.put(idField, id);
						List<Map<String, Object>> result = getResultList(query + clauseTerm + idField + " = :" + idField, parameters);
						
						ObjectMapper objectMapper = new ObjectMapper();
						objectMapper.configure(SerializationFeature.INDENT_OUTPUT, true);
						if (result.size() > 0) {
							objectMapper.writeValue(stringEmp, result);
						}
					} catch (Throwable e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						return null;
					}
					return stringEmp.toString();
				}
			});
	}

	@SuppressWarnings({ "unchecked", "deprecation" })
	private List<Map<String, Object>> getResultList(String queryString, Map<String, String> parameters) throws IOException {
		Session session = HibernateFactory.getSessionFactory().openSession();
		Transaction tx = session.beginTransaction();

		Query<Map<String, Object>> query = (Query<Map<String, Object>>) session.createQuery(queryString);
		if (parameters != null) {
			for (Entry<String, String> param : parameters.entrySet()) {
				Type paramType = query.getParameterMetadata().getQueryParameter(param.getKey()).getType();
				Object typedValue = QueryTypes.convertString(paramType.getClass(), param.getValue());
				query.setParameter(param.getKey(), typedValue, paramType);
			}
		}
		System.out.println(query.getQueryString());
		List<Map<String, Object>> rows = query.getResultList();
		tx.commit();
		session.close();
		return rows;
	}
}
