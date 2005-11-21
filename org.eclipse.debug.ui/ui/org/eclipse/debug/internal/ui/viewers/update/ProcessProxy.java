package org.eclipse.debug.internal.ui.viewers.update;

import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.internal.ui.viewers.IModelDelta;
import org.eclipse.debug.internal.ui.viewers.IModelDeltaNode;

public class ProcessProxy extends EventHandlerModelProxy {

    private IProcess fProcess;

    private DebugEventHandler fProcessEventHandler = new DebugEventHandler(this) {
        protected boolean handlesEvent(DebugEvent event) {
            return fProcess.equals(event.getSource());
        }

        protected void handleChange(DebugEvent event) {
            ModelDelta delta = new ModelDelta();
            IModelDeltaNode node = delta.addNode(DebugPlugin.getDefault().getLaunchManager(), IModelDelta.NOCHANGE);
            node = node.addNode(fProcess.getLaunch(), IModelDelta.NOCHANGE);
            node.addNode(fProcess, IModelDelta.CHANGED | IModelDelta.STATE);
            fireModelChanged(delta);

        }

        protected void handleCreate(DebugEvent event) {
        	// do nothing - Launch change notification handles this
        }
        
    };

    public ProcessProxy(IProcess process) {
        fProcess = process;
    }

    protected synchronized boolean containsEvent(DebugEvent event) {
        return fProcess.equals(event.getSource());
    }

    protected DebugEventHandler[] createEventHandlers() {
        return new DebugEventHandler[] {fProcessEventHandler};
    }
}
