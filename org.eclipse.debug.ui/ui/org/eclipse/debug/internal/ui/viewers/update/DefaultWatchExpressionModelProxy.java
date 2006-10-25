/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
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
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.contexts.DebugContextEvent;
import org.eclipse.debug.ui.contexts.IDebugContextListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
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
		DebugUITools.getDebugContextManager().getContextService(window).addDebugContextListener(this);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.provisional.AbstractModelProxy#installed()
	 */
	public void installed() {
		super.installed();
		ISelection activeContext = DebugUITools.getDebugContextManager().getContextService(fWindow).getActiveContext();
		if (activeContext != null) {
			contextActivated(activeContext);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.update.DefaultExpressionModelProxy#dispose()
	 */
	public synchronized void dispose() {
		super.dispose();
		DebugUITools.getDebugContextManager().getContextService(fWindow).removeDebugContextListener(this);
		fWindow = null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.update.EventHandlerModelProxy#createEventHandlers()
	 */
	protected DebugEventHandler[] createEventHandlers() {
		return new DebugEventHandler[]{new ExpressionEventHandler(this)};
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.contexts.provisional.IDebugContextListener#contextEvent(org.eclipse.debug.internal.ui.contexts.provisional.DebugContextEvent)
	 */
	public void debugContextChanged(DebugContextEvent event) {
		if ((event.getFlags() & DebugContextEvent.ACTIVATED) > 0) {
			contextActivated(event.getContext());
		}
	}

	/**
	 * @param selection
	 */
	protected void contextActivated(ISelection selection) {
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
