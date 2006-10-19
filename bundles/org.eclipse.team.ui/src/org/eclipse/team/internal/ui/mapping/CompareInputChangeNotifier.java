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
package org.eclipse.team.internal.ui.mapping;

import java.util.*;

import org.eclipse.compare.structuremergeviewer.ICompareInput;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.swt.widgets.Display;
import org.eclipse.team.core.*;
import org.eclipse.team.core.diff.IDiffChangeListener;
import org.eclipse.team.core.diff.IDiffTree;
import org.eclipse.team.core.mapping.ISynchronizationContext;
import org.eclipse.team.internal.core.BackgroundEventHandler;
import org.eclipse.team.internal.core.BackgroundEventHandler.Event;
import org.eclipse.team.internal.ui.Policy;

/**
 * An abstract class that 
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
		IResourceChangeListener, IDiffChangeListener {

	private ISynchronizationContext context;
	private Map inputs = new HashMap();
	private InputChangeEventHandler eventHandler;

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
	
	private static final int COMPARE_INPUT_CHANGE = 1;
	
	private static class InputChangeEvent extends Event {
		private final ICompareInput[] inputs;
		public InputChangeEvent(ICompareInput[] inputs) {
			super(COMPARE_INPUT_CHANGE);
			this.inputs = inputs;
			
		}
		public ICompareInput[] getChangedInputs() {
			return inputs;
		}
	}
	
	private class InputChangeEventHandler extends BackgroundEventHandler {

		private final Set changedInputs = new HashSet();
		private final List pendingRunnables = new ArrayList();
		
		protected InputChangeEventHandler() {
			super("Updating Compare Editor", "Error updating compare editor");
		}

		protected boolean doDispatchEvents(IProgressMonitor monitor)
				throws TeamException {
			ICompareInput[] toDispatch;
			RunnableEvent[] events;
			synchronized (pendingRunnables) {
				synchronized (changedInputs) {
					if (changedInputs.isEmpty() && pendingRunnables.isEmpty())
						return false;
					toDispatch = (ICompareInput[]) changedInputs.toArray(new ICompareInput[changedInputs.size()]);
					events = (RunnableEvent[]) pendingRunnables.toArray(new RunnableEvent[pendingRunnables.size()]);
					changedInputs.clear();
					pendingRunnables.clear();
				}
			}
			dispatchChanges(toDispatch, monitor);
			for (int i = 0; i < events.length; i++) {
				RunnableEvent event = events[i];
				executeRunnableNow(event, monitor);
			}
			return true;
		}

		protected void processEvent(Event event, IProgressMonitor monitor)
				throws CoreException {
			int type = event.getType();
			switch (type) {
				case BackgroundEventHandler.RUNNABLE_EVENT :
					RunnableEvent runnableEvent = ((RunnableEvent)event);
					if (runnableEvent.isPreemtive())
						executeRunnableNow(event, monitor);
					else
						executeRunnableDuringDispatch(event);
					break;
				case COMPARE_INPUT_CHANGE :
					if (event instanceof InputChangeEvent) {
						InputChangeEvent changeEvent = (InputChangeEvent) event;
						ICompareInput[] inputs = changeEvent.getChangedInputs();
						synchronized (changedInputs) {
							for (int i = 0; i < inputs.length; i++) {
								ICompareInput input = inputs[i];
								changedInputs.add(input);
							}
						}
					}
					break;
			}
		}
		
		private void executeRunnableDuringDispatch(Event event) {
			synchronized (pendingRunnables) {
				pendingRunnables.add(event);
			}
		}

		private void executeRunnableNow(Event event, IProgressMonitor monitor) {
			try {
				// Dispatch any queued results to clear pending output events
				dispatchEvents(Policy.subMonitorFor(monitor, 1));
			} catch (TeamException e) {
				handleException(e);
			}
			try {
				((RunnableEvent)event).run(Policy.subMonitorFor(monitor, 1));
			} catch (CoreException e) {
				handleException(e);
			}
		}
		
		protected synchronized void queueEvent(Event event) {
			super.queueEvent(event, false);
		}
		
		protected long getShortDispatchDelay() {
			// Only wait 250 for additional changes to come in
			return 250;
		}
		
		protected Object getJobFamiliy() {
			return getContext();
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
		eventHandler = new InputChangeEventHandler();
	}
	
	/**
	 * Dispose of the change notifier. This method is invoked when the context
	 * to which the change notifier is associated is disposed.
	 * Subclasses may extend this method.
	 */
	protected void handleDispose() {
		ResourcesPlugin.getWorkspace().removeResourceChangeListener(this);
		context.getDiffTree().removeDiffChangeListener(this);
		eventHandler.shutdown();
	}

	/**
	 * Connect the input to this change notifier. Once connected, the change notifier will issue 
	 * change events for the given input. When change notification is no longer desired, the 
	 * input should be disconnected. The number of calls to {@link #connect(ICompareInput)} needs to
	 * be matched by the same number of calls to {@link #disconnect(ICompareInput)}.
	 * @param input the compare input
	 */
	public void connect(ICompareInput input) {
		CompareInputConnecton con = (CompareInputConnecton)inputs.get(input);
		if (con == null) {
			con = new CompareInputConnecton(input);
			inputs.put(input, con);
		}
		con.increment();
	}

	/**
	 * Disconnect the input from this change notifier.
	 * @param input the compare input
	 * @see #connect(ICompareInput)
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
	 * Send out notification that the given compare inputs have changed.
	 * @param inputs the changed inputs
	 */
	protected void inputsChanged(ICompareInput[] inputs) {
		InputChangeEvent event = new InputChangeEvent(inputs);
		eventHandler.queueEvent(event);
	}
	
	/**
	 * Dispatch the changes to the given inputs.
	 * @param inputs the changed compare inputs
	 * @param monitor a progress monitor
	 */
	protected void dispatchChanges(final ICompareInput[] inputs, IProgressMonitor monitor) {
		prepareInputs(inputs, monitor);
		Display.getDefault().syncExec(new Runnable() {
			public void run() {
				fireChanges(inputs);
			}
		});
	}

	/**
	 * Prepare the inputs in the background before firing the compare input change event.
	 * This allows for the caching of contents etc. before the input change event is fired.
	 * @param inputs the changed inputs
	 * @param monitor a progress monitor
	 */
	protected void prepareInputs(ICompareInput[] inputs, IProgressMonitor monitor) {
		monitor.beginTask(null, inputs.length * 100);
		for (int i = 0; i < inputs.length; i++) {
			ICompareInput input = inputs[i];
			prepareInput(input, Policy.subMonitorFor(monitor, 100));
		}
		monitor.done();
	}

	/**
	 * Prepare the input before firing the compare input change event.
	 * This allows for the caching of contents etc. before the input change event is fired.
	 * This method is called from {@link #prepareInputs(ICompareInput[], IProgressMonitor)}
	 * for each input. By default, nothing is done, subclasses may override.
	 * @param input the compare input
	 * @param monitor a progress monitor
	 */
	protected void prepareInput(ICompareInput input, IProgressMonitor monitor) {
		// Default is to do nothing
	}
	
	/**
	 * Update the compare inputs and fire the change events.
	 * This method is called from the UI thread after the inputs have
	 * been prepared in a background thread
	 * (see {@link #prepareInputs(ICompareInput[], IProgressMonitor)})
	 * @param inputs the changed inputs
	 */
	protected void fireChanges(ICompareInput[] inputs) {
		for (int i = 0; i < inputs.length; i++) {
			ICompareInput input = inputs[i];
			fireChange(input);
		}
	}

	/**
	 * Update the compare input and fire the change event.
	 * This method is called from {@link #fireChanges(ICompareInput[])}
	 * for each changed input.
	 * @param input the changed compare input
	 */
	protected abstract void fireChange(ICompareInput input);
	
	/**
	 * Run the given runnable in the background.
	 * @param runnable the runnable
	 */
	protected void runInBackground(IWorkspaceRunnable runnable) {
		eventHandler.queueEvent(new BackgroundEventHandler.RunnableEvent(runnable, false));
	}

}
