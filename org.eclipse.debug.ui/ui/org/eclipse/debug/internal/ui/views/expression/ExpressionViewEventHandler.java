package org.eclipse.debug.internal.ui.views.expression;

/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp.  All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
**********************************************************************/

import java.util.List;

import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IExpressionsListener;
import org.eclipse.debug.core.model.IExpression;
import org.eclipse.debug.internal.ui.views.variables.VariablesViewEventHandler;
import org.eclipse.debug.ui.AbstractDebugView;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.StructuredSelection;
 
/**
 * Updates the expression view
 */ 
public class ExpressionViewEventHandler extends VariablesViewEventHandler implements IExpressionsListener {

	/**
	 * Constructs a new event handler on the given view
	 * 
	 * @param view variables view
	 */
	public ExpressionViewEventHandler(AbstractDebugView view) {
		super(view);
		DebugPlugin plugin= DebugPlugin.getDefault();
		plugin.getExpressionManager().addExpressionListener(this);		
	}
	
	/**
	 * De-registers this event handler from the debug model.
	 */
	public void dispose() {
		DebugPlugin plugin= DebugPlugin.getDefault();
		plugin.getExpressionManager().removeExpressionListener(this);
		super.dispose();
	}	
	
	/**
	 * @see IExpressionsListener#expressionsAdded(IExpression[])
	 */
	public void expressionsAdded(final IExpression[] expressions) {
		Runnable r = new Runnable() {
			public void run() {
				if (isAvailable()) {
					getTreeViewer().getControl().setRedraw(false);
					for (int i = 0; i < expressions.length; i++) {
						IExpression expression = expressions[i];
						insert(expression);	
						getTreeViewer().expandToLevel(expression, 1);
					}
					getTreeViewer().getControl().setFocus();
					selectAndReveal(expressions[expressions.length - 1]);
					getTreeViewer().getControl().setRedraw(true);
				}
			}
		};
		getView().asyncExec(r);
	}

	/**
	 * @see IExpressionsListener#expressionsRemoved(IExpression[])
	 */
	public void expressionsRemoved(final IExpression[] expressions) {
		Runnable r = new Runnable() {
			public void run() {
				if (isAvailable()) {
					getTreeViewer().getControl().setRedraw(false);
					for (int i = 0; i < expressions.length; i++) {
						IExpression expression = expressions[i];
						remove(expression);
						IContentProvider provider= getTreeViewer().getContentProvider();
						if (provider instanceof ExpressionViewContentProvider) {
							ExpressionViewContentProvider expressionProvider= (ExpressionViewContentProvider) provider;
							List decendants = expressionProvider.getCachedDecendants(expression);
							decendants.add(expression);
							// Remove the parent cache for the expression and its children
							expressionProvider.removeCache(decendants.toArray());
							IExpression[] expressions= DebugPlugin.getDefault().getExpressionManager().getExpressions();
							if (expressions.length > 0) {
								getTreeViewer().setSelection(new StructuredSelection(expressions[0]), true);
							}
						}						
					}
					getTreeViewer().getControl().setRedraw(true);
				}
			}
		};
		getView().asyncExec(r);
	}

	/**
	 * @see IExpressionsListener#expressionsChanged(IExpression[])
	 */
	public void expressionsChanged(final IExpression[] expressions) {
		Runnable r = new Runnable() {
			public void run() {
				getTreeViewer().getControl().setRedraw(false);
				for (int i = 0; i < expressions.length; i++) {
					IExpression expression = expressions[i];
					refresh(expression);	
				}
				getTreeViewer().getControl().setRedraw(true);
			}
		};
		getView().asyncExec(r);			
	}
	
	/**
	 * Override the superclass method. Do nothing.
	 */
	protected void doHandleResumeEvent(DebugEvent event) {
	}
}
