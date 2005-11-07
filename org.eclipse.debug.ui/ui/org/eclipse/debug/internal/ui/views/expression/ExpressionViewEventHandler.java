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
package org.eclipse.debug.internal.ui.views.expression;


import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IExpressionsListener;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IExpression;
import org.eclipse.debug.core.model.IWatchExpression;
import org.eclipse.debug.internal.ui.views.variables.VariablesViewEventHandler;
import org.eclipse.debug.ui.AbstractDebugView;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
 
/**
 * Updates the expression view
 */ 
public class ExpressionViewEventHandler extends VariablesViewEventHandler implements IExpressionsListener {

	/**
	 * Also update expressions if a target terminates
	 * 
	 * @see org.eclipse.debug.internal.ui.views.variables.VariablesViewEventHandler#doHandleTerminateEvent(org.eclipse.debug.core.DebugEvent)
	 */
	protected void doHandleTerminateEvent(DebugEvent event) {
		super.doHandleTerminateEvent(event);
		if (event.getSource() instanceof IDebugTarget) {
			IExpression[] expressions = DebugPlugin.getDefault().getExpressionManager().getExpressions();
			IAdaptable object = DebugUITools.getDebugContext();
			IDebugElement context= null;
			if (object instanceof IDebugElement) {
				context= (IDebugElement) object;
			} else if (object instanceof ILaunch) {
				context= ((ILaunch) object).getDebugTarget();
			}
			for (int i = 0; i < expressions.length; i++) {
				IExpression expression = expressions[i];
				if (expression instanceof IWatchExpression) {
					((IWatchExpression)expression).setExpressionContext(context);
				}
			}			
		}
	}

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
					getStructuredViewer().refresh();
					if (expressions.length > 0) {
						ISelection selection = new StructuredSelection(expressions[0]); 
						getStructuredViewer().setSelection(selection, true);
					}
				}
			}
		};
		getView().asyncExec(r);
	}

	/**
	 * @see IExpressionsListener#expressionsRemoved(IExpression[])
	 */
	public void expressionsRemoved(final IExpression[] expressions) {
		if (isAvailable()) {
			getStructuredViewer().refresh();
		}
	}

	/**
	 * @see IExpressionsListener#expressionsChanged(IExpression[])
	 */
	public void expressionsChanged(final IExpression[] expressions) {
		Runnable r = new Runnable() {
			public void run() {
				if (isAvailable()) {
					getStructuredViewer().getControl().setRedraw(false);
					for (int i = 0; i < expressions.length; i++) {
						IExpression expression = expressions[i];
						refresh(expression);
						// update details if selected
						IStructuredSelection selection = (IStructuredSelection)getViewer().getSelection();
						if (selection.size() == 1 && selection.getFirstElement().equals(expression)) {
							getVariablesView().populateDetailPane();	
						}
					}
					getStructuredViewer().getControl().setRedraw(true);
				}
			}
		};
		getView().asyncExec(r);			
	}
	
	/**
	 * Override the superclass method. Do nothing.
	 */
	protected void doHandleResumeEvent(DebugEvent event) {
	}
	/**
	 * @see org.eclipse.debug.internal.ui.views.variables.VariablesViewEventHandler#doHandleChangeEvent(org.eclipse.debug.core.DebugEvent)
	 */
	protected void doHandleChangeEvent(DebugEvent event) {
		if (event.getSource() instanceof IExpression) {
			refresh(event.getSource());
			getVariablesView().populateDetailPane();
		} else {
			super.doHandleChangeEvent(event);
		}
	}

	protected boolean isFiltered(DebugEvent event) {
		// TODO Auto-generated method stub
		return false;
	}
	
	
	
}
