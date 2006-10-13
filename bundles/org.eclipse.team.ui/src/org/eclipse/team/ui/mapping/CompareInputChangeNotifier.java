/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.ui.mapping;

import java.util.*;

import org.eclipse.compare.structuremergeviewer.ICompareInput;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.team.core.ICache;
import org.eclipse.team.core.ICacheListener;
import org.eclipse.team.core.diff.IDiffChangeListener;
import org.eclipse.team.core.diff.IDiffTree;
import org.eclipse.team.core.mapping.ISynchronizationContext;
import org.eclipse.team.internal.ui.mapping.CompareInputChangeEvent;

/**
 * An abstract implementation of {@link ICompareInputChangeNotifier} that 
 * listens to resource changes and synchronization context changes.
 * <p>
 * This class can be subclassed by clients.
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is a guarantee neither that this API will
 * work nor that it will remain the same. Please do not use this API without
 * consulting with the Platform/Team team.
 * </p>
 * @since 3.3
 */
public abstract class CompareInputChangeNotifier implements
		ICompareInputChangeNotifier, IResourceChangeListener, IDiffChangeListener {

	private ISynchronizationContext context;
	private ListenerList listeners = new ListenerList(ListenerList.IDENTITY);
	private Map inputs = new HashMap();

	private class CompareInputConnecton {
		private ICompareInput input;
		private int connections;
		public CompareInputConnecton(ICompareInput input) {
			this.input = input;
		}
		public void increment() {
			connections++;
		}
		public void decrement() {
			if (connections > 0)
				connections--;
			
		}
		public boolean isDisconnected() {
			return connections == 0;
		}
		public ICompareInput getInput() {
			return input;
		}
	}
	
	/**
	 * Create a change notifier for the given synchronization context.
	 * @param context the synchronization context.
	 */
	public CompareInputChangeNotifier(ISynchronizationContext context) {
		super();
		initialize(context);
	}

	/**
	 * Initialize the change notifier. This method is called from the
	 * constructor and registers a listener with the workspace and the
	 * synchronization context. It also registers a listener with the context
	 * cache which will unregister the listeners when the context is disposed.
	 * Subclasses may extend this method.
	 * 
	 * @param context the synchronization context
	 */
	protected void initialize(ISynchronizationContext context) {
		this.context = context;
		ResourcesPlugin.getWorkspace().addResourceChangeListener(this, IResourceChangeEvent.POST_CHANGE);
		context.getDiffTree().addDiffChangeListener(this);
		context.getCache().addCacheListener(new ICacheListener() {
			public void cacheDisposed(ICache cache) {
				handleDispose();
			}
		});
	}
	
	/**
	 * Dispose of the change notifier. This method is invoked when the context
	 * to which the change notifier is associated is disposed.
	 * Subclasses may extend this method.
	 */
	protected void handleDispose() {
		ResourcesPlugin.getWorkspace().removeResourceChangeListener(this);
		context.getDiffTree().removeDiffChangeListener(this);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.mapping.ISynchronizationCompareInputChangeNotifier#addChangeListener(org.eclipse.team.ui.mapping.ISynchronizationCompareInputChangeListener)
	 */
	public void addChangeListener(
			ICompareInputChangeListener listener) {
		listeners.add(listener);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.mapping.ISynchronizationCompareInputChangeNotifier#removeChangeListener(org.eclipse.team.ui.mapping.ISynchronizationCompareInputChangeListener)
	 */
	public void removeChangeListener(
			ICompareInputChangeListener listener) {
		listeners.remove(listener);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.mapping.ISynchronizationCompareInputChangeNotifier#connect(org.eclipse.compare.structuremergeviewer.ICompareInput)
	 */
	public void connect(ICompareInput input) {
		CompareInputConnecton con = (CompareInputConnecton)inputs.get(input);
		if (con == null) {
			con = new CompareInputConnecton(input);
			inputs.put(input, con);
		}
		con.increment();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.mapping.ISynchronizationCompareInputChangeNotifier#disconnect(org.eclipse.compare.structuremergeviewer.ICompareInput)
	 */
	public void disconnect(ICompareInput input) {
		CompareInputConnecton con = (CompareInputConnecton)inputs.get(input);
		if (con != null) {
			con.decrement();
			if (con.isDisconnected()) {
				inputs.remove(input);
			}
		}
	}
	
	/**
	 * Return the array of inputs that have connections.
	 * @return the array of inputs that have connections
	 */
	protected ICompareInput[] getConnectedInputs() {
		return (ICompareInput[])inputs.keySet().toArray(new ICompareInput[inputs.size()]);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.core.diff.IDiffChangeListener#propertyChanged(org.eclipse.team.core.diff.IDiffTree, int, org.eclipse.core.runtime.IPath[])
	 */
	public void propertyChanged(IDiffTree tree, int property, IPath[] paths) {
		// Property changes are not interesting w.r.t. state changes
	}

	/**
	 * Return the synchronization context to which this notifier is associated.
	 * @return the synchronization context to which this notifier is associated
	 */
	public final ISynchronizationContext getContext() {
		return context;
	}
	
	/**
	 * Handle the input changes by notifying any listeners of the changed inputs.
	 * @param inputs the changed inputs
	 */
	protected void handleInputChanges(ICompareInput[] inputs) {
		Set inSync = new HashSet();
		Set changed = new HashSet();
		for (int i = 0; i < inputs.length; i++) {
			ICompareInput input = inputs[i];
			if (isInSync(input)) {
				inSync.add(input);
			} else {
				changed.add(input);
			}
		}
		ICompareInputChangeEvent event = new CompareInputChangeEvent(inSync, changed);
		fireEvent(event);
	}

	private void fireEvent(final ICompareInputChangeEvent event) {
		Object[] allListeners = listeners.getListeners();
		for (int i = 0; i < allListeners.length; i++) {
			final ICompareInputChangeListener listener = (ICompareInputChangeListener)allListeners[i];
			SafeRunner.run(new SafeRunnable() {
				public void run() throws Exception {
					listener.compareInputsChanged(event);
				}
			});
		}
	}

	/**
	 * Return whether the given input is in-sync. This method is 
	 * called from {@link #handleInputChanges(ICompareInput[])}
	 * to differentiate changed inputs from those that are
	 * no longer relevant.
	 * @param input a compare input
	 * @return whether the given input is in-sync
	 */
	protected abstract boolean isInSync(ICompareInput input);

}
