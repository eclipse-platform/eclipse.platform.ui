/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Wind River Systems - integration with non-standard debug models (Bug 209883)
 *******************************************************************************/
package org.eclipse.debug.internal.ui.actions.expressions;

import java.util.Iterator;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IDebugEventSetListener;
import org.eclipse.debug.core.model.IWatchExpression;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.IActionDelegate2;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;

/**
 * 
 */
public class EnableWatchExpressionAction implements IViewActionDelegate, IActionDelegate2, IDebugEventSetListener {

	private ISelection fSelection;
	private IAction fAction;
	protected boolean fEnable= true;

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IViewActionDelegate#init(org.eclipse.ui.IViewPart)
	 */
	public void init(IViewPart view) {
		DebugPlugin.getDefault().addDebugEventListener(this);
	}

	/**
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	public void run(IAction action) {
		if (fSelection instanceof IStructuredSelection) {
			Iterator iter= ((IStructuredSelection) fSelection).iterator();
			IWatchExpression expression;
			while (iter.hasNext()) {
				expression= getWatchExpression(iter.next());
				if (expression != null) {
    				expression.setEnabled(fEnable);
    				fireWatchExpressionChanged(expression);
				}
			}
		} else if (fSelection instanceof IWatchExpression) {
			IWatchExpression expression= ((IWatchExpression) fSelection);
			expression.setEnabled(fEnable);
			fireWatchExpressionChanged(expression);
		}
	}

	/**
	 * Fires a {@link DebugEvent} for the given watch expression
	 * @param expression the expression that has changed
	 */
	private void fireWatchExpressionChanged(IWatchExpression expression) {
		DebugPlugin.getDefault().fireDebugEventSet(new DebugEvent[] {new DebugEvent(expression, DebugEvent.CHANGE)});
	}

	/**
	 * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {
		fSelection= selection;
		if (fSelection instanceof IStructuredSelection) {
			boolean enabled= false;
			Iterator iter= ((IStructuredSelection) selection).iterator();
			while (iter.hasNext()) {
				IWatchExpression expression = getWatchExpression(iter.next());
				if (expression != null && expression.isEnabled() != fEnable) {
					enabled= true;
					break;
				}
			}
			action.setEnabled(enabled);
		} else if (fSelection instanceof IWatchExpression) {
			action.setEnabled(((IWatchExpression) fSelection).isEnabled() != fEnable);
		} else {
			action.setEnabled(false);
		}
	}

	private IWatchExpression getWatchExpression(Object element) {
	    if (element instanceof IWatchExpression) {
	        return (IWatchExpression)element;
	    } else if (element instanceof IAdaptable) {
	        return (IWatchExpression)((IAdaptable)element).getAdapter(IWatchExpression.class);
	    }
	    return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate2#dispose()
	 */
	public void dispose() {
		DebugPlugin.getDefault().removeDebugEventListener(this);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate2#init(org.eclipse.jface.action.IAction)
	 */
	public void init(IAction action) {
		fAction = action;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate2#runWithEvent(org.eclipse.jface.action.IAction, org.eclipse.swt.widgets.Event)
	 */
	public void runWithEvent(IAction action, Event event) {
		run(action);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.IDebugEventSetListener#handleDebugEvents(org.eclipse.debug.core.DebugEvent[])
	 */
	public void handleDebugEvents(DebugEvent[] events) {
		for (int i = 0; i < events.length; i++) {
			DebugEvent event = events[i];
			if (event.getSource() instanceof IWatchExpression) {
				if (event.getKind() == DebugEvent.CHANGE) {
					selectionChanged(fAction, fSelection);
				}
			}
		}
		
	}
}
