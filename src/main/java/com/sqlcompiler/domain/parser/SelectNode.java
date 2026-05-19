package com.sqlcompiler.domain.parser;

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class SelectNode extends ASTNode {

    
    private boolean selectAll;
    private boolean distinct;
    private List<String> columns;

    private String tableName;
    private String tableAlias;

    private ConditionNode whereCondition;

    
    private List<String> orderByColumns;
    private List<Boolean> orderByAsc;

  
    private String limit;
    private String offset;
    private String top;

    private List<String> groupByColumns;
    private ConditionNode havingCondition;

    
    private List<JoinInfo> joins;

    public SelectNode() {
        columns = new ArrayList<>();
        orderByColumns = new ArrayList<>();
        orderByAsc = new ArrayList<>();
        joins = new ArrayList<>();
        groupByColumns = new ArrayList<>();
    }

    //GETTERS SETTERS 

    public boolean isSelectAll() { return selectAll; }
    public void setSelectAll(boolean v) { selectAll = v; }

    public boolean isDistinct() { return distinct; }
    public void setDistinct(boolean v) { distinct = v; }

    public List<String> getColumns() { return columns; }

    public String getTableName() { return tableName; }
    public void setTableName(String v) { tableName = v; }

    public String getTableAlias() { return tableAlias; }
    public void setTableAlias(String v) { tableAlias = v; }

    public ConditionNode getWhereCondition() { return whereCondition; }
    public void setWhereCondition(ConditionNode v) { whereCondition = v; }

    public List<String> getOrderByColumns() { return orderByColumns; }
    public List<Boolean> getOrderByAsc() { return orderByAsc; }

    public String getLimit() { return limit; }
    public void setLimit(String v) { limit = v; }

    public String getOffset() { return offset; }
    public void setOffset(String v) { offset = v; }

    public String getTop() { return top; }
    public void setTop(String v) { top = v; }

    public List<String> getGroupByColumns() { return groupByColumns; }
    public void addGroupByColumn(String col) { groupByColumns.add(col); }

    public ConditionNode getHavingCondition() { return havingCondition; }
    public void setHavingCondition(ConditionNode cond) { havingCondition = cond; }

    public List<JoinInfo> getJoins() { return joins; }

   
    public static class JoinInfo {
        private String type;
        private String tableName;
        private String tableAlias;
        private ConditionNode onCondition;

        public JoinInfo(String type, String tableName, String tableAlias, ConditionNode onCondition) {
            this.type = type;
            this.tableName = tableName;
            this.tableAlias = tableAlias;
            this.onCondition = onCondition;
        }

        public String getType() { return type; }
        public String getTableName() { return tableName; }
        public String getTableAlias() { return tableAlias; }
        public ConditionNode getOnCondition() { return onCondition; }
    }

    @Override
    public void print(int indent) {
        String sp = getIndentation(indent);
        System.out.println(sp + "SELECT Query:");

        if (distinct) System.out.println(sp + "  DISTINCT");
        if (top != null) System.out.println(sp + "  TOP: " + top);

        System.out.print(sp + "  Columns: ");
        System.out.println(selectAll ? "*" : String.join(", ", columns));

        System.out.println(sp + "  FROM: " + tableName);

        for (JoinInfo j : joins) {
            System.out.println(sp + "  " + j.type + " JOIN: " + j.tableName);
        }

        if (whereCondition != null) {
            System.out.println(sp + "  WHERE:");
            whereCondition.print(indent + 4);
        }

        if (!groupByColumns.isEmpty())
            System.out.println(sp + "  GROUP BY: " + groupByColumns);

        if (havingCondition != null) {
            System.out.println(sp + "  HAVING:");
            havingCondition.print(indent + 4);
        }

        if (!orderByColumns.isEmpty())
            System.out.println(sp + "  ORDER BY: " + orderByColumns);

        if (limit != null) System.out.println(sp + "  LIMIT: " + limit);
        if (offset != null) System.out.println(sp + "  OFFSET: " + offset);
    }

   
    @Override
    public Map<String, Object> toVisualTree() {

        Map<String,Object> root = new HashMap<>();
        root.put("name","SelectStatement");
        List<Map<String,Object>> children = new ArrayList<>();

       
        Map<String,Object> proj = new HashMap<>();
        proj.put("name", "ProjectionList");
        List<Map<String,Object>> projChildren = new ArrayList<>();
        if (selectAll) {
            projChildren.add(Map.of("name", "*"));
        } else {
            for (String col : columns) {
                projChildren.add(Map.of("name", col));
            }
        }
        proj.put("children", projChildren);
        children.add(proj);

     
        Map<String,Object> fromClause = new HashMap<>();
        fromClause.put("name", "FromClause");
        List<Map<String,Object>> fromChildren = new ArrayList<>();

        String leftName = tableAlias != null ? tableName + " " + tableAlias : tableName;
        fromChildren.add(Map.of("name", leftName));

        for (JoinInfo j : joins) {
            Map<String,Object> joinExpr = new HashMap<>();
            joinExpr.put("name", "JoinExpression: " + j.type + " JOIN");
            List<Map<String,Object>> joinChildren = new ArrayList<>();
            String rightName = j.tableAlias != null ? j.tableName + " " + j.tableAlias : j.tableName;
            joinChildren.add(Map.of("name", rightName));
            if (j.onCondition != null) {
                joinChildren.add(j.onCondition.toVisualTree());
            }
            joinExpr.put("children", joinChildren);
            fromChildren.add(joinExpr);
        }

        fromClause.put("children", fromChildren);
        children.add(fromClause);

       
        if (whereCondition != null) {
            children.add(whereCondition.toVisualTree());
        }

        if (!groupByColumns.isEmpty())
            children.add(Map.of("name", "GROUP BY: " + String.join(", ", groupByColumns)));
        if (havingCondition != null)
            children.add(havingCondition.toVisualTree());
        if (!orderByColumns.isEmpty())
            children.add(Map.of("name", "ORDER BY: " + String.join(", ", orderByColumns)));
        if (limit != null)
            children.add(Map.of("name", "LIMIT: " + limit));
        if (offset != null)
            children.add(Map.of("name", "OFFSET: " + offset));

        root.put("children", children);
        return root;
    }
}