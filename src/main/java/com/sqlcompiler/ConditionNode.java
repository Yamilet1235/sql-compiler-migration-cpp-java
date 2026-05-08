package com.sqlcompiler;

public class ConditionNode extends ASTNode {
    private ExpressionNode left;
    private CompOperator op;
    private ExpressionNode right;

    public ConditionNode(ExpressionNode left, CompOperator op, ExpressionNode right) {
        this.left = left;
        this.op = op;
        this.right = right;
    }

    public ExpressionNode getLeft() {
        return left;
    }

    public CompOperator getOp() {
        return op;
    }

    public ExpressionNode getRight() {
        return right;
    }

    @Override
    public void print(int indent) {
        String spaces = getIndentation(indent);
        System.out.println(spaces + "Condition:");
        System.out.println(spaces + "  Left:");
        left.print(indent + 4);
        System.out.println(spaces + "  Operator: " + CompOperator.toString(op));
        System.out.println(spaces + "  Right:");
        right.print(indent + 4);
    }
}
