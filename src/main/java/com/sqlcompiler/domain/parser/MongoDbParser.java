package com.sqlcompiler.domain.parser;

import java.util.List;
import com.sqlcompiler.domain.lexer.Token;

public class MongoDbParser extends Parser {

    public MongoDbParser(List<Token> tokens) {
        super(tokens);
    }

    @Override
    public SelectNode parse() {
        throw new RuntimeException("MongoDB no usa SQL. Usa sintaxis JSON como: db.coleccion.find({})");
    }
}  