package org.clafer.choco.constraint.propagator;

import java.util.Arrays;
import solver.constraints.Propagator;
import solver.constraints.PropagatorPriority;
import solver.exception.ContradictionException;
import solver.variables.EventType;
import solver.variables.IntVar;
import solver.variables.SetVar;
import solver.variables.Variable;
import util.ESat;

/**
 *
 * @author jimmy
 */
public class PropJoinInjectiveRelationCard extends Propagator<Variable> {

    private final SetVar take;
    private final IntVar takeCard;
    private final IntVar[] childrenCards;
    private final IntVar toCard;

    public PropJoinInjectiveRelationCard(SetVar take, IntVar takeCard, IntVar[] childrenCards, IntVar toCard) {
        super(buildArray(take, takeCard, toCard, childrenCards), PropagatorPriority.LINEAR, false);
        this.take = take;
        this.takeCard = takeCard;
        this.childrenCards = childrenCards;
        this.toCard = toCard;
    }

    private static Variable[] buildArray(SetVar take, IntVar takeCard, IntVar toCard, IntVar[] childrenCards) {
        Variable[] array = new Variable[childrenCards.length + 3];
        array[0] = take;
        array[1] = takeCard;
        array[2] = toCard;
        System.arraycopy(childrenCards, 0, array, 3, childrenCards.length);
        return array;
    }

    private boolean isTakeVar(int idx) {
        return idx == 0;
    }

    private boolean isTakeCardVar(int idx) {
        return idx == 1;
    }

    private boolean isToCardVar(int idx) {
        return idx == 2;
    }

    private boolean isChildCardVar(int idx) {
        return idx >= 3;
    }

    private int getChildCardVarIndex(int idx) {
        assert isChildCardVar(idx);
        return idx - 3;
    }

    @Override
    public int getPropagationConditions(int vIdx) {
        if (isTakeVar(vIdx)) {
            return EventType.ADD_TO_KER.mask + EventType.REMOVE_FROM_ENVELOPE.mask;
        }
        assert isTakeCardVar(vIdx) || isToCardVar(vIdx) || isChildCardVar(vIdx);
        return EventType.BOUND.mask + EventType.INSTANTIATE.mask;
    }

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        int minCard = 0;
        int maxCard = 0;
        for (int i = take.getEnvelopeFirst(); i != SetVar.END; i = take.getEnvelopeNext()) {
            IntVar childCard = childrenCards[i];
            if (take.kernelContains(i)) {
                minCard += childCard.getLB();
            }
            maxCard += childCard.getUB();
        }
        boolean changed;
        do {
            changed = false;
            toCard.updateLowerBound(minCard, aCause);
            toCard.updateUpperBound(maxCard, aCause);

            int lb = toCard.getLB();
            int ub = toCard.getUB();
            int minCardInc = 0;
            int maxCardDec = 0;
            
            for (int i = take.getEnvelopeFirst(); i != SetVar.END; i = take.getEnvelopeNext()) {
                if (!take.kernelContains(i)) {
                    IntVar childCard = childrenCards[i];
                    if (maxCard - childCard.getUB() < lb) {
                        take.addToKernel(i, aCause);
                        minCardInc += childCard.getLB();
                        changed = true;
                    } else if (minCard + childCard.getLB() > ub) {
                        take.removeFromEnvelope(i, aCause);
                        maxCardDec += childCard.getUB();
                        changed = true;
                    }
                }
            }
            minCard += minCardInc;
            maxCard -= maxCardDec;
        } while (changed);
    }

    @Override
    public void propagate(int idxVarInProp, int mask) throws ContradictionException {
        forcePropagate(EventType.FULL_PROPAGATION);
    }

    @Override
    public ESat isEntailed() {
        int minCard = 0;
        int maxCard = 0;
        for (int i = take.getEnvelopeFirst(); i != SetVar.END; i = take.getEnvelopeNext()) {
            IntVar childCard = childrenCards[i];
            if (take.kernelContains(i)) {
                minCard += childCard.getLB();
            }
            maxCard += childCard.getUB();
        }

        if (toCard.getUB() < minCard) {
            return ESat.FALSE;
        }
        if (toCard.getLB() > maxCard) {
            return ESat.FALSE;
        }

        return isCompletelyInstantiated() ? ESat.TRUE : ESat.UNDEFINED;
    }

    @Override
    public String toString() {
        return "joinInjectiveRelationCard(" + take + ", " + takeCard + ", " + Arrays.toString(childrenCards) + ", " + toCard + ")";
    }
}
