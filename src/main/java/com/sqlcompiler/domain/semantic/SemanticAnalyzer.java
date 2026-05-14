package com.sqlcompiler.domain.semantic;

import java.util.ArrayList;
import java.util.List;
import com.sqlcompiler.domain.parser.*;
import com.sqlcompiler.domain.model.Table;

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
        if (ast == null) {
            errors.add("AST vacio");
            return false;
        }
        errors.clear();
        warnings.clear();

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
        Table table = symbolTable.findTable(select.getTableName());
        if (table == null) {
            errors.add("Tabla '" + select.getTableName() + "' no existe");
            return;
        }
        if (!select.isSelectAll()) {
            for (String col : select.getColumns()) {
                if (table.findColumn(col) == null) {
                    errors.add("Columna '" + col + "' no existe en tabla '" + table.getName() + "'");
                }
            }
        }
    }

    private void analyzeInsert(InsertNode insert) {
        if (symbolTable.findTable(insert.table) == null) {
            errors.add("Tabla '" + insert.table + "' no existe");
        }
    }

    private void analyzeUpdate(UpdateNode update) {
        if (symbolTable.findTable(update.table) == null) {
            errors.add("Tabla '" + update.table + "' no existe");
        }
    }

    private void analyzeDelete(DeleteNode delete) {
        if (symbolTable.findTable(delete.table) == null) {
            errors.add("Tabla '" + delete.table + "' no existe");
        }
    }

    private void analyzeCreate(CreateTableNode create) {
        // CREATE TABLE siempre es valido semanticamente
    }

    public List<String> getErrors() { return errors; }
    public List<String> getWarnings() { return warnings; }

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