/*
 * Copyright 2021 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package io.netty.build.checkstyle;

import com.puppycrawl.tools.checkstyle.api.*;

import java.util.regex.Pattern;

public class StaticFinalBufferCheck extends AbstractCheck {

    private static final Pattern pattern = Pattern.compile(
            "(Unpooled\\s*\\.)?unreleasableBuffer\\(.*?\\)\\s*\\.asReadOnly\\(\\)",
            Pattern.MULTILINE);

    @Override
    public int[] getRequiredTokens() {
        return new int[]{TokenTypes.VARIABLE_DEF};
    }

    @Override
    public int[] getDefaultTokens() {
        return getRequiredTokens();
    }

    @Override
    public int[] getAcceptableTokens() {
        return getRequiredTokens();
    }

    @Override
    public void visitToken(DetailAST ast) {
        DetailAST modifiersAST = ast.findFirstToken(TokenTypes.MODIFIERS);
        boolean isStatic = modifiersAST.findFirstToken(TokenTypes.LITERAL_STATIC) != null;
        boolean isFinal = modifiersAST.findFirstToken(TokenTypes.FINAL) != null;
        FullIdent typeIdent = FullIdent.createFullIdentBelow(ast.findFirstToken(TokenTypes.TYPE));
        if (!isStatic || !isFinal || !typeIdent.getText().endsWith("Buf")) {
            return;
        }
        DetailAST assignAST = ast.findFirstToken(TokenTypes.ASSIGN);
        DetailAST semiAST = ast.findFirstToken(TokenTypes.SEMI);
        if (assignAST == null || semiAST == null) {
            log(ast.getLineNo(), "Missing assignment for static final buffer");
            return;
        }
        FileContents fc = getFileContents();
        StringBuilder sb = new StringBuilder();
        for (int i = assignAST.getLineNo(); i <= semiAST.getLineNo(); i++) {
            // getLineNo returns 1-based line number, getLine expects 0-based.
            sb.append(fc.getLine(i - 1));
        }
        if (!pattern.matcher(sb.toString()).find()) {
            log(ast.getLineNo(), "static final buffer assignment should match pattern " + pattern);
        }
    }
}
