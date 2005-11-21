package org.eclipse.debug.internal.ui.viewers.update;

import org.eclipse.debug.core.IExpressionManager;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IExpression;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.core.model.IWatchExpression;
import org.eclipse.debug.internal.ui.viewers.IModelProxy;
import org.eclipse.debug.internal.ui.viewers.IModelProxyFactory;
import org.eclipse.debug.internal.ui.viewers.IPresentationContext;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.ui.IWorkbenchPart;

public class DefaultModelProxyFactory implements IModelProxyFactory {

	public IModelProxy createModelProxy(Object element, IPresentationContext context) {
		IWorkbenchPart part = context.getPart();
		if (part != null) {
			String id = part.getSite().getId();
			if (IDebugUIConstants.ID_DEBUG_VIEW.equals(id)) {
				if (element instanceof IDebugTarget) {
					return new DebugTargetProxy((IDebugTarget)element);
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
			if (IDebugUIConstants.ID_EXPRESSION_VIEW.equals(id)) {
				if (element instanceof IExpressionManager) {
					return new ExpressionManagerModelProxy();
				} if (element instanceof IWatchExpression) {
					return new DefaultWatchExpressionModelProxy((IWatchExpression)element, part.getSite().getWorkbenchWindow());
				}
				if (element instanceof IExpression) {
					return new DefaultExpressionModelProxy((IExpression)element);
				}
			}
			if (IDebugUIConstants.ID_REGISTER_VIEW.equals(id)) {
				if (element instanceof IStackFrame) {
					return new DefaultVariableViewModelProxy((IStackFrame)element);
				}
			}
		}
		return null;
	}

}
