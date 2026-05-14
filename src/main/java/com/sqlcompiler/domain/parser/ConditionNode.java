package com.sqlcompiler.domain.parser;

public class ConditionNode extends ASTNode {
    public static enum CondType { SIMPLE, AND, OR, NOT, PAREN }

    private CondType type;
    private String column;
    private String operator;
    private String value;
    private ConditionNode left;
    private ConditionNode right;
    private ConditionNode expr; // for NOT

    // Simple: column op value
    public ConditionNode(String column, String operator, String value) {
        this.type = CondType.SIMPLE;
        this.column = column;
        this.operator = operator;
        this.value = value;
    }

    // Compound: left AND/OR right
    public ConditionNode(CondType type, ConditionNode left, ConditionNode right) {
        this.type = type;
        this.left = left;
        this.right = right;
    }

    // NOT
    public ConditionNode(CondType type, ConditionNode expr) {
        this.type = type;
        this.expr = expr;
    }

    public CondType getCondType() { return type; }
    public String getColumn() { return column; }
    public String getOperator() { return operator; }
    public String getValue() { return value; }
    public ConditionNode getLeft() { return left; }
    public ConditionNode getRight() { return right; }
    public ConditionNode getExpr() { return expr; }

    @Override
    public void print(int indent) {
        String sp = getIndentation(indent);
        switch (type) {
            case SIMPLE:
                System.out.println(sp + column + " " + operator + " " + value);
                break;
            case AND:
                System.out.println(sp + "AND:");
                left.print(indent + 2);
                right.print(indent + 2);
                break;
            case OR:
                System.out.println(sp + "OR:");
                left.print(indent + 2);
                right.print(indent + 2);
                break;
            case NOT:
                System.out.println(sp + "NOT:");
                expr.print(indent + 2);
                break;
            case PAREN:
                expr.print(indent);
                break;
        }
    }
}