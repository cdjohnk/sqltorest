package org.portland.sqltorest;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class SearchParser {
	private String tables = null;
	private JsonNode search = null;
	private int pageSize = 25;
	private int pageStart = 1;
	private Map parameters = new HashMap();
	
	public SearchParser(String tables, String jsonSearch) throws JsonParseException, JsonMappingException, IOException {
		ObjectMapper mapper = new ObjectMapper();

		this.tables = tables;
		this.search = mapper.readTree(jsonSearch);
		
	}
	
	public String getHQL() {
		String hql = "";
		
		if (search.has("fields")) hql += parseFields(search.get("fields"));
		
		hql += tables;
		
		if (search.has("terms")) hql += parseTerms(search.get("terms"), true);
		if (search.has("orderby")) hql += parseOrderBy(search.get("orderby"));
		
		return hql;
	}
	
	public Map getParameters() {
		return parameters;
	}
	
	public int getPageSize() {
		int pageSize = this.pageSize;
		if (search.has("pagesize")) {
			pageSize = search.get("pagesize").asInt();
		}
		return pageSize;
	}
	
	public int getPageStart() {
		int pageStart = this.pageStart;
		if (search.has("pagestart")) {
			pageStart = search.get("pagestart").asInt();
		}
		return pageStart;
	}
	
	private String parseFields(JsonNode fields) {
		String select = "";
		if (fields.size() > 0) {
			select += "SELECT ";
			for (Iterator i = fields.iterator(); i.hasNext();) {
				select += ((JsonNode)i.next()).asText() + ", ";
			}
			select = select.substring(0, select.length()-2) + "\r\n";
		}
		return select;
	}
	
	private String parseTerms(JsonNode terms, boolean top) {
		String where = "";
		if (terms.size() > 0) {
			if(top) where += "\r\nWHERE\r\n";
			for (Iterator i = terms.iterator(); i.hasNext();) {
				JsonNode term = (JsonNode)i.next();
				if (term.has("terms")) {
					where += "(" + parseTerms(term.get("terms"), false) + ") ";
				}
				else {
					String field = term.get("field").asText();
					String jsonOp = term.get("operator").asText();
					String op = Operators.getValueFor(jsonOp).getHQLOperator();
					String id = term.get("id").asText();
					String param = getParamName(field, id);
					parameters.put(param.substring(1), term.get("value").asText());
					where += field + " " + op.replaceAll("\\?", param);
				}
				String linkage = i.hasNext() ? " " + term.get("linkage").asText() + "\r\n" : "";
				where += linkage;
			}
		}
		return where;
	}
	
	private String parseOrderBy(JsonNode orderBy) {
		String order = "";
		if (orderBy.size() > 0) {
			order += "\r\nORDER BY ";
			for (Iterator i = orderBy.iterator(); i.hasNext();) {
				JsonNode orderTerm = (JsonNode)i.next();
				order += orderTerm.get("field").asText() + " " + orderTerm.get("direction").asText() + ", ";
			}
			order = order.substring(0, order.length()-2) + "\r\n";
		}
		return order;
	}
	
	private String getParamName(String field, String id) {
		int dotPos = field.contains(".") ? field.indexOf(".")+1 : 0;
		field = field.substring(dotPos);
		return ":" + field + id;
	}
}
