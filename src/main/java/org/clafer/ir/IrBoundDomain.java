package org.clafer.ir;

import gnu.trove.iterator.TIntIterator;
import org.clafer.collection.BoundIntIterator;
import org.clafer.collection.ReverseBoundIntIterator;

/**
 * A contiguous domain between a low and high bound.
 *
 * @author jimmy
 */
public class IrBoundDomain implements IrDomain {

    private final int low;
    private final int high;

    /**
     * @param low lowest value in the domain, inclusive
     * @param high highest value in the domain, inclusive
     */
    public IrBoundDomain(int low, int high) {
        if (low > high) {
            throw new IllegalArgumentException();
        }
        this.low = low;
        this.high = high;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isBounded() {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean contains(int value) {
        return value >= low && value <= high;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getLowBound() {
        return low;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getHighBound() {
        return high;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isEmpty() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int size() {
        return high + 1 - low;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int[] getValues() {
        int[] values = new int[size()];
        for (int i = 0; i < values.length; i++) {
            values[i] = low + i;
        }
        return values;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TIntIterator iterator() {
        return iterator(true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TIntIterator iterator(boolean increasing) {
        return increasing ? new BoundIntIterator(low, high) : new ReverseBoundIntIterator(low, high);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof IrDomain) {
            IrDomain other = (IrDomain) obj;
            if (other.isEmpty()) {
                return false;
            }
            if (other.isBounded()) {
                return low == other.getLowBound() && high == other.getHighBound();
            }
            int[] otherValues = other.getValues();
            if (size() != other.size()) {
                return false;
            }
            for (int i = 0; i < otherValues.length; i++) {
                if (otherValues[i] != low + i) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return low ^ high;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "{" + low + ", ..., " + high + "}";
    }
}
