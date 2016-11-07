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

// TODO: Finish mapping all of the hibernate types to a conversion operation.
public enum QueryTypes {
    INTEGER(IntegerType.class, (String string) -> { return new Integer(string); }),
	BINARY(BinaryType.class, (String string) -> { return string.getBytes(); }),
    BIGINTEGER(BigIntegerType.class, (String string) -> { return new BigInteger(string); }),
	BOOLEAN(BooleanType.class, (String string) -> { return string.equals("true") ? true : false; }),
	NUMBOOLEAN(TrueFalseType.class, (String string) -> { return string.equals("1") ? true : false; }),
	YNBOOLEAN(TrueFalseType.class, (String string) -> { return string.equals("yes") ? true : false; }),
    TFBOOLEAN(TrueFalseType.class, (String string) -> { return string.equals("true") ? true : false; }),
    BYTE(ByteType.class, (String string) -> { return string.getBytes()[0]; }),
    CALENDAR(CalendarType.class, (String string) -> { DateFormat formatter = new SimpleDateFormat("MM/dd/yy");
    												Date date = null;
    												try { date = formatter.parse(string); }
    												catch (ParseException pe) {pe.printStackTrace();}
    												Calendar calendar = Calendar.getInstance();
    												calendar.setTime(date); 
    												return calendar; }),
    CHARACTER(CharacterType.class, (String string) -> { return string.charAt(0); }),
    DATE(DateType.class, (String string) -> { DateFormat formatter = new SimpleDateFormat("MM/dd/yy");
											Date date = null;
											try { date = formatter.parse(string); }
											catch (ParseException pe) {pe.printStackTrace();}
    										return date; }),
    LONG(LongType.class, (String string) -> { return new Long(string); }),
    SHORT(ShortType.class, (String string) -> { return new Short(string); }),
    STRING(StringType.class, (String string) -> { return string; }),
    TIMESTAMP(TimestampType.class, (String string) -> { return new Timestamp(new Long(string)); });
    
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
