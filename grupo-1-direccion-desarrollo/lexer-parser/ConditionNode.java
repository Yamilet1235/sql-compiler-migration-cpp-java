package com.sqlcompiler.domain.parser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConditionNode extends ASTNode {
    
    public enum CondType { SIMPLE, AND, OR, NOT, PAREN }
    
    private CondType condType = CondType.SIMPLE;
    private OperandNode leftOperand;
    private String operator;
    private OperandNode rightOperand;
    
    private ConditionNode leftNode;
    private ConditionNode rightNode;
    private ConditionNode exprNode;

    public static class OperandNode {
        private final String value;
        public OperandNode(String value) { this.value = value; }
        public String getValue() { return value; }
    }

    public ConditionNode(String left, String operator, String right) {
        this.condType = CondType.SIMPLE;
        this.leftOperand = new OperandNode(left);
        this.operator = operator;
        this.rightOperand = new OperandNode(right);
    }

    
    public ConditionNode(CondType type, ConditionNode leftCond, ConditionNode rightCond) {
        this.condType = type;
        this.leftNode = leftCond;
        this.rightNode = rightCond;
    }

    public ConditionNode(CondType type, ConditionNode leftCond) {
        this.condType = type;
        this.leftNode = leftCond;
        this.exprNode = leftCond;
    }


    public CondType getCondType() { return condType; }
    public String getColumn() { return (leftOperand != null) ? leftOperand.getValue() : ""; }
    public String getValue() { return (rightOperand != null) ? rightOperand.getValue() : ""; }
    public ConditionNode getLeft() { return leftNode; }
    public ConditionNode getRight() { return rightNode; }
    public ConditionNode getExpr() { return exprNode; }

    
    public OperandNode getLeftOperand() { return leftOperand != null ? leftOperand : new OperandNode(""); }
    public OperandNode getRightOperand() { return rightOperand != null ? rightOperand : new OperandNode(""); }
    public String getOperator() { return operator != null ? operator : ""; }

    @Override
    public void print(int indent) {
        String ind = getIndentation(indent);
        if (condType == CondType.SIMPLE) {
            System.out.println(ind + "Condition: " + getColumn() + " " + getOperator() + " " + getValue());
        } else {
            System.out.println(ind + "Logical Op: " + condType);
        }
    }

    @Override
    public Map<String, Object> toVisualTree() {
        Map<String, Object> node = new HashMap<>();
        if (condType == CondType.SIMPLE) {
            String expr = getColumn() + " " + getOperator() + " " + getValue();
            node.put("name", expr.trim().isEmpty() ? "Condition" : expr);
            node.put("children", new ArrayList<>());
        } else if (condType == CondType.NOT || condType == CondType.PAREN) {
            node.put("name", condType.toString());
            List<Map<String, Object>> children = new ArrayList<>();
            if (exprNode != null) children.add(exprNode.toVisualTree());
            node.put("children", children);
        } else {
            node.put("name", condType.toString());
            List<Map<String, Object>> children = new ArrayList<>();
            if (leftNode != null) children.add(leftNode.toVisualTree());
            if (rightNode != null) children.add(rightNode.toVisualTree());
            node.put("children", children);
        }
        return node;
    }
}   