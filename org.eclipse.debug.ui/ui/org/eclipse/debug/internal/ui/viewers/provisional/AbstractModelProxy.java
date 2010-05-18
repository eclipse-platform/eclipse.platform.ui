/*******************************************************************************
 * Copyright (c) 2005, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.viewers.provisional;

import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelChangedListener;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelDelta;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelProxy;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;
import org.eclipse.jface.viewers.Viewer;

/**
 * Common function for a model proxy.
 * <p>
 * Clients may subclass this class.
 * </p>
 * @since 3.2
 */
public abstract class AbstractModelProxy implements IModelProxy {
	
	private IPresentationContext fContext;
	private Viewer fViewer;
	private boolean fDisposed = false;

	private ListenerList fListeners = new ListenerList();
	
	// debug flags
	public static boolean DEBUG_DELTAS = false;
	
	static {
		DEBUG_DELTAS = DebugUIPlugin.DEBUG && "true".equals( //$NON-NLS-1$
		 Platform.getDebugOption("org.eclipse.debug.ui/debug/viewers/deltas")); //$NON-NLS-1$
	} 	

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
	public void fireModelChanged(IModelDelta delta) {
		final IModelDelta root = getRootDelta(delta);
		Object[] listeners = getListeners();
		for (int i = 0; i < listeners.length; i++) {
			final IModelChangedListener listener = (IModelChangedListener) listeners[i];
			ISafeRunnable safeRunnable = new ISafeRunnable() {
				public void handleException(Throwable exception) {
					DebugUIPlugin.log(exception);
				}

				public void run() throws Exception {
					listener.modelChanged(root, AbstractModelProxy.this);
				}

			};
            SafeRunner.run(safeRunnable);
		}
	}
	
	/**
	 * Returns the root node of the given delta.
	 * 
	 * @param delta delta node
	 * @return returns the root of the given delta
	 */
	protected IModelDelta getRootDelta(IModelDelta delta) {
		IModelDelta parent = delta.getParentDelta();
		while (parent != null) {
			delta = parent;
			parent = delta.getParentDelta();
		}
		return delta;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.IModelProxy#dispose()
	 */
	public synchronized void dispose() {
		fDisposed = true;
		fContext = null;
		fViewer = null;
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
	public IPresentationContext getPresentationContext() {
		return fContext;
	}

	/* (non-Javadoc)
	 * 
	 * Subclasses should override as required.
	 * 
	 * @see org.eclipse.debug.internal.ui.viewers.provisional.IModelProxy#installed(org.eclipse.jface.viewers.Viewer)
	 */
	public void installed(Viewer viewer) {	
		fViewer = viewer;
	}
	
	/**
	 * Returns the viewer this proxy is installed in.
	 * 
	 * @return viewer or <code>null</code> if not installed
	 */
	protected Viewer getViewer() {
		return fViewer;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.model.provisional.IModelProxy#isDisposed()
	 */
	public synchronized boolean isDisposed() {
		return fDisposed;
	}	

}
