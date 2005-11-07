package org.eclipse.debug.internal.ui.viewers.update;

import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.internal.ui.viewers.IModelProxy;
import org.eclipse.debug.internal.ui.viewers.IModelProxyFactory;
import org.eclipse.debug.internal.ui.viewers.IPresentationContext;
import org.eclipse.debug.ui.IDebugUIConstants;

public class DefaultModelProxyFactory implements IModelProxyFactory {

	public IModelProxy createModelProxy(Object element, IPresentationContext context) {
		String id = context.getPart().getSite().getId();
		if (IDebugUIConstants.ID_DEBUG_VIEW.equals(id)) {
			if (element instanceof IDebugTarget) {
				return new EventHandlerModelProxy();
			}
			if (element instanceof ILaunch) {
				return new LaunchProxy((ILaunch)element);
			}
			if (element instanceof ILaunchManager) {
				return new LaunchManagerProxy();
			}
			if (element instanceof IProcess) {
				return new ProcessProxy((IProcess)element);
			}
		}
		if (IDebugUIConstants.ID_VARIABLE_VIEW.equals(id)) {
			if (element instanceof IStackFrame) {
				return new DefaultVariableViewModelProxy((IStackFrame)element);
			}
		}
		return null;
	}

}
