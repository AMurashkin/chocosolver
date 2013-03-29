package org.clafer.ir;

import java.util.Arrays;
import org.clafer.Check;

/**
 *
 * @author jimmy
 */
public class IrIntChannel implements IrConstraint {

    private final IrIntExpr[] ints;
    private final IrSetExpr[] sets;

    IrIntChannel(IrIntExpr[] ints, IrSetExpr[] sets) {
        this.ints = Check.noNulls(ints);
        this.sets = Check.noNulls(sets);
    }

    public IrIntExpr[] getInts() {
        return ints;
    }

    public IrSetExpr[] getSets() {
        return sets;
    }

    @Override
    public <A, B> B accept(IrConstraintVisitor<A, B> visitor, A a) {
        return visitor.visit(this, a);
    }

    @Override
    public String toString() {
        return "intChannel(" + Arrays.toString(ints) + ", " + Arrays.toString(sets) + ")";
    }
}