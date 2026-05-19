package com.sqlcompiler.domain.parser;

import com.sqlcompiler.domain.translator.DialectTranslator;


public class SqlServerParser implements DialectTranslator {

    @Override
    public String translate(ASTNode node) {

        if (node instanceof SelectNode)
            return translateSelect((SelectNode) node);

        if (node instanceof InsertNode)
            return translateInsert((InsertNode) node);

        if (node instanceof UpdateNode)
            return translateUpdate((UpdateNode) node);

        if (node instanceof DeleteNode)
            return translateDelete((DeleteNode) node);

        if (node instanceof CreateTableNode)
            return translateCreate((CreateTableNode) node);

        throw new RuntimeException("Nodo no soportado por SQL Server");
    }

    private String translateSelect(SelectNode node) {
        StringBuilder sql = new StringBuilder("SELECT ");

        if (node.isSelectAll()) sql.append("*");
        else sql.append(String.join(", ", node.getColumns()));

        sql.append(" FROM ").append(node.getTableName());

        if (node.getWhereCondition() != null)
            sql.append(" WHERE ").append(translateCondition(node.getWhereCondition()));

        return sql.toString();
    }

    private String translateInsert(InsertNode node) {
        return "INSERT INTO " + node.table +
               " VALUES (" + String.join(", ", node.values) + ")";
    }

    private String translateUpdate(UpdateNode node) {
        StringBuilder sql = new StringBuilder("UPDATE " + node.table + " SET ");

        node.setColumns.forEach((col,val) ->
                sql.append(col).append("=").append(val).append(", ")
        );

        sql.delete(sql.length()-2, sql.length());

        if (node.condition != null)
            sql.append(" WHERE ").append(translateCondition(node.condition));

        return sql.toString();
    }

    private String translateDelete(DeleteNode node) {
        String sql = "DELETE FROM " + node.table;

        if (node.condition != null)
            sql += " WHERE " + translateCondition(node.condition);

        return sql;
    }

    private String translateCreate(CreateTableNode node) {
        StringBuilder sql = new StringBuilder("CREATE TABLE " + node.table + " (");

        node.columns.forEach((col,type) ->
                sql.append(col).append(" ").append(type).append(", ")
        );

        sql.delete(sql.length()-2, sql.length());
        sql.append(")");

        return sql.toString();
    }

       private String translateCondition(ConditionNode cond) {
        return cond.getLeft().getValue() + " " +
               cond.getOperator() + " " +
               cond.getRight().getValue();
    }
 
}