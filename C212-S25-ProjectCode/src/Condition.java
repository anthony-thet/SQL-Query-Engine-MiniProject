import com.sun.jdi.Value;

import java.util.Map;

/**
 * A condition is of the form operand1 operator operand2, e.g. sid = s1
 */
public class Condition {
    private String operand1;
    private String operand2;
    private String operator;

    /**
     * constructor
     * @param operand1
     * @param operand2
     * @param operator
     */
    public Condition(String operand1, String operand2, String operator) {
        this.operand1 = operand1;
        this.operand2 = operand2;
        this.operator = operator;
        if (operand2.contains("'")) {
            this.operand2 = operand2.replace("'", "").trim();
        }
    }

    /**
     * Getters and setters
     */

    public String getOperand1() {
        return operand1;
    }

    public void setOperand1(String operand1) {
        this.operand1 = operand1;
    }

    public String getOperand2() {
        return operand2;
    }

    public void setOperand2(String operand2) {
        this.operand2 = operand2;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public boolean checkCondition(ITuple tuple, ISchema schema) throws InvalidQueryException {
        Integer index = null;
        String type = null;
        for (Map.Entry<Integer, String> entry: schema.getAttributes().entrySet()) {
            String[] parts = entry.getValue().split(":");
            String name = parts[0].trim();
            if (operand1.equalsIgnoreCase(name)) {
                type = parts[1].trim();
                index = entry.getKey();
                break;
            }
        }
        if (index == null) {
            throw new InvalidQueryException("Attribute not found in schema");
        }

        switch (type) {
            case "Integer":
                int operandInt = Integer.parseInt(operand2);
                return compare(tuple.getValue(index), operandInt);
            case "Double":
                double operandDouble = Double.parseDouble(operand2);
                return compare(tuple.getValue(index), operandDouble);
            case "String":
                return compare(tuple.getValue(index), operand2);
            default:
                throw new InvalidQueryException("Unsupported type: " + type);
        }
    }

    private <T extends Comparable<T>> boolean compare(T o1, T o2) throws InvalidQueryException {
        switch (operator) {
            case "=":
                return o1.equals(o2);
            case "!=":
                return !o1.equals(o2);
            case "<":
                return o1.compareTo(o2) < 0;
            case ">":
                return o1.compareTo(o2) > 0;
            case "<=":
                return o1.compareTo(o2) <= 0;
            case ">=":
                return o1.compareTo(o2) >= 0;
            default:
                throw new InvalidQueryException("Invalid operator: " + operator);
        }
    }
}
