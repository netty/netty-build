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
        return this.getRequiredTokens();
    }

    @Override
    public int[] getAcceptableTokens() {
        return this.getRequiredTokens();
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
