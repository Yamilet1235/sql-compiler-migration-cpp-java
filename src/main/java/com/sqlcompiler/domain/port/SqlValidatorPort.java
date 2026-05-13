package com.sqlcompiler.domain.port;

import com.sqlcompiler.domain.parser.SelectNode;

public interface SqlValidatorPort {
    boolean validate(SelectNode ast);
}
