package com.sqlcompiler.domain.parser;

import java.util.HashMap;
import java.util.Map;

public class UpdateNode extends ASTNode {

    public String table;
    public Map<String,String> setColumns = new HashMap<>();
    public ConditionNode condition;

    @Override
    public void print(int indent) {
        System.out.println(getIndentation(indent) + "UPDATE " + table);
        System.out.println(getIndentation(indent+2) + "SET: " + setColumns);

        if (condition != null)
            condition.print(indent + 2);
    }
}