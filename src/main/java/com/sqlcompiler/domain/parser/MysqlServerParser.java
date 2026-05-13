package com.sqlcompiler.domain.parser;
import java.util.List;
import com.sqlcompiler.domain.lexer.Token;
public class MysqlServerParser extends Parser{

    public MysqlServerParser(List<Token> tokens) {
        super(tokens);
    }

    @Override
    public SelectNode parse() {
        SelectNode node = super.parse();
        return node;
    }

    
    
}
