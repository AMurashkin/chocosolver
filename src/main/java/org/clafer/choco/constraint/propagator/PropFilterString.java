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
 * result = [string !! i | i <- set].
 *
 * Pads result with extra negative one if necessary.
 *
 * @author jimmy
 */
public class PropFilterString extends Propagator<Variable> {

    private final SetVar set;
    private final int offset;
    // Sorted in decreasing order. Non-negatives
    private final IntVar[] string;
    private final IntVar[] result;

    public PropFilterString(SetVar set, int offset, IntVar[] string, IntVar[] result) {
        super(buildArray(set, string, result), PropagatorPriority.LINEAR, false);
        this.set = set;
        this.offset = offset;
        this.string = string;
        this.result = result;
    }

    public static Variable[] buildArray(SetVar set, IntVar[] string, IntVar[] result) {
        Variable[] array = new Variable[1 + string.length + result.length];
        array[0] = set;
        System.arraycopy(string, 0, array, 1, string.length);
        System.arraycopy(result, 0, array, 1 + string.length, result.length);
        return array;
    }

    private boolean isSetVar(int idx) {
        return idx == 0;
    }

    private boolean isStringVar(int idx) {
        return idx > 0 && idx <= string.length;
    }

    private int getStringVarIndex(int idx) {
        assert isStringVar(idx);
        return idx - 1;
    }

    private boolean isResultVar(int idx) {
        return idx > string.length;
    }

    private int getResultVarIndex(int idx) {
        assert isResultVar(idx);
        return idx - 1 - string.length;
    }

    @Override
    public int getPropagationConditions(int vIdx) {
        if (isSetVar(vIdx)) {
            return EventType.ADD_TO_KER.mask + EventType.REMOVE_FROM_ENVELOPE.mask;
        }
        return EventType.INT_ALL_MASK();
    }

    private boolean subset(IntVar sub, IntVar[] sups, int from, int to) throws ContradictionException {
        boolean changed = false;
        int ub = sub.getUB();
        for (int val = sub.getLB(); val <= ub; val = sub.nextValue(val)) {
            boolean found = false;
            for (int i = from; i < to; i++) {
                if (sups[i].contains(val)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                changed |= sub.removeValue(val, aCause);
            }
        }
        return changed;
    }

    private boolean subset(IntVar sub, IntVar[] sups, int[] indices, int low, int high) throws ContradictionException {
        boolean changed = false;
        int ub = sub.getUB();
        for (int val = sub.getLB(); val <= ub; val = sub.nextValue(val)) {
            boolean found = false;
            for (int i = low; i <= high; i++) {
                if (sups[indices[i]].contains(val)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                changed |= sub.removeValue(val, aCause);
            }
        }
        return changed;
    }

    private boolean subsetOrNegativeOne(IntVar sub, IntVar[] sups, int[] indices, int from, int to) throws ContradictionException {
        boolean changed = false;
        int ub = sub.getUB();
        for (int val = sub.getLB(); val <= ub; val = sub.nextValue(val)) {
            if (val != -1) {
                boolean found = false;
                for (int i = from; i < to; i++) {
                    assert i < indices.length;
                    if (sups[indices[i]].contains(val)) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    changed |= sub.removeValue(val, aCause);
                }
            }
        }
        return changed;
    }

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        if (set.getKernelSize() > result.length) {
            contradiction(set, "Too many in kernel");
        }

        int[] env = new int[set.getEnvelopeSize()];
        int[] kerIndices = new int[set.getKernelSize()];

        boolean changed;
        do {
            changed = false;
            // The number of ker elements seen.
            int kerIndex = 0;
            // The number of env elements seen.
            int envIndex = 0;
            for (int i = set.getEnvelopeFirst(); i != SetVar.END; i = set.getEnvelopeNext()) {
                int x = i - offset;
                env[envIndex] = x;
                if (set.kernelContains(i)) {
                    kerIndices[kerIndex] = envIndex;
                    changed |= subset(string[x], result, kerIndex, Math.min(envIndex + 1, result.length));
                    envIndex++;
                    kerIndex++;
                } else {
                    boolean found = false;
                    for (int j = kerIndex; j < result.length && j <= envIndex; j++) {
                        if (PropUtil.domainIntersectDomain(string[x], result[j])) {
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        changed |= set.removeFromEnvelope(i, aCause);
                    } else {
                        envIndex++;
                    }
                }
            }
            assert envIndex <= env.length;
            assert kerIndex == kerIndices.length;

            for (int i = 0; i < result.length; i++) {
                if (i < kerIndices.length) {
                    changed |= subset(result[i], string, env, i, kerIndices[i]);
                } else {
                    changed |= subsetOrNegativeOne(result[i], string, env, i, envIndex);
                }
            }
        } while (changed);

        if (set.instantiated()) {
            for (int i = set.getKernelSize(); i < result.length; i++) {
                result[i].instantiateTo(-1, aCause);
            }
        } else {
            int i = set.getKernelSize();
            for (; i < result.length && !result[i].contains(-1); i++) {
            }
            if (set.getEnvelopeSize() < i) {
                contradiction(set, "Too few in envelope");
            }
        }
    }

    @Override
    public void propagate(int idxVarInProp, int mask) throws ContradictionException {
        forcePropagate(EventType.FULL_PROPAGATION);
    }

    @Override
    public ESat isEntailed() {
        if (!isCompletelyInstantiated()) {
            return ESat.UNDEFINED;
        }
        int index = 0;
        for (int i = set.getKernelFirst(); i != SetVar.END; i = set.getKernelNext(), index++) {
            if (index >= result.length) {
                return ESat.FALSE;
            }
            int x = i - offset;
            if (string[x].getValue() != result[index].getValue()) {
                return ESat.FALSE;
            }
        }
        for (; index < result.length; index++) {
            if (result[index].getValue() != -1) {
                return ESat.FALSE;
            }
        }
        return ESat.TRUE;
    }

    @Override
    public String toString() {
        return "filter(" + set + " >> " + offset + ", " + Arrays.toString(string) + ", " + Arrays.toString(result) + ")";
    }
}
