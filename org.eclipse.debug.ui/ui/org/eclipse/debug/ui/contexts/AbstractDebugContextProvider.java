/*******************************************************************************
 * Copyright (c) 2006, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.ui.contexts;

import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.ui.IWorkbenchPart;

/**
 * Abstract implementation of a debug context provider.
 * <p>
 * Clients implementing context providers should subclass this class.
 * </p>
 * @since 3.3
 */
public abstract class AbstractDebugContextProvider implements IDebugContextProvider {
	
	/**
	 * Event listeners
	 */
	private ListenerList fListeners = new ListenerList();
	
	/**
	 * Part or <code>null</code>
	 */
	private IWorkbenchPart fPart;
	
	/**
	 * Constructs a context provider for the specified part, possibly <code>null</code>.
	 * 
	 * @param part workbench part or <code>null</code>
	 */
	public AbstractDebugContextProvider(IWorkbenchPart part) {
		fPart = part;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.contexts.provisional.IDebugContextProvider#addDebugContextEventListener(org.eclipse.debug.internal.ui.contexts.provisional.IDebugContextEventListener)
	 */
	public void addDebugContextListener(IDebugContextListener listener) {
		fListeners.add(listener);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.contexts.provisional.IDebugContextProvider#getPart()
	 */
	public IWorkbenchPart getPart() {
		return fPart;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.contexts.provisional.IDebugContextProvider#removeDebugContextEventListener(org.eclipse.debug.internal.ui.contexts.provisional.IDebugContextEventListener)
	 */
	public void removeDebugContextListener(IDebugContextListener listener) {
		fListeners.remove(listener);
	}

	/**
	 * Fires the given context event to all registered listeners.
	 * 
	 * @param event debug context event
	 */
	protected void fire(final DebugContextEvent event) {
		Object[] listeners = fListeners.getListeners();
		for (int i = 0; i < listeners.length; i++) {
			final IDebugContextListener listener = (IDebugContextListener) listeners[i];
            SafeRunner.run(new ISafeRunnable() {
				public void run() throws Exception {
					listener.debugContextChanged(event);
				}
				public void handleException(Throwable exception) {
					DebugUIPlugin.log(exception);
				}
			});
			
		}
	}
}
