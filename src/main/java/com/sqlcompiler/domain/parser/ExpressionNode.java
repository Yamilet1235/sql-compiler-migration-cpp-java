package com.sqlcompiler.domain.parser;

import java.util.ArrayList;
import java.util.HashMap;

import java.util.Map;

public class ExpressionNode extends ASTNode {
    public String value;

    @Override
    public void print(int indent) {
        String ind = getIndentation(indent);
        System.out.println(ind + "Expression: " + value);
    }

    @Override
    public Map<String, Object> toVisualTree() {
        Map<String, Object> node = new HashMap<>();
        node.put("name", value != null ? value : "Expression");
        node.put("children", new ArrayList<>());
        return node;
    }
}