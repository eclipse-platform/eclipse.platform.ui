/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.views.expression;


import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IExpressionManager;
import org.eclipse.debug.core.model.IErrorReportingExpression;
import org.eclipse.debug.core.model.IExpression;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.views.variables.VariablesViewContentProvider;
import org.eclipse.debug.ui.IDebugView;
import org.eclipse.debug.ui.IObjectBrowser;
 
/**
 * Provides contents for the expression view
 */
public class ExpressionViewContentProvider extends VariablesViewContentProvider {

	public ExpressionViewContentProvider(IDebugView view) {
		super(view);
	}

	/**
	 * @see ITreeContentProvider#getChildren(Object)
	 */
	public Object[] getChildren(Object parent) {
		Object[] children= null;
		try {
			if (parent instanceof IExpressionManager) {
				// do not cache parents
				return ((IExpressionManager)parent).getExpressions();
			} else if (parent instanceof IExpression) {
				if (parent instanceof IErrorReportingExpression) {
					IErrorReportingExpression expression= (IErrorReportingExpression) parent;
					if (expression.hasErrors()) {
						children= expression.getErrorMessages();
					}
				}
				if (children == null) {
					children = getModelSpecificExpressionChildren((IExpression)parent);
				}
			} else if (parent instanceof IVariable) {
				children = getModelSpecificVariableChildren((IVariable)parent);
			}
			if (children != null) {
				cache(parent, children);
				return children;
			}
		} catch (DebugException de) {
			DebugUIPlugin.log(de);
		}
		return new Object[0];
	}
	
	protected IVariable[] getModelSpecificExpressionChildren(IExpression parent) throws DebugException {
		IObjectBrowser contentProvider = getObjectBrowser(getDebugModelId(parent));
		IValue value = parent.getValue();
		if (value == null) {
			return new IVariable[0];	
		}
		return contentProvider.getChildren(value);
	}
	
	/**
	 * @see ITreeContentProvider#getParent(Object)
	 */
	public Object getParent(Object item) {
		if (item instanceof IExpression) {
			return DebugPlugin.getDefault().getExpressionManager();
		}
		return super.getParent(item);
	}
	
	/**
	 * @see ITreeContentProvider#hasChildren(Object)
	 */
	public boolean hasChildren(Object element) {
		if (element instanceof IExpressionManager) {
			return ((IExpressionManager)element).hasExpressions();
		} else if (element instanceof IExpression) {
			if (element instanceof IErrorReportingExpression && ((IErrorReportingExpression) element).hasErrors()) {
				return true;
			}
			IValue v = ((IExpression)element).getValue();
			if (v == null) {
				return false;
			}
			try {
				return v.hasVariables();
			} catch (DebugException e) {
				DebugUIPlugin.log(e);
				return false;
			}
		}
		return super.hasChildren(element);
	}	
}
