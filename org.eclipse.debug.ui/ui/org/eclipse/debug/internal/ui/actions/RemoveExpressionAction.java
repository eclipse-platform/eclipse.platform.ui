package org.eclipse.debug.internal.ui.actions;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IExpressionManager;
import org.eclipse.debug.core.model.IExpression;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.debug.ui.IDebugView;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;

public class RemoveExpressionAction extends AbstractRemoveActionDelegate {

	protected void doAction(Object element) {
		IExpressionManager manager = DebugPlugin.getDefault().getExpressionManager();
		IExpression exp = getExpression(element);
		if (exp != null) {
			manager.removeExpression(exp);
		}
	}
	
	/**
	 * Returns the expression associated with the given
	 * element.
	 * 
	 * @param element an expression of child of an expression in
	 *  the expression view.
	 * @return associated expression
	 */
	protected IExpression getExpression(Object obj) {
		if (getView() == null) {
			return null;
		}
		IDebugView adapter= (IDebugView)getView().getAdapter(IDebugView.class);
		if (adapter != null) {
			Viewer v= adapter.getViewer();
			if (v instanceof TreeViewer) {
				ITreeContentProvider cp = (ITreeContentProvider)((TreeViewer)v).getContentProvider();
				while (!(obj instanceof IExpression) && obj != null) {
					obj = cp.getParent(obj);
				}
				return (IExpression)obj;
			}	
		}
		return null;
	}
	
	/**
	 * @see AbstractDebugActionDelegate#isEnabledFor(Object)
	 */
	protected boolean isEnabledFor(Object element) {
		return element instanceof IVariable || element instanceof IExpression;
	}
}

