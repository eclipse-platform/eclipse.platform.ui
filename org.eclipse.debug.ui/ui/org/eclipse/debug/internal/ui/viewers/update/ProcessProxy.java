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
            return event.getSource().equals(fProcess);
        }

		protected void handleChange(DebugEvent event) {
			ModelDelta delta = null;
        	synchronized (ProcessProxy.this) {
        		if (!isDisposed()) {
                    delta = new ModelDelta();
                    IModelDeltaNode node = delta.addNode(DebugPlugin.getDefault().getLaunchManager(), IModelDelta.NOCHANGE);
                    node = node.addNode(fProcess.getLaunch(), IModelDelta.NOCHANGE);
                    node.addNode(fProcess, IModelDelta.CHANGED | IModelDelta.STATE);        			
        		}
			}
        	if (delta != null && !isDisposed()) {
        		fireModelChanged(delta);
        	}
        }

        protected void handleCreate(DebugEvent event) {
        	// do nothing - Launch change notification handles this
        }
        
    };

    /* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.update.EventHandlerModelProxy#dispose()
	 */
	public synchronized void dispose() {
		super.dispose();
		fProcess = null;
	}

	public ProcessProxy(IProcess process) {
        fProcess = process;
    }

    protected synchronized boolean containsEvent(DebugEvent event) {
        return event.getSource().equals(fProcess);
    }

    protected DebugEventHandler[] createEventHandlers() {
        return new DebugEventHandler[] {fProcessEventHandler};
    }

	public void setInitialState() {
	}
}
