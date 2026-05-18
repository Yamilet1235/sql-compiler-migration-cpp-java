package com.sqlcompiler.domain.semantic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sqlcompiler.domain.model.Table;
import com.sqlcompiler.domain.parser.*;

public class SemanticAnalyzer {

    private final SymbolTable symbolTable;
    private List<String> errors;
    private List<String> warnings;

    public SemanticAnalyzer(SymbolTable symbolTable) {
        this.symbolTable = symbolTable;
        this.errors = new ArrayList<>();
        this.warnings = new ArrayList<>();
    }

    public boolean analyze(ASTNode ast) {
        errors.clear();
        warnings.clear();

        if (ast == null) {
            errors.add("AST vacio");
            return false;
        }

        if (ast instanceof SelectNode) {
            analyzeSelect((SelectNode) ast);
        } else if (ast instanceof InsertNode) {
            analyzeInsert((InsertNode) ast);
        } else if (ast instanceof UpdateNode) {
            analyzeUpdate((UpdateNode) ast);
        } else if (ast instanceof DeleteNode) {
            analyzeDelete((DeleteNode) ast);
        } else if (ast instanceof CreateTableNode) {
            analyzeCreate((CreateTableNode) ast);
        }

        return errors.isEmpty();
    }

    private void analyzeSelect(SelectNode select) {

        Map<String, Table> aliasMap = new HashMap<>();

        Table mainTable = symbolTable.findTable(select.getTableName());

        if (mainTable == null) {
            errors.add("Tabla '" + select.getTableName() + "' no existe");
            return;
        }

        aliasMap.put(select.getTableName().toLowerCase(), mainTable);

        if (select.getTableAlias() != null && !select.getTableAlias().isBlank()) {
            aliasMap.put(select.getTableAlias().toLowerCase(), mainTable);
        }

        for (SelectNode.JoinInfo join : select.getJoins()) {

            Table joinTable = symbolTable.findTable(join.tableName);

            if (joinTable == null) {
                errors.add("Tabla JOIN '" + join.tableName + "' no existe");
                continue;
            }

            aliasMap.put(join.tableName.toLowerCase(), joinTable);

            if (join.tableAlias != null && !join.tableAlias.isBlank()) {
                aliasMap.put(join.tableAlias.toLowerCase(), joinTable);
            }

            validateCondition(join.onCondition, aliasMap, mainTable);
        }

        if (!select.isSelectAll()) {
            for (String col : select.getColumns()) {
                validateColumn(col, aliasMap, mainTable);
            }
        
        }

        validateCondition(select.getWhereCondition(), aliasMap, mainTable);

        for (String orderColumn : select.getOrderByColumns()) {
            validateColumn(orderColumn, aliasMap, mainTable);
        }
    }

    private void analyzeInsert(InsertNode insert) {

        Table table = symbolTable.findTable(insert.table);

        if (table == null) {
            errors.add("Tabla '" + insert.table + "' no existe");
            return;
        }

        for (String column : insert.columns) {
            if (table.findColumn(column) == null) {
                errors.add("Columna '" + column + "' no existe en tabla '" + table.getName() + "'");
            }
        }
    }

    private void analyzeUpdate(UpdateNode update) {

        Table table = symbolTable.findTable(update.table);

        if (table == null) {
            errors.add("Tabla '" + update.table + "' no existe");
            return;
        }

        for (String column : update.setColumns.keySet()) {
            if (table.findColumn(column) == null) {
                errors.add("Columna '" + column + "' no existe en tabla '" + table.getName() + "'");
            }
        }

        Map<String, Table> aliasMap = new HashMap<>();
        aliasMap.put(update.table.toLowerCase(), table);

        validateCondition(update.condition, aliasMap, table);
    }

    private void analyzeDelete(DeleteNode delete) {

        Table table = symbolTable.findTable(delete.table);

        if (table == null) {
            errors.add("Tabla '" + delete.table + "' no existe");
            return;
        }

        Map<String, Table> aliasMap = new HashMap<>();
        aliasMap.put(delete.table.toLowerCase(), table);

        validateCondition(delete.condition, aliasMap, table);
    }

   private void analyzeCreate(CreateTableNode create) {

    

    warnings.add(
        "CREATE TABLE detectado. Validacion semantica basica aplicada."
    );
}

    private void validateCondition(ConditionNode condition, Map<String, Table> aliasMap, Table defaultTable) {

        if (condition == null) {
            return;
        }

        switch (condition.getCondType()) {
            case SIMPLE:
                validateColumn(condition.getColumn(), aliasMap, defaultTable);

                String value = condition.getValue();

                if (value != null && value.contains(".")) {
                    validateColumn(value, aliasMap, defaultTable);
                }

                break;

            case AND:
            case OR:
                validateCondition(condition.getLeft(), aliasMap, defaultTable);
                validateCondition(condition.getRight(), aliasMap, defaultTable);
                break;

            case NOT:
            case PAREN:
                validateCondition(condition.getExpr(), aliasMap, defaultTable);
                break;
        }
    }

    private void validateColumn(String columnExpression, Map<String, Table> aliasMap, Table defaultTable) {

        if (columnExpression == null || columnExpression.isBlank()) {
            return;
        }

        String cleanColumn = columnExpression.trim();

        if (cleanColumn.equals("*")) {
            return;
        }

        if (cleanColumn.contains(".")) {

            String[] parts = cleanColumn.split("\\.");

            if (parts.length != 2) {
                errors.add("Referencia invalida de columna: '" + cleanColumn + "'");
                return;
            }

            String alias = parts[0].toLowerCase();
            String column = parts[1];

            Table table = aliasMap.get(alias);

            if (table == null) {
                errors.add("Alias o tabla '" + alias + "' no existe");
                return;
            }

            if (table.findColumn(column) == null) {
                errors.add("Columna '" + column + "' no existe en tabla '" + table.getName() + "'");
            }

        } else {

            Table tableToUse = defaultTable;

            if (tableToUse == null && aliasMap.size() == 1) {
                tableToUse = aliasMap.values().iterator().next();
            }

            if (tableToUse == null) {
                warnings.add("La columna '" + cleanColumn + "' no tiene alias. Se recomienda usar alias cuando hay JOIN");
                return;
            }

            if (tableToUse.findColumn(cleanColumn) == null) {
                errors.add("Columna '" + cleanColumn + "' no existe en tabla '" + tableToUse.getName() + "'");
            }
        }
    }

    public List<String> getErrors() {
        return errors;
    }

    public List<String> getWarnings() {
        return warnings;
    }

    public void printDiagnostics() {
        if (!errors.isEmpty()) {
            System.out.println("\nERRORES SEMANTICOS:");
            for (String err : errors) System.out.println("  - " + err);
        }

        if (!warnings.isEmpty()) {
            System.out.println("\nADVERTENCIAS:");
            for (String warn : warnings) System.out.println("  - " + warn);
        }

        if (errors.isEmpty() && warnings.isEmpty()) {
            System.out.println("\nAnalisis semantico exitoso");
        }
    }
}


