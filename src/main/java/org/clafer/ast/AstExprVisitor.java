package org.clafer.ast;

/**
 * Dynamic dispatch for AST expressions.
 *
 * @param <A> the parameter type
 * @param <B> the return type
 * @author jimmy
 */
public interface AstExprVisitor<A, B> {

    public B visit(AstThis ast, A a);

    public B visit(AstGlobal ast, A a);

    public B visit(AstConstant ast, A a);

    public B visit(AstJoin ast, A a);

    public B visit(AstJoinParent ast, A a);

    public B visit(AstJoinRef ast, A a);

    public B visit(AstNot ast, A a);

    public B visit(AstMinus ast, A a);

    public B visit(AstCard ast, A a);

    public B visit(AstSetTest ast, A a);

    public B visit(AstCompare ast, A a);

    public B visit(AstArithm ast, A a);

    public B visit(AstBoolArithm ast, A a);

    public B visit(AstSetArithm ast, A a);

    public B visit(AstMembership ast, A a);

    public B visit(AstTernary ast, A a);

    public B visit(AstIfThenElse ast, A a);

    public B visit(AstUpcast ast, A a);

    public B visit(AstLocal ast, A a);

    public B visit(AstQuantify ast, A a);
}
