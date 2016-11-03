package org.portland.sqltorest;

import java.io.IOException;
import java.io.StringWriter;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.MediaType;

import org.glassfish.jersey.process.Inflector;
import org.glassfish.jersey.server.model.Resource;
import org.glassfish.jersey.server.model.ResourceMethod;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import org.hibernate.type.Type;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

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
						List result = getResultList(query, null);
						
						ObjectMapper objectMapper = new ObjectMapper();
						objectMapper.configure(SerializationFeature.INDENT_OUTPUT, true);
						objectMapper.writeValue(stringEmp, result);
					} catch (Throwable e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
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
						List result = getResultList(query + clauseTerm + idField + " = :" + idField, parameters);
						
						ObjectMapper objectMapper = new ObjectMapper();
						objectMapper.configure(SerializationFeature.INDENT_OUTPUT, true);
						objectMapper.writeValue(stringEmp, result.get(0));
					} catch (Throwable e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					return stringEmp.toString();
				}
			});
	}

	private List getResultList(String queryString, Map<String, String> parameters) throws IOException {
		Session session = HibernateFactory.getSessionFactory().openSession();
		Transaction tx = session.beginTransaction();

		Query<Map<String, Object>> query = (Query<Map<String, Object>>) session
				.createQuery(queryString);
		if (parameters != null) {
			for (Entry<String, String> param : parameters.entrySet()) {
				Type paramType = query.getParameterMetadata().getQueryParameter(param.getKey()).getType();
				Object typedValue = QueryTypes.convertString(paramType.getClass(), param.getValue());
				query.setParameter(param.getKey(), typedValue, paramType);
			}
		}
		List<Map<String, Object>> rows = new ArrayList<Map<String, Object>>();
		for (Iterator<Map<String, Object>> it = query.iterate(); it.hasNext();) {
			Map<String, Object> row = new HashMap<String, Object>();

			Set<Entry<String, Object>> set = ((Map<String, Object>) it.next())
					.entrySet();
			for (Entry<String, Object> entry : set) {
				row.put(entry.getKey(), entry.getValue());
			}
			rows.add(row);
			row = null;
		}
		tx.commit();
		session.close();
		return rows;
	}
}
