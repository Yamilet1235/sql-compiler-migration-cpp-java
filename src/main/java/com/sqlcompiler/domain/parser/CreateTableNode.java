package com.sqlcompiler.domain.parser;

import java.util.HashMap;
import java.util.Map;

public class CreateTableNode extends ASTNode {

    public String table;
    public Map<String, String> columns = new HashMap<>();

    @Override
    public void print(int indent) {
        String ind = getIndentation(indent);

        System.out.println(ind + "CREATE TABLE");
        System.out.println(ind + "  Name: " + table);

        System.out.println(ind + "  Columns:");
        columns.forEach((col, type) ->
                System.out.println(ind + "    " + col + " : " + type)
        );
    }
}