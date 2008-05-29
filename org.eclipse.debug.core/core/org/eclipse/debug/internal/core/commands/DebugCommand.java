/*******************************************************************************
 * Copyright (c) 2006, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.core.commands;

import java.util.LinkedHashSet;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IRequest;
import org.eclipse.debug.core.commands.IDebugCommandHandler;
import org.eclipse.debug.core.commands.IDebugCommandRequest;
import org.eclipse.debug.core.commands.IEnabledStateRequest;
import org.eclipse.debug.internal.core.DebugOptions;

/**
 * Common function for standard debug commands.
 * 
 * @since 3.3
 *
 */
public abstract class DebugCommand implements IDebugCommandHandler {
	
	/**
	 * Scheduling rule to serialize commands on an object
	 */
   class SerialPerObjectRule implements ISchedulingRule {

		private Object fObject = null;

		public SerialPerObjectRule(Object lock) {
			fObject = lock;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.core.runtime.jobs.ISchedulingRule#contains(org.eclipse.core.runtime.jobs.ISchedulingRule)
		 */
		public boolean contains(ISchedulingRule rule) {
			return rule == this;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.core.runtime.jobs.ISchedulingRule#isConflicting(org.eclipse.core.runtime.jobs.ISchedulingRule)
		 */
		public boolean isConflicting(ISchedulingRule rule) {
			if (rule instanceof SerialPerObjectRule) {
				SerialPerObjectRule vup = (SerialPerObjectRule) rule;
				return fObject == vup.fObject;
			}
			return false;
		}

	}
   
	public boolean execute(final IDebugCommandRequest request) {
		Job job = new Job(getExecuteTaskName()) {
			protected IStatus run(IProgressMonitor monitor) {
				if (DebugOptions.DEBUG_COMMANDS) {
					System.out.println("execute: " + DebugCommand.this); //$NON-NLS-1$
				}
				Object[] elements = request.getElements();
				Object[] targets = new Object[elements.length];
				for (int i = 0; i < elements.length; i++) {
					targets[i]= getTarget(elements[i]);
				}
				targets = coalesce(targets);
				monitor.beginTask(getExecuteTaskName(), targets.length);
				try {
					doExecute(targets, monitor, request);
				} catch (CoreException e) {
					request.setStatus(e.getStatus());
					if (DebugOptions.DEBUG_COMMANDS) {
						System.out.println("\t" + e.getStatus().getMessage()); //$NON-NLS-1$
					}
				}
				request.done();
				monitor.setCanceled(request.isCanceled());
				monitor.done();
				return Status.OK_STATUS;
			}
		};
		job.setSystem(true);
		job.schedule();
		return isRemainEnabled();
	}	
	
	/**
	 * Returns whether this command should remain enabled after execution is invoked.
	 * 
	 * @return whether to remain enabled
	 */
	protected boolean isRemainEnabled() {
		return false;
	}
	
	public void canExecute(final IEnabledStateRequest request) {
		Job job = new Job(getEnablementTaskName()) {
			protected IStatus run(IProgressMonitor monitor) {
				if (DebugOptions.DEBUG_COMMANDS) {
					System.out.print("can execute command: " + DebugCommand.this); //$NON-NLS-1$
				}
				Object[] elements = request.getElements();
				Object[] targets = new Object[elements.length];
				for (int i = 0; i < elements.length; i++) {
					targets[i] = getTarget(elements[i]);
					if (targets[i] == null) {
						request.setEnabled(false);
						request.cancel();
						if (DebugOptions.DEBUG_COMMANDS) {
							System.out.println(" >> false (no adapter)"); //$NON-NLS-1$
						}
					}
				}
				if (!request.isCanceled()) {
					targets = coalesce(targets);
					monitor.beginTask(getEnablementTaskName(), targets.length);
					try {
						boolean executable = isExecutable(targets, monitor, request);
						if (DebugOptions.DEBUG_COMMANDS) {
							System.out.println(" >> " + executable); //$NON-NLS-1$
						}
						request.setEnabled(executable);
					} catch (CoreException e) {
						request.setStatus(e.getStatus());
						request.setEnabled(false);
						if (DebugOptions.DEBUG_COMMANDS) {
							System.out.println(" >> ABORTED"); //$NON-NLS-1$
							System.out.println("\t" + e.getStatus().getMessage()); //$NON-NLS-1$
						}
					}
				}
				monitor.setCanceled(request.isCanceled());
				request.done();
				monitor.done();
				return Status.OK_STATUS;
			}
		};
		job.setSystem(true);
		job.setRule(createUpdateSchedulingRule(request));
		job.schedule();
		
	}
	
	/**
	 * Returns the name to use for jobs and progress monitor task names when checking
	 * enabled state.
	 * 
	 * @return task name
	 */
	protected String getEnablementTaskName() {
		// this is a system job name and does not need to be NLS'd
		return "Check Debug Command"; //$NON-NLS-1$
	}
	
	/**
	 * Returns the name to use for jobs and progress monitor task names when executing
	 * a debug command
	 * 
	 * @return task name
	 */
	protected String getExecuteTaskName() {
		// this is a system job name and does not need to be NLS'd
		return "Execute Debug Command"; //$NON-NLS-1$
	}	

	/**
	 * Executes the actual operation.
	 * 
	 * @param targets objects to perform on
	 * @param request request
	 */
	protected abstract void doExecute(Object[] targets, IProgressMonitor monitor, IRequest request) throws CoreException;

	/**
	 * Returns whether this command is executable.
	 * 
	 * @param targets objects to check command for
	 * @param monitor progress monitor
	 * @param request request
	 * @return whether this command can be executed
	 */
	protected abstract boolean isExecutable(Object[] targets, IProgressMonitor monitor, IEnabledStateRequest request) throws CoreException;
	
	/**
	 * Returns the appropriate command adapter from the given object.
	 * 
	 * @param element object to obtain adapter from
	 * @return adapter
	 */
	protected abstract Object getTarget(Object element); 
	
	/**
	 * Returns an adapter of the specified type for the given object or <code>null</code>
	 * if none. The object itself is returned if it is an instance of the specified type.
	 * 
	 * @param element element to retrieve adapter for
	 * @param type adapter type
	 * @return adapter or <code>null</code>
	 */
	protected Object getAdapter(Object element, Class type) {
    	return DebugPlugin.getAdapter(element, type);	
	}	
	
	/**
	 * Scheduling rule for updating command enabled state.
	 * 
	 * @return scheduling rule or <code>null</code>
	 */
	protected ISchedulingRule createUpdateSchedulingRule(IDebugCommandRequest request) {
		return new SerialPerObjectRule(request.getElements()[0]);
	}
	
	private Object[] coalesce(Object[] objects) {
		if (objects.length == 1) {
			return objects;
		} else {
			LinkedHashSet set = new LinkedHashSet(objects.length);
			for (int i = 0; i < objects.length; i++) {
				set.add(objects[i]);
			}
			return set.toArray();
		}
	}
}
