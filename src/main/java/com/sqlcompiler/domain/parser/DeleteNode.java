package com.sqlcompiler.domain.parser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DeleteNode extends ASTNode {
    public String table;
    public ConditionNode condition; // Tipo de dato original restaurado

    @Override
    public void print(int indent) {
        String ind = getIndentation(indent);
        System.out.println(ind + "DELETE FROM " + table);
    }

    @Override
    public Map<String, Object> toVisualTree() {
        Map<String, Object> node = new HashMap<>();
        node.put("name", "DELETE");

        List<Map<String, Object>> children = new ArrayList<>();

        Map<String, Object> tableNode = new HashMap<>();
        tableNode.put("name", "Table: " + table);
        children.add(tableNode);

        if (condition != null) {
            Map<String, Object> whereNode = new HashMap<>();
            whereNode.put("name", "WHERE");
            List<Map<String, Object>> whereChildren = new ArrayList<>();
            whereChildren.add(condition.toVisualTree());
            whereNode.put("children", whereChildren);
            children.add(whereNode);
        }

        node.put("children", children);
        return node;
    }
}