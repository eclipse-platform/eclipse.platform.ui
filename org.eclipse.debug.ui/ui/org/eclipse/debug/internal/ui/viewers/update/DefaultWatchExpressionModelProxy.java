/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.viewers.update;

import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.core.model.IWatchExpression;
import org.eclipse.debug.internal.ui.contexts.DebugContextManager;
import org.eclipse.debug.internal.ui.contexts.provisional.IDebugContextListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;

/**
 * @since 3.2
 *
 */
public class DefaultWatchExpressionModelProxy extends DefaultExpressionModelProxy implements IDebugContextListener {
	
	private IWorkbenchWindow fWindow;
	
	public DefaultWatchExpressionModelProxy(IWatchExpression expression, IWorkbenchWindow window) {
		super(expression);
		fWindow = window;
		DebugContextManager.getDefault().addDebugContextListener(this, window);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.provisional.AbstractModelProxy#installed()
	 */
	public void installed() {
		super.installed();
		IWorkbenchPart part = getPresentationContext().getPart();
		if (part != null) {
			ISelection activeContext = DebugContextManager.getDefault().getActiveContext(part.getSite().getWorkbenchWindow());
			if (activeContext != null) {
				contextActivated(activeContext, null);
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.update.DefaultExpressionModelProxy#dispose()
	 */
	public synchronized void dispose() {
		super.dispose();
		DebugContextManager.getDefault().removeDebugContextListener(this, fWindow);
		fWindow = null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.update.EventHandlerModelProxy#createEventHandlers()
	 */
	protected DebugEventHandler[] createEventHandlers() {
		return new DebugEventHandler[]{new ExpressionEventHandler(this)};
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.contexts.IDebugContextListener#contextActivated(org.eclipse.jface.viewers.ISelection, org.eclipse.ui.IWorkbenchPart)
	 */
	public void contextActivated(ISelection selection, IWorkbenchPart part) {
		if (fWindow != null) {
			if (selection instanceof IStructuredSelection) {
				IDebugElement context = null;
				IStructuredSelection ss = (IStructuredSelection)selection;
				if (ss.size() < 2) {
					Object object = ss.getFirstElement();
					if (object instanceof IDebugElement) {
						context= (IDebugElement) object;
					} else if (object instanceof ILaunch) {
						context= ((ILaunch) object).getDebugTarget();
					}
				}
				((IWatchExpression)getExpression()).setExpressionContext(context);
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.contexts.IDebugContextListener#contextChanged(org.eclipse.jface.viewers.ISelection, org.eclipse.ui.IWorkbenchPart)
	 */
	public void contextChanged(ISelection selection, IWorkbenchPart part) {		
	}	
	
}
