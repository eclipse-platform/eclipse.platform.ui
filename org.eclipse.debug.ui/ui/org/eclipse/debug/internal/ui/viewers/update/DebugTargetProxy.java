package org.eclipse.debug.internal.ui.viewers.update;

import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.core.model.IDebugTarget;

public class DebugTargetProxy extends EventHandlerModelProxy {

    private IDebugTarget fDebugTarget;

    private DebugEventHandler[] fDebugEventHandlers = new DebugEventHandler[] { new DebugTargetEventHandler(this), new ThreadEventHandler(this) };

    public DebugTargetProxy(IDebugTarget target) {
        fDebugTarget = target;
    }

    protected boolean containsEvent(DebugEvent event) {
        Object source = event.getSource();
        if (source instanceof IDebugElement) {
            IDebugTarget debugTarget = ((IDebugElement) source).getDebugTarget();
            return fDebugTarget.equals(debugTarget);
        }
        return false;
    }

    protected DebugEventHandler[] createEventHandlers() {
        return fDebugEventHandlers;
    }

}
