/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.actions;


import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IExpressionManager;
import org.eclipse.debug.core.model.IExpression;
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
}

