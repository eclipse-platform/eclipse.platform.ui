/*******************************************************************************
 * Copyright (c) 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.commands.actions;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler2;
import org.eclipse.core.commands.IHandlerListener;
import org.eclipse.debug.internal.ui.views.launch.LaunchView;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

/**
 * An action handler that delegates to an action in the debug view.
 */
public abstract class DebugActionHandler implements IHandler2 {
	
	private String fActionId;
	
	/**
	 * Constructs a new handler for the given action identifier.
	 * 
	 * @param actionId action identifier
	 */
	public DebugActionHandler(String actionId) {
		fActionId = actionId;
	}
		
	/**
	 * Returns the delegate handler or <code>null</code> if none.
	 * 
	 * @return handler or <code>null</code>
	 */
	protected IHandler2 getDelegate() {
		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		if (window != null) {
			IViewReference reference = window.getActivePage().findViewReference(IDebugUIConstants.ID_DEBUG_VIEW);
			if (reference != null) {
				IViewPart view = reference.getView(false);
				if (view instanceof LaunchView) {
					return ((LaunchView)view).getHandler(fActionId);
				}
			}
		}
		return null;
	}
		
	/* (non-Javadoc)
	 * @see org.eclipse.core.commands.IHandler#execute(org.eclipse.core.commands.ExecutionEvent)
	 */
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IHandler2 delegate = getDelegate();
		if (delegate != null) {
			return delegate.execute(event);
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.commands.IHandler#addHandlerListener(org.eclipse.core.commands.IHandlerListener)
	 */
	public void addHandlerListener(IHandlerListener handlerListener) {
		IHandler2 delegate = getDelegate();
		if (delegate != null) {
			delegate.addHandlerListener(handlerListener);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.commands.IHandler#dispose()
	 */
	public void dispose() {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.commands.IHandler#isEnabled()
	 */
	public boolean isEnabled() {
		IHandler2 delegate = getDelegate();
		if (delegate != null) {
			return delegate.isEnabled();
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.commands.IHandler#isHandled()
	 */
	public boolean isHandled() {
		IHandler2 delegate = getDelegate();
		if (delegate != null) {
			return delegate.isHandled();
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.commands.IHandler#removeHandlerListener(org.eclipse.core.commands.IHandlerListener)
	 */
	public void removeHandlerListener(IHandlerListener handlerListener) {
		IHandler2 delegate = getDelegate();
		if (delegate != null) {
			delegate.removeHandlerListener(handlerListener);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.commands.IHandler2#setEnabled(java.lang.Object)
	 */
	public void setEnabled(Object evaluationContext) {
		IHandler2 delegate = getDelegate();
		if (delegate != null) {
			delegate.setEnabled(evaluationContext);
		}
	}
}
