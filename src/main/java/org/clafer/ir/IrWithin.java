package org.clafer.ir;

import org.clafer.common.Check;

/**
 *
 * @author jimmy
 */
public class IrWithin extends IrAbstractBool implements IrBoolExpr {

    private final IrIntExpr value;
    private final IrDomain range;

    IrWithin(IrIntExpr value, IrDomain range, IrBoolDomain domain) {
        super(domain);
        this.value = Check.notNull(value);
        this.range = Check.notNull(range);

        if (range.isEmpty()) {
            throw new IllegalArgumentException();
        }
    }

    public IrIntExpr getValue() {
        return value;
    }

    public IrDomain getRange() {
        return range;
    }

    @Override
    public IrBoolExpr negate() {
        return new IrNotWithin(value, range, getDomain().invert());
    }

    @Override
    public boolean isNegative() {
        return false;
    }

    @Override
    public <A, B> B accept(IrBoolExprVisitor<A, B> visitor, A a) {
        return visitor.visit(this, a);
    }

    @Override
    public <A, B> B accept(IrIntExprVisitor<A, B> visitor, A a) {
        return visitor.visit(this, a);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof IrWithin) {
            IrWithin other = (IrWithin) obj;
            return value.equals(other.value) && range.equals(other.range) && super.equals(other);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return value.hashCode() ^ range.hashCode();
    }

    @Override
    public String toString() {
        return value + " ∈ " + range;
    }
}
