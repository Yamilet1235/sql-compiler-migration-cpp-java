package com.sqlcompiler.domain.parser;

import java.util.ArrayList;
import java.util.List;

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
}
