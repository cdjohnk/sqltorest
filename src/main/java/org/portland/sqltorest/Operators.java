package org.portland.sqltorest;

public enum Operators {
    EQUALS("equals","= ?", true),
    GT("gt","> ?", true),
    GTE("gte",">= ?", true),
    LT("lte","< ?", true),
    LTE("lte","<= ?", true),
    EMPTY("empty", "IS EMPTY", false),
    NOTEMPTY("notempty", "IS NOT EMPTY", false),
    LIKE("like","LIKE ?", true)
    ;

    private final String text;
    private final String hqlOperator;
    private final boolean requiresParameter;

    private Operators(final String text, final String hqlOperator, final boolean requiresParameter) {
        this.text = text;
        this.hqlOperator = hqlOperator;
        this.requiresParameter = requiresParameter;
    }

    @Override
    public String toString() {
        return text;
    }
    
    public boolean equals(String op) {
    	return text.equals(op);
    }
    
    public String getHQLOperator() {
    	return hqlOperator;
    }

    public boolean getRequiresParameter() {
    	return requiresParameter;
    }
    
    public static Operators getValueFor(String op) {
    	Operators value = null;
    	for (Operators operator: Operators.values()) {
    		if (operator.equals(op)) {
    			value = operator;
    			break;
    		}
    	}
    	return value;
    }
}
