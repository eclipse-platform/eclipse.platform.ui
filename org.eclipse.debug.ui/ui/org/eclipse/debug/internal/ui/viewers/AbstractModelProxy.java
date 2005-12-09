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
package org.eclipse.debug.internal.ui.viewers;

import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.Platform;
import org.eclipse.debug.internal.ui.DebugUIPlugin;

/**
 * Common function for model proxies.
 * <p>
 * Clients may subclass this class.
 * </p>
 * @since 3.2
 */
public abstract class AbstractModelProxy implements IModelProxy {
	
	private IPresentationContext fContext;

	private ListenerList fListeners = new ListenerList();

	protected Object[] getListeners() {
		synchronized (fListeners) {
			return fListeners.getListeners();
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.IModelProxy#addModelChangedListener(org.eclipse.debug.internal.ui.viewers.IModelChangedListener)
	 */
	public void addModelChangedListener(IModelChangedListener listener) {
		synchronized (fListeners) {
			fListeners.add(listener);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.IModelProxy#removeModelChangedListener(org.eclipse.debug.internal.ui.viewers.IModelChangedListener)
	 */
	public void removeModelChangedListener(IModelChangedListener listener) {
		synchronized (fListeners) {
			fListeners.remove(listener);
		}
	}

	/**
	 * Notifies registered listeners of the given delta.
	 * 
	 * @param delta model delta to broadcast
	 */
	public void fireModelChanged(final IModelDelta delta) {
		Object[] listeners = getListeners();
		for (int i = 0; i < listeners.length; i++) {
			final IModelChangedListener listener = (IModelChangedListener) listeners[i];
			ISafeRunnable safeRunnable = new ISafeRunnable() {
				public void handleException(Throwable exception) {
					DebugUIPlugin.log(exception);
				}

				public void run() throws Exception {
					listener.modelChanged(delta);
				}

			};
			Platform.run(safeRunnable);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.IModelProxy#dispose()
	 */
	public synchronized void dispose() {
		fContext = null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.IModelProxy#init(org.eclipse.debug.internal.ui.viewers.IPresentationContext)
	 */
	public void init(IPresentationContext context) {
		fContext = context;
	}
	
	/**
	 * Returns the context this model proxy is installed in.
	 * 
	 * @return presentation context, or <code>null</code> if this
	 *  model proxy has been disposed
	 */
	protected IPresentationContext getPresentationContext() {
		return fContext;
	}

	/* (non-Javadoc)
	 * Subclasses should override as required.
	 * 
	 * @see org.eclipse.debug.internal.ui.viewers.IModelProxy#installed()
	 */
	public void installed() {	
	}
	
	
	

}
