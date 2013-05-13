package org.clafer.constraint.propagator;

import gnu.trove.set.hash.TIntHashSet;
import solver.constraints.propagators.Propagator;
import solver.constraints.propagators.PropagatorPriority;
import solver.exception.ContradictionException;
import solver.variables.EventType;
import solver.variables.IntVar;
import solver.variables.SetVar;
import solver.variables.Variable;
import solver.variables.delta.IIntDeltaMonitor;
import solver.variables.delta.monitor.SetDeltaMonitor;
import util.ESat;
import util.procedure.IntProcedure;

/**
 * Assumptions: Take set is over a small domain. Ref.length is small.
 * 
 * @author jimmy
 */
public class PropJoinRef extends Propagator<Variable> {

    private final SetVar take;
    private final SetDeltaMonitor takeD;
    private final IntVar[] refs;
    private final IIntDeltaMonitor[] refsD;
    private final SetVar to;
    private final SetDeltaMonitor toD;

    public PropJoinRef(SetVar take, IntVar[] refs, SetVar to) {
        super(buildArray(take, to, refs), PropagatorPriority.LINEAR);
        this.take = take;
        this.takeD = take.monitorDelta(aCause);
        this.refs = refs;
        this.refsD = PropUtil.monitorDeltas(refs, aCause);
        this.to = to;
        this.toD = to.monitorDelta(aCause);
    }

    private static Variable[] buildArray(SetVar take, SetVar to, IntVar[] refs) {
        Variable[] array = new Variable[refs.length + 2];
        array[0] = take;
        array[1] = to;
        System.arraycopy(refs, 0, array, 2, refs.length);
        return array;
    }

    private boolean isTakeVar(int idx) {
        return idx == 0;
    }

    private boolean isToVar(int idx) {
        return idx == 1;
    }

    private boolean isRefVar(int idx) {
        return idx >= 2;
    }

    private int getRefVarIndex(int idx) {
        return idx - 2;
    }

    @Override
    public int getPropagationConditions(int vIdx) {
        if (isTakeVar(vIdx)) {
            return EventType.ADD_TO_KER.mask + EventType.REMOVE_FROM_ENVELOPE.mask;
        }
        if (isToVar(vIdx)) {
            return EventType.ADD_TO_KER.mask + EventType.REMOVE_FROM_ENVELOPE.mask;
        }
        assert isRefVar(vIdx);
        return EventType.INT_ALL_MASK();
    }

    private boolean possibleTo(int[] take, int to) {
        for (int i : take) {
            if (refs[i].contains(to)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        // Prune to
        // Need to iterate env(take) many times so read it into an array
        int[] takeEnv = PropUtil.iterateEnv(take);
        for (int i = to.getEnvelopeFirst(); i != SetVar.END; i = to.getEnvelopeNext()) {
            if (!possibleTo(takeEnv, i)) {
                to.removeFromEnvelope(i, aCause);
            }
        }

        // Prune refs, Pick to
        for (int i = take.getKernelFirst(); i != SetVar.END; i = take.getKernelNext()) {
            PropUtil.intSubsetEnv(refs[i], to, aCause);
            if (refs[i].instantiated()) {
                to.addToKernel(refs[i].getValue(), aCause);
            }
        }

        // Prune take
        for (int i : takeEnv) {
            if (!PropUtil.canIntersect(refs[i], to)) {
                take.removeFromEnvelope(i, aCause);
            }
        }
    }

    @Override
    public void propagate(int idxVarInProp, int mask) throws ContradictionException {
        if (isTakeVar(idxVarInProp)) {
            if ((mask & EventType.REMOVE_FROM_ENVELOPE.mask) != 0) {
                // Prune to
                int[] takeEnv = PropUtil.iterateEnv(take);
                for (int i = to.getEnvelopeFirst(); i != SetVar.END; i = to.getEnvelopeNext()) {
                    if (!possibleTo(takeEnv, i)) {
                        to.removeFromEnvelope(i, aCause);
                    }
                }
            }
            takeD.freeze();
            takeD.forEach(pruneRefAndPickToOnTakeKer, EventType.ADD_TO_KER);
            takeD.unfreeze();
        } else if (isToVar(idxVarInProp)) {
            toD.freeze();
            toD.forEach(pruneRefAndPickToOnToEnv, EventType.REMOVE_FROM_ENVELOPE);
            toD.unfreeze();
            if ((mask & EventType.REMOVE_FROM_ENVELOPE.mask) != 0) {
                // Prune take
                for (int i = take.getEnvelopeFirst(); i != SetVar.END; i = take.getEnvelopeNext()) {
                    if (!PropUtil.canIntersect(refs[i], to)) {
                        take.removeFromEnvelope(i, aCause);
                    }
                }
            }
        } else {
            assert isRefVar(idxVarInProp);
            int id = getRefVarIndex(idxVarInProp);

            refsD[id].freeze();
            if (take.envelopeContains(id)) {
                refsD[id].forEach(pruneToOnRefRem, EventType.REMOVE);
            }
            refsD[id].unfreeze();

            // Prune refs, Pick to
            for (int i = take.getKernelFirst(); i != SetVar.END; i = take.getKernelNext()) {
                PropUtil.intSubsetEnv(refs[i], to, aCause);
                if (refs[i].instantiated()) {
                    to.addToKernel(refs[i].getValue(), aCause);
                }
            }

            // Prune take
            for (int i : PropUtil.iterateEnv(take)) {
                if (!PropUtil.canIntersect(refs[i], to)) {
                    take.removeFromEnvelope(i, aCause);
                }
            }
        }
    }
    private final IntProcedure pruneRefAndPickToOnTakeKer = new IntProcedure() {

        @Override
        public void execute(int takeKer) throws ContradictionException {
            PropUtil.intSubsetEnv(refs[takeKer], to, aCause);
            if (refs[takeKer].instantiated()) {
                to.addToKernel(refs[takeKer].getValue(), aCause);
            }
        }
    };
    private final IntProcedure pruneRefAndPickToOnToEnv = new IntProcedure() {

        @Override
        public void execute(int toEnv) throws ContradictionException {
            for (int i = take.getKernelFirst(); i != SetVar.END; i = take.getKernelNext()) {
                refs[i].removeValue(toEnv, aCause);
                if (refs[i].instantiated() && take.kernelContains(i)) {
                    to.addToKernel(refs[i].getValue(), aCause);
                }
            }
        }
    };
    private final IntProcedure pruneToOnRefRem = new IntProcedure() {

        @Override
        public void execute(int refRem) throws ContradictionException {
            for (int i = take.getEnvelopeFirst(); i != SetVar.END; i = take.getEnvelopeNext()) {
                if (refs[i].contains(refRem)) {
                    return;
                }
            }
            to.removeFromEnvelope(refRem, aCause);
        }
    };

    @Override
    public ESat isEntailed() {
        for (int i = take.getKernelFirst(); i != SetVar.END; i = take.getKernelNext()) {
            if (!PropUtil.approxCanIntersect(refs[i], to, true)) {
                return ESat.FALSE;
            }
        }
        if (!take.instantiated() || !to.instantiated()) {
            return ESat.UNDEFINED;
        }
        TIntHashSet values = new TIntHashSet();
        for (IntVar ref : refs) {
            if (!ref.instantiated()) {
                return ESat.UNDEFINED;
            }
            values.add(ref.getValue());
        }
        return values.containsAll(to.getValue()) ? ESat.TRUE : ESat.UNDEFINED;
    }
}