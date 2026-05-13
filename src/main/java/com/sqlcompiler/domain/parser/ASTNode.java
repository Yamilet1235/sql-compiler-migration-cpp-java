package com.sqlcompiler.domain.parser;

public abstract class ASTNode {
    public abstract void print(int indent);

    protected String getIndentation(int indent) {
        return " ".repeat(indent);
    }
}
