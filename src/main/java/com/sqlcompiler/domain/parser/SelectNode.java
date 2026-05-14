package com.sqlcompiler.domain.parser;

import java.util.List;
import java.util.ArrayList;

public class SelectNode extends ASTNode {
    private boolean selectAll;
    private List<String> columns;
    private String tableName;
    private String tableAlias;
    private ConditionNode whereCondition;
    private List<String> orderByColumns;
    private List<Boolean> orderByAsc;
    private String limit;
    private String offset;
    private String top;
    private List<JoinInfo> joins;

    public SelectNode() {
        this.columns = new ArrayList<>();
        this.orderByColumns = new ArrayList<>();
        this.orderByAsc = new ArrayList<>();
        this.joins = new ArrayList<>();
        this.whereCondition = null;
        this.selectAll = false;
        this.tableAlias = null;
        this.limit = null;
        this.offset = null;
        this.top = null;
    }

    public boolean isSelectAll() { return selectAll; }
    public void setSelectAll(boolean v) { this.selectAll = v; }

    public List<String> getColumns() { return columns; }

    public String getTableName() { return tableName; }
    public void setTableName(String v) { this.tableName = v; }

    public String getTableAlias() { return tableAlias; }
    public void setTableAlias(String v) { this.tableAlias = v; }

    public ConditionNode getWhereCondition() { return whereCondition; }
    public void setWhereCondition(ConditionNode v) { this.whereCondition = v; }

    public List<String> getOrderByColumns() { return orderByColumns; }
    public List<Boolean> getOrderByAsc() { return orderByAsc; }

    public String getLimit() { return limit; }
    public void setLimit(String v) { this.limit = v; }

    public String getOffset() { return offset; }
    public void setOffset(String v) { this.offset = v; }

    public String getTop() { return top; }
    public void setTop(String v) { this.top = v; }

    public List<JoinInfo> getJoins() { return joins; }

    public static class JoinInfo {
        public String type;
        public String tableName;
        public String tableAlias;
        public ConditionNode onCondition;

        public JoinInfo(String type, String tableName, String tableAlias, ConditionNode onCondition) {
            this.type = type;
            this.tableName = tableName;
            this.tableAlias = tableAlias;
            this.onCondition = onCondition;
        }
    }

    @Override
    public void print(int indent) {
        String sp = getIndentation(indent);
        System.out.println(sp + "SELECT Query:");

        if (top != null) System.out.println(sp + "  TOP: " + top);

        System.out.print(sp + "  Columns: ");
        if (selectAll) {
            System.out.println("*");
        } else {
            System.out.println(String.join(", ", columns));
        }

        System.out.println(sp + "  FROM: " + tableName + (tableAlias != null ? " (alias: " + tableAlias + ")" : ""));

        for (JoinInfo j : joins) {
            System.out.println(sp + "  " + j.type + " JOIN: " + j.tableName + (j.tableAlias != null ? " (alias: " + j.tableAlias + ")" : ""));
            if (j.onCondition != null) {
                System.out.println(sp + "    ON:");
                j.onCondition.print(indent + 6);
            }
        }

        if (whereCondition != null) {
            System.out.println(sp + "  WHERE:");
            whereCondition.print(indent + 4);
        }

        if (!orderByColumns.isEmpty()) {
            System.out.print(sp + "  ORDER BY: ");
            for (int i = 0; i < orderByColumns.size(); i++) {
                System.out.print(orderByColumns.get(i) + (orderByAsc.get(i) ? " ASC" : " DESC"));
                if (i < orderByColumns.size() - 1) System.out.print(", ");
            }
            System.out.println();
        }

        if (limit != null) System.out.println(sp + "  LIMIT: " + limit);
        if (offset != null) System.out.println(sp + "  OFFSET: " + offset);
    }
}