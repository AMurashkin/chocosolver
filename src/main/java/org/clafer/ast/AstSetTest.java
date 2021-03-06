package org.clafer.ast;

import org.clafer.common.Check;

/**
 * 
 * @author jimmy
 */
public class AstSetTest implements AstBoolExpr {

    private final AstSetExpr left;
    private final Op op;
    private final AstSetExpr right;

    AstSetTest(AstSetExpr left, Op op, AstSetExpr right) {
        this.left = Check.notNull(left);
        this.op = Check.notNull(op);
        this.right = Check.notNull(right);
    }

    public Op getOp() {
        return op;
    }

    public AstSetExpr getLeft() {
        return left;
    }

    public AstSetExpr getRight() {
        return right;
    }

    @Override
    public <A, B> B accept(AstExprVisitor<A, B> visitor, A a) {
        return visitor.visit(this, a);
    }

    @Override
    public String toString() {
        return left + " " + op.getSyntax() + " " + right;
    }

    public static enum Op {

        Equal("="),
        NotEqual("!=");
        private final String syntax;

        private Op(String syntax) {
            this.syntax = syntax;
        }

        public String getSyntax() {
            return syntax;
        }
    }
}
