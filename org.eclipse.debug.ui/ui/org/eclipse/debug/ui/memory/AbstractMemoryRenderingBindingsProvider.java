/*******************************************************************************
 * Copyright (c) 2004, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.ui.memory;

import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.debug.internal.ui.DebugUIPlugin;

/**
 * Common function for a dynamic memory rendering bindings provider.
 * <p>
 * Clients may subclass this class.
 * </p>
 * @see org.eclipse.debug.ui.memory.IMemoryRenderingBindingsProvider
 * @since 3.1
 */
public abstract class AbstractMemoryRenderingBindingsProvider implements IMemoryRenderingBindingsProvider {
    	
	// list of binding listeners
	private ListenerList fListeners;
        
	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.memory.IMemoryRenderingBindingsProvider#addListener(org.eclipse.debug.ui.memory.IMemoryRenderingBindingsListener)
	 */
	public void addListener(IMemoryRenderingBindingsListener listener) {
		if (fListeners == null) {
			fListeners = new ListenerList();
		}
		fListeners.add(listener);
	}
	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.memory.IMemoryRenderingBindingsProvider#removeListener(org.eclipse.debug.ui.memory.IMemoryRenderingBindingsListener)
	 */
	public void removeListener(IMemoryRenderingBindingsListener listener) {
		if (fListeners != null) {
			fListeners.remove(listener);
		}
	}
	
	/**
	 * Notifies all registered listeners that bindings have changed.
	 */
	protected void fireBindingsChanged() {
		if (fListeners == null) {
			return;
		}
		
		Object[] listeners = fListeners.getListeners();
		
		for (int i=0; i<listeners.length; i++) {
			if (listeners[i] instanceof IMemoryRenderingBindingsListener) {
				final IMemoryRenderingBindingsListener listener = (IMemoryRenderingBindingsListener)listeners[i];
				ISafeRunnable runnable = new ISafeRunnable () {
					public void handleException(Throwable exception) {
						DebugUIPlugin.log(exception);
					}
					public void run() throws Exception {
						listener.memoryRenderingBindingsChanged();
					}};
				SafeRunner.run(runnable);
			}
		}
	}
}
