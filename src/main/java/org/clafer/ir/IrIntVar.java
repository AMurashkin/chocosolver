package org.clafer.ir;

import org.clafer.common.Check;

/**
 *
 * @author jimmy
 */
public class IrIntVar extends IrAbstractInt implements IrIntExpr, IrVar {

    private final String name;

    IrIntVar(String name, IrDomain domain) {
        super(domain);
        this.name = Check.notNull(name);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public <A, B> B accept(IrIntExprVisitor<A, B> visitor, A a) {
        return visitor.visit(this, a);
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj;
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public String toString() {
        // TODO: return only name
        return name + "{domain=" + getDomain() + "}";
    }
}
