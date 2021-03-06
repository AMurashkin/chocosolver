package org.clafer.ir;

import org.clafer.common.Check;

/**
 * multiplicant * multiplier
 * 
 * @author jimmy
 */
public class IrMul extends IrAbstractInt implements IrIntExpr {

    /**
     * Multiplication is internally represented as a binary operation unlike
     * addition and subtraction. The reason is that this makes optimizing for
     * Choco easier.
     */
    private final IrIntExpr multiplicand, multiplier;

    IrMul(IrIntExpr multiplicand, IrIntExpr multiplier, IrDomain domain) {
        super(domain);
        this.multiplicand = Check.notNull(multiplicand);
        this.multiplier = Check.notNull(multiplier);
    }

    public IrIntExpr getMultiplicand() {
        return multiplicand;
    }

    public IrIntExpr getMultiplier() {
        return multiplier;
    }

    @Override
    public <A, B> B accept(IrIntExprVisitor< A, B> visitor, A a) {
        return visitor.visit(this, a);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof IrMul) {
            IrMul other = (IrMul) obj;
            return multiplicand.equals(other.multiplicand) && multiplier.equals(other.multiplier);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return multiplicand.hashCode() ^ multiplier.hashCode();
    }

    @Override
    public String toString() {
        return "(" + multiplicand + ") * (" + multiplier + ")";
    }
}
