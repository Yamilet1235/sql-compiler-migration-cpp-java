package com.sqlcompiler;

import java.util.List;
import java.util.ArrayList;

public class SelectNode extends ASTNode {
    private List<String> columns;
    private boolean selectAll;
    private String tableName;
    private ConditionNode whereCondition;

    public SelectNode() {
        this.columns = new ArrayList<>();
        this.selectAll = false;
        this.whereCondition = null;
    }

    public List<String> getColumns() {
        return columns;
    }

    public void setColumns(List<String> columns) {
        this.columns = columns;
    }

    public boolean isSelectAll() {
        return selectAll;
    }

    public void setSelectAll(boolean selectAll) {
        this.selectAll = selectAll;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public ConditionNode getWhereCondition() {
        return whereCondition;
    }

    public void setWhereCondition(ConditionNode whereCondition) {
        this.whereCondition = whereCondition;
    }

    @Override
    public void print(int indent) {
        String spaces = getIndentation(indent);
        System.out.println(spaces + "SELECT Query:");

        System.out.print(spaces + "  Columns: ");
        if (selectAll) {
            System.out.println("*");
        } else {
            for (int i = 0; i < columns.size(); i++) {
                System.out.print(columns.get(i));
                if (i < columns.size() - 1) System.out.print(", ");
            }
            System.out.println();
        }

        System.out.println(spaces + "  FROM: " + tableName);

        if (whereCondition != null) {
            System.out.println(spaces + "  WHERE:");
            whereCondition.print(indent + 4);
        }
    }
}
