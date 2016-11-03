package org.portland.sqltorest;

import java.math.BigInteger;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.function.Function;

import org.hibernate.type.*;

public enum QueryTypes {
    INTEGER(IntegerType.class, (String string) -> { return new Integer(string); }),
	BINARY(byte[].class, (String string) -> { return string.getBytes(); }),
    BIGINTEGER(BigInteger.class, (String string) -> { return new BigInteger(string); }),
    BOOLEAN(Boolean.class, (String string) -> { return string.equals("true") ? true : false; }),
    BYTE(Byte.class, (String string) -> { return string.getBytes()[0]; }),
    CALENDAR(Calendar.class, (String string) -> { DateFormat formatter = new SimpleDateFormat("MM/dd/yy");
    												Date date = null;
    												try { date = formatter.parse(string); }
    												catch (ParseException pe) {pe.printStackTrace();}
    												Calendar calendar = Calendar.getInstance();
    												calendar.setTime(date); 
    												return calendar; }),
    CHARACTER(Character.class, (String string) -> { return string.charAt(0); }),
    DATE(Date.class, (String string) -> { DateFormat formatter = new SimpleDateFormat("MM/dd/yy");
											Date date = null;
											try { date = formatter.parse(string); }
											catch (ParseException pe) {pe.printStackTrace();}
    										return date; }),
    LONG(Long.class, (String string) -> { return new Long(string); }),
    SHORT(Short.class, (String string) -> { return new Short(string); }),
    STRING(String.class, (String string) -> { return string; }),
    TIMESTAMP(Timestamp.class, (String string) -> { return new Timestamp(new Long(string)); });
    
	private final Class<?> typeClass;
	private final Function<String, Object> function;
	
	private QueryTypes(Class<?> typeClass, Function<String, Object> function) {
		this.typeClass = typeClass;
		this.function = function;
	}
	
	public Object convert (String value) {
		return this.function.apply(value);
	}
	
	public static Object convertString (Class<?> typeClass, String value) {
		Object typedValue = null;
		for (QueryTypes type : QueryTypes.values()) {
			if (type.typeClass.equals(typeClass)) {
				typedValue = type.function.apply(value);
			}
		}
		return typedValue;
	}
}
