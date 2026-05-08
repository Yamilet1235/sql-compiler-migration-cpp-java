package com.sqlcompiler;

import java.util.ArrayList;
import java.util.List;

public class SemanticAnalyzer {
    private final SymbolTable symbolTable;
    private List<String> errors;
    private List<String> warnings;

    public SemanticAnalyzer(SymbolTable symbolTable) {
        this.symbolTable = symbolTable;
        this.errors = new ArrayList<>();
        this.warnings = new ArrayList<>();
    }

    private Table validateTable(String tableName) {
        Table table = symbolTable.findTable(tableName);
        if (table == null) {
            errors.add("Tabla '" + tableName + "' no existe en el schema");
        }
        return table;
    }

    private void validateColumns(SelectNode selectNode, Table table) {
        if (table == null) return;

        if (selectNode.isSelectAll()) {
            return;
        }

        for (String colName : selectNode.getColumns()) {
            Column col = table.findColumn(colName);
            if (col == null) {
                errors.add("Columna '" + colName + "' no existe en la tabla '" + table.getName() + "'");
            }
        }
    }

    private DataType getExpressionType(ExpressionNode expr, Table table) {
        if (expr.getType() == ExpressionNode.ExprType.NUMBER) {
            return DataType.INT;
        } else if (expr.getType() == ExpressionNode.ExprType.STRING) {
            return DataType.VARCHAR;
        } else if (expr.getType() == ExpressionNode.ExprType.IDENTIFIER) {
            if (table == null) {
                return DataType.VARCHAR;
            }

            Column col = table.findColumn(expr.getValue());
            if (col == null) {
                errors.add("Columna '" + expr.getValue() + "' no existe en la tabla '" + table.getName() + "'");
                return DataType.VARCHAR;
            }

            return col.getType();
        }

        return DataType.VARCHAR;
    }

    private boolean areTypesCompatible(DataType left, DataType right, CompOperator op) {
        if (left == right) {
            return true;
        }

        if ((left == DataType.FLOAT && right == DataType.INT) ||
            (left == DataType.INT && right == DataType.FLOAT)) {
            return true;
        }

        return false;
    }

    private void validateCondition(ConditionNode condition, Table table) {
        if (condition == null || table == null) return;

        DataType leftType = getExpressionType(condition.getLeft(), table);
        DataType rightType = getExpressionType(condition.getRight(), table);

        if (!areTypesCompatible(leftType, rightType, condition.getOp())) {
            errors.add("Tipos incompatibles en comparacion: " +
                      leftType + " " +
                      CompOperator.toString(condition.getOp()) + " " +
                      rightType);
        }
    }

    public boolean analyze(SelectNode ast) {
        if (ast == null) {
            errors.add("AST vacio");
            return false;
        }

        errors.clear();
        warnings.clear();

        Table table = validateTable(ast.getTableName());

        validateColumns(ast, table);

        if (ast.getWhereCondition() != null) {
            validateCondition(ast.getWhereCondition(), table);
        }

        return errors.isEmpty();
    }

    public List<String> getErrors() {
        return errors;
    }

    public List<String> getWarnings() {
        return warnings;
    }

    public void printDiagnostics() {
        if (!errors.isEmpty()) {
            System.out.println();
            System.out.println("ERRORES SEMANTICOS:");
            for (String err : errors) {
                System.out.println("  - " + err);
            }
        }

        if (!warnings.isEmpty()) {
            System.out.println();
            System.out.println("ADVERTENCIAS:");
            for (String warn : warnings) {
                System.out.println("  - " + warn);
            }
        }

        if (errors.isEmpty() && warnings.isEmpty()) {
            System.out.println();
            System.out.println("Analisis semantico exitoso - No se encontraron errores");
        }
    }
}
