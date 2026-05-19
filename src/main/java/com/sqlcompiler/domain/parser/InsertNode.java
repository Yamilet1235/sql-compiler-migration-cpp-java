package com.sqlcompiler.domain.parser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InsertNode extends ASTNode {
    public String table;
    public List<String> columns = new ArrayList<>();
    public List<String> values = new ArrayList<>();

    @Override
    public void print(int indent) {
        String ind = getIndentation(indent);
        System.out.println(ind + "INSERT");
        System.out.println(ind + "  Table: " + table);
        if (!columns.isEmpty()) {
            System.out.println(ind + "  Columns: " + columns);
        }
        System.out.println(ind + "  Values:");
        for (String v : values) {
            System.out.println(ind + "    - " + v);
        }
    }

    @Override
    public Map<String, Object> toVisualTree() {
        Map<String, Object> node = new HashMap<>();
        node.put("name", "INSERT");

        List<Map<String, Object>> children = new ArrayList<>();

        Map<String, Object> tableNode = new HashMap<>();
        tableNode.put("name", "Table: " + table);
        children.add(tableNode);

        if (!columns.isEmpty()) {
            Map<String, Object> colsNode = new HashMap<>();
            colsNode.put("name", "Columns: " + String.join(", ", columns));
            children.add(colsNode);
        }

        if (!values.isEmpty()) {
            Map<String, Object> valuesParentNode = new HashMap<>();
            valuesParentNode.put("name", "Values");
            
            List<Map<String, Object>> valuesChildren = new ArrayList<>();
            for (String val : values) {
                Map<String, Object> vNode = new HashMap<>();
                vNode.put("name", "Value: " + val);
                valuesChildren.add(vNode);
            }
            valuesParentNode.put("children", valuesChildren);
            children.add(valuesParentNode);
        }

        node.put("children", children);
        return node;
    }
}