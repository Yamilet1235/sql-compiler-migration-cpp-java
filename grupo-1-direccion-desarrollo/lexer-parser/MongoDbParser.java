package com.sqlcompiler.domain.parser;

import com.sqlcompiler.domain.translator.DialectTranslator;

public class MongoDbParser implements DialectTranslator {

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

        throw new RuntimeException("Nodo no soportado por MongoDB");
    }

    private String translateSelect(SelectNode node) {
        String collection = node.getTableName();

        if (node.getWhereCondition() == null)
            return "db." + collection + ".find({})";

        return "db." + collection + ".find(" +
               translateCondition(node.getWhereCondition()) + ")";
    }

    private String translateInsert(InsertNode node) {
        return "db." + node.table + ".insert({ values: [" +
               String.join(", ", node.values) + "] })";
    }

    private String translateUpdate(UpdateNode node) {
        return "db." + node.table + ".updateMany({}, { $set: {...} })";
    }

    private String translateDelete(DeleteNode node) {
        return "db." + node.table + ".deleteMany({})";
    }

      private String translateCondition(ConditionNode cond) {
        String field = cond.getLeft().getValue();
        String value = cond.getRight().getValue();

        switch (cond.getOperator()) {
            case "=": return "{ " + field + ": " + value + " }";
            case ">": return "{ " + field + ": { $gt: " + value + " } }";
            case "<": return "{ " + field + ": { $lt: " + value + " } }";
            case ">=": return "{ " + field + ": { $gte: " + value + " } }";
            case "<=": return "{ " + field + ": { $lte: " + value + " } }";
            case "!=": return "{ " + field + ": { $ne: " + value + " } }";
        }
        return "{}";
     }}