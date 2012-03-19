/*******************************************************************************
 * Copyright (c) 2005, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.viewers.provisional;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelChangedListener;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelDelta;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelProxy2;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;
import org.eclipse.debug.internal.ui.viewers.model.provisional.ITreeModelViewer;
import org.eclipse.jface.viewers.Viewer;

/**
 * Common function for a model proxy.
 * <p>
 * Clients may subclass this class.
 * </p>
 * @since 3.2
 */
public abstract class AbstractModelProxy implements IModelProxy2 {
	
	private IPresentationContext fContext;
	private boolean fInstalled = false;
	private ITreeModelViewer fViewer;
	private boolean fDisposed = false;
	private Job fInstallJob;
	
	
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
	public void fireModelChanged(IModelDelta delta) {
	    synchronized(this) {
	        if (!fInstalled || fDisposed) return;
	    }
	    
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
	    if (fInstallJob != null) {
	        fInstallJob.cancel();
	        fInstallJob = null;
	    }
		fDisposed = true;
		fContext = null;
		fViewer = null;
	}

	protected synchronized void setInstalled(boolean installed) {
	    fInstalled = installed;
	}
	
	protected synchronized boolean isInstalled() {
	    return fInstalled;
	}
	
	protected synchronized void setDisposed(boolean disposed) {
	    fDisposed = disposed;
	}
	
	public void initialize(ITreeModelViewer viewer) {
        setDisposed(false);
        
        synchronized(this) {
    	    fViewer = viewer;
    	    fContext = viewer.getPresentationContext();
            fInstallJob = new Job("Model Proxy installed notification job") {//$NON-NLS-1$
                protected IStatus run(IProgressMonitor monitor) {
                    synchronized(this) {
                        fInstallJob = null;
                    }
                    if (!monitor.isCanceled()) {
                        init(getTreeModelViewer().getPresentationContext());
                        setInstalled(true);
                        installed(getViewer());
                    }
                    return Status.OK_STATUS;
                }
    
                /*
                 * (non-Javadoc)
                 * 
                 * @see org.eclipse.core.runtime.jobs.Job#shouldRun()
                 */
                public boolean shouldRun() {
                    return !isDisposed();
                }
            };
            fInstallJob.setSystem(true);
        }
        fInstallJob.schedule();
	}
	
	/**
	 * Returns the context this model proxy is installed in.
	 * 
	 * @return presentation context, or <code>null</code> if this
	 *  model proxy has been disposed
	 */
	public synchronized IPresentationContext getPresentationContext() {
		return fContext;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.IModelProxy#init(org.eclipse.debug.internal.ui.viewers.IPresentationContext)
	 */
	public void init(IPresentationContext context) {
	}

	/* (non-Javadoc)
	 * 
	 * Subclasses should override as required.
	 * 
	 * @see org.eclipse.debug.internal.ui.viewers.provisional.IModelProxy#installed(org.eclipse.jface.viewers.Viewer)
	 */
	public void installed(Viewer viewer) {	
	}
	
	/**
	 * Returns the viewer this proxy is installed in.
	 * 
	 * @return viewer or <code>null</code> if not installed
	 */
	protected Viewer getViewer() {
		return (Viewer)fViewer;
	}

    /**
     * Returns the viewer this proxy is installed in.
     * 
     * @return viewer or <code>null</code> if not installed
     */
    protected ITreeModelViewer getTreeModelViewer() {
        return fViewer;
    }
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.model.provisional.IModelProxy#isDisposed()
	 */
	public synchronized boolean isDisposed() {
		return fDisposed;
	}	

}
