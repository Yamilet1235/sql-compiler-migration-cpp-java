package com.sqlcompiler.domain.parser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public abstract class ASTNode {
    public abstract void print(int indent);

   
    public Map<String, Object> toVisualTree() {
        Map<String, Object> fallbackNode = new HashMap<>();
       
        fallbackNode.put("name", this.getClass().getSimpleName());
        fallbackNode.put("children", new ArrayList<>());
        return fallbackNode;
    }

    protected String getIndentation(int indent) {
        return " ".repeat(indent);
    }
}