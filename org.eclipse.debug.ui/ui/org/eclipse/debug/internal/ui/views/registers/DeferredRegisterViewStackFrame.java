package org.eclipse.debug.internal.ui.views.registers;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.internal.ui.elements.adapters.DeferredStackFrame;

public class DeferredRegisterViewStackFrame extends DeferredStackFrame {
    public Object[] getChildren(Object parent) {
        try {
            return ((IStackFrame)parent).getRegisterGroups();
        } catch (DebugException e) {
        }
        return EMPTY;
    }
}
