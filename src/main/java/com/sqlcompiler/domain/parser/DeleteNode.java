package com.sqlcompiler.domain.parser;

public class DeleteNode extends ASTNode {

    public String table;
    public ConditionNode condition;

    @Override
    public void print(int indent) {
        String ind = getIndentation(indent);

        System.out.println(ind + "DELETE");
        System.out.println(ind + "  Table: " + table);

        if (condition != null) {
            System.out.println(ind + "  WHERE:");
            condition.print(indent + 4);
        }
    }
}