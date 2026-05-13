package com.sqlcompiler.domain.parser;
import java.util.List;
import com.sqlcompiler.domain.lexer.Token;

public class PostgresParser extends Parser {

    public PostgresParser(List<Token> tokens) {
        super(tokens);
        
    }

    @Override
    public SelectNode parse() {
        SelectNode node = super.parse();
        return node;
    }

    
   
}
