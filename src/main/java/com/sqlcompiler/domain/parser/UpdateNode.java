package com.sqlcompiler.domain.parser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class UpdateNode extends ASTNode {
    public String table;
    // Restaurado el mapa original que tus traductores recorren
    public Map<String, String> setColumns = new LinkedHashMap<>(); 
    public ConditionNode condition;

    @Override
    public void print(int indent) {
        String ind = getIndentation(indent);
        System.out.println(ind + "UPDATE: " + table);
    }

    @Override
    public Map<String, Object> toVisualTree() {
        Map<String, Object> node = new HashMap<>();
        node.put("name", "UPDATE");

        List<Map<String, Object>> children = new ArrayList<>();

        Map<String, Object> tableNode = new HashMap<>();
        tableNode.put("name", "Table: " + table);
        children.add(tableNode);

        if (!setColumns.isEmpty()) {
            Map<String, Object> setNode = new HashMap<>();
            setNode.put("name", "SET Assignments");
            List<Map<String, Object>> setChildren = new ArrayList<>();
            
            setColumns.forEach((col, val) -> {
                Map<String, Object> assignNode = new HashMap<>();
                assignNode.put("name", col + " = " + val);
                setChildren.add(assignNode);
            });
            setNode.put("children", setChildren);
            children.add(setNode);
        }

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