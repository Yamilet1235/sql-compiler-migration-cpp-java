package com.sqlcompiler.domain.parser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class CreateTableNode extends ASTNode {
    public String table;
    // Restaurado el mapa original para el bucle (col, type) -> ...
    public Map<String, String> columns = new LinkedHashMap<>(); 

    @Override
    public void print(int indent) {
        String ind = getIndentation(indent);
        System.out.println(ind + "CREATE TABLE: " + table);
    }

    @Override
    public Map<String, Object> toVisualTree() {
        Map<String, Object> node = new HashMap<>();
        node.put("name", "CREATE TABLE");

        List<Map<String, Object>> children = new ArrayList<>();

        Map<String, Object> tableNode = new HashMap<>();
        tableNode.put("name", "Table: " + table);
        children.add(tableNode);

        if (!columns.isEmpty()) {
            Map<String, Object> colsParentNode = new HashMap<>();
            colsParentNode.put("name", "Columns Definitions");
            List<Map<String, Object>> colsChildren = new ArrayList<>();
            
            columns.forEach((col, type) -> {
                Map<String, Object> cNode = new HashMap<>();
                cNode.put("name", col + " (" + type + ")");
                colsChildren.add(cNode);
            });
            colsParentNode.put("children", colsChildren);
            children.add(colsParentNode);
        }

        node.put("children", children);
        return node;
    }
}