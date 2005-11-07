package org.eclipse.debug.internal.ui.viewers.update;

import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IDebugEventSetListener;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.core.model.IThread;
import org.eclipse.debug.internal.ui.viewers.AbstractModelProxy;
import org.eclipse.debug.internal.ui.viewers.IModelDelta;
import org.eclipse.debug.internal.ui.viewers.IModelDeltaNode;
import org.eclipse.debug.internal.ui.viewers.IPresentationContext;

public class DebugTargetProxy extends AbstractModelProxy implements IDebugEventSetListener {

	private IDebugTarget fDebugTarget;
	private IPresentationContext fContext;

	public DebugTargetProxy(IDebugTarget target) {
		fDebugTarget = target;
	}

	public void init(IPresentationContext context) {
		fContext = context;
		DebugPlugin.getDefault().addDebugEventListener(this);
	}

	public void dispose() {
		DebugPlugin.getDefault().removeDebugEventListener(this);
		fContext = null;
	}

	public void handleDebugEvents(DebugEvent[] events) {
		for (int i = 0; i < events.length; i++) {
			DebugEvent event = events[i];
			if (containsEvent(event)) {
				switch (event.getKind()) {
				case DebugEvent.CREATE:
					dispatchCreate(event);
					break;
				case DebugEvent.TERMINATE:
//					dispatchTerminate(event);
					break;
				case DebugEvent.SUSPEND:
//					dispatchSuspend(event);
					break;
				case DebugEvent.RESUME:
//					dispatchResume(event);
					break;
				case DebugEvent.CHANGE:
//					dispatchChange(event);
					break;
				default:
//					dispatchOther(event);
					break;
				}
			}
		}
	}

	private void dispatchCreate(DebugEvent event) {
		Object source = event.getSource();
		ModelDelta delta = createDelta(source);
		fireModelChanged(delta);
	}

	private ModelDelta createDelta(Object source) {
		ModelDelta delta = new ModelDelta();
		IModelDeltaNode node = delta.addNode(DebugPlugin.getDefault().getLaunchManager(), IModelDelta.NOCHANGE); 
		node = node.addNode(fDebugTarget.getLaunch(), IModelDelta.NOCHANGE);
		node.addNode(fDebugTarget, IModelDelta.NOCHANGE);
		
		if (source instanceof IThread) {
			node.addNode(source, IModelDelta.ADDED | IModelDelta.CONTENT);
		}
		
		if (source instanceof IStackFrame) {
			IStackFrame frame = (IStackFrame) source;
			node.addNode(frame.getThread(), IModelDelta.NOCHANGE);
			node.addNode(frame, IModelDelta.ADDED | IModelDelta.SELECT);
		}
		return delta;
	}

	protected boolean containsEvent(DebugEvent event) {
		Object source = event.getSource();
		if (source instanceof IDebugElement) {
			return fDebugTarget.equals(((IDebugElement) source).getDebugTarget());
		}
		return false;
	}

}
