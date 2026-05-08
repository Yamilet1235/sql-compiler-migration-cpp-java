package com.sqlcompiler;

public class ExpressionNode extends ASTNode {
    public enum ExprType {
        IDENTIFIER,
        NUMBER,
        STRING
    }

    private ExprType type;
    private String value;

    public ExpressionNode(ExprType type, String value) {
        this.type = type;
        this.value = value;
    }

    public ExprType getType() {
        return type;
    }

    public String getValue() {
        return value;
    }

    @Override
    public void print(int indent) {
        String spaces = getIndentation(indent);
        if (type == ExprType.IDENTIFIER) {
            System.out.println(spaces + "Identifier: " + value);
        } else if (type == ExprType.NUMBER) {
            System.out.println(spaces + "Number: " + value);
        } else {
            System.out.println(spaces + "String: '" + value + "'");
        }
    }
}
