package com.sqlcompiler.domain.translator;

import com.sqlcompiler.domain.parser.ASTNode;

public interface DialectTranslator {
    String translate(ASTNode node);
}