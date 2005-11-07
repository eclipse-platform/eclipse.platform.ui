package org.eclipse.debug.internal.ui.viewers.update;

import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IDebugEventSetListener;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.internal.ui.viewers.AbstractModelProxy;
import org.eclipse.debug.internal.ui.viewers.IModelDelta;
import org.eclipse.debug.internal.ui.viewers.IModelDeltaNode;
import org.eclipse.debug.internal.ui.viewers.IPresentationContext;

public class ProcessProxy extends AbstractModelProxy implements IDebugEventSetListener {

	private IProcess fProcess;

	public ProcessProxy(IProcess process) {
		fProcess = process;
	}

	public void init(IPresentationContext context) {
		DebugPlugin.getDefault().addDebugEventListener(this);
	}

	public void dispose() {
		DebugPlugin.getDefault().removeDebugEventListener(this);
	}
	
	protected synchronized boolean containsEvent(DebugEvent event) {
		return fProcess.equals(event.getSource());
	}

	public void handleDebugEvents(DebugEvent[] events) {
		for (int i = 0; i < events.length; i++) {
			DebugEvent event = events[i];
			switch (event.getKind()) {
			case DebugEvent.CREATE:
				handleCreate();
				break;
			default:
				handleChange();
				break;
			}
		}
	}

	private void handleChange() {
		ModelDelta delta = new ModelDelta();
		IModelDeltaNode node = delta.addNode(DebugPlugin.getDefault().getLaunchManager(), IModelDelta.NOCHANGE);
		node = node.addNode(fProcess.getLaunch(), IModelDelta.NOCHANGE);
		node.addNode(fProcess, IModelDelta.CHANGED | IModelDelta.STATE);
		fireModelChanged(delta);
	}

	private void handleCreate() {
		ModelDelta delta = new ModelDelta();
		IModelDeltaNode node = delta.addNode(DebugPlugin.getDefault().getLaunchManager(), IModelDelta.NOCHANGE);
		node = node.addNode(fProcess.getLaunch(), IModelDelta.NOCHANGE);
		node.addNode(fProcess, IModelDelta.ADDED);
		fireModelChanged(delta);
	}

}
