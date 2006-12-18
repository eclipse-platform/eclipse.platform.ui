/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.core.commands;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.commands.IBooleanCollector;
import org.eclipse.debug.core.commands.IDebugCommand;
import org.eclipse.debug.core.commands.IStatusCollector;
import org.eclipse.debug.internal.core.DebugOptions;

/**
 * Common function for standard debug commands.
 * 
 * @since 3.3
 *
 */
public abstract class DebugCommand implements IDebugCommand {
	
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
   
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.ui.commands.IDebugCommand#performCapability(java.lang.Object,
	 *      org.eclipse.debug.internal.ui.viewers.provisional.IStatusMonitor)
	 */
	public boolean execute(final Object element, final IProgressMonitor monitor, final IStatusCollector collector) {
		Job job = new Job(getExecuteTaskName()) {
			protected IStatus run(IProgressMonitor pm) {
				if (DebugOptions.DEBUG_COMMANDS) {
					System.out.println("execute: " + DebugCommand.this); //$NON-NLS-1$
				}
				Object target = getTarget(element);
				if (target != null) {
					try {
						pm.beginTask(getExecuteTaskName(), 1);
						monitor.beginTask(getExecuteTaskName(), 1);
						doExecute(target, monitor, collector);
						monitor.worked(1);
						pm.worked(1);
					} catch (CoreException e) {
						collector.setStatus(e.getStatus());
						if (DebugOptions.DEBUG_COMMANDS) {
							System.out.println("\t" + e.getStatus().getMessage()); //$NON-NLS-1$
						}
					}
				}
				collector.done();
				pm.setCanceled(monitor.isCanceled());
				monitor.done();
				pm.done();
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
	
	public void canExecute(final Object element, final IProgressMonitor monitor, final IBooleanCollector collector) {
		Job job = new Job(getEnablementTaskName()) {
			protected IStatus run(IProgressMonitor pm) {
				if (DebugOptions.DEBUG_COMMANDS) {
					System.out.print("can execute command: " + DebugCommand.this); //$NON-NLS-1$
				}
				Object target = getTarget(element);
				if (target != null) {
					pm.beginTask(getEnablementTaskName(), 1);
					monitor.beginTask(getEnablementTaskName(), 1);
					try {
						boolean executable = isExecutable(target, monitor, collector);
						if (DebugOptions.DEBUG_COMMANDS) {
							System.out.println(" >> " + executable); //$NON-NLS-1$
						}
						collector.setResult(executable);
						monitor.worked(1);
						pm.worked(1);
					} catch (CoreException e) {
						collector.setStatus(e.getStatus());
						if (DebugOptions.DEBUG_COMMANDS) {
							System.out.println(" >> ABORTED"); //$NON-NLS-1$
							System.out.println("\t" + e.getStatus().getMessage()); //$NON-NLS-1$
						}
					}
				} else {
					collector.setResult(false);
					if (DebugOptions.DEBUG_COMMANDS) {
						System.out.println(" >> false (no adapter)"); //$NON-NLS-1$
					}
				}
				collector.done();
				pm.setCanceled(monitor.isCanceled());
				monitor.done();
				pm.done();
				return Status.OK_STATUS;
			}
		};
		job.setSystem(true);
		job.setRule(createUpdateSchedulingRule(element));
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
	 * @param target object to perform on
	 * @param monitor progress monitor
	 */
	protected abstract void doExecute(Object target, IProgressMonitor monitor, IStatusCollector collector) throws CoreException;

	/**
	 * Returns whether this command is executable.
	 * 
	 * @param target object to check command for
	 * @param monitor progress monitor
	 * @return whether this command can be executed
	 */
	protected abstract boolean isExecutable(Object target, IProgressMonitor monitor, IBooleanCollector collector) throws CoreException;
	
	/**
	 * Returns the appropriate command adapter from the given object.
	 * 
	 * @param element object to obtain adapter from
	 * @return adapter
	 */
	protected abstract Object getTarget(Object element); 
	
	/**
	 * Scheduling rule for checking capability.
	 * 
	 * @return scheduling rule or <code>null</code>
	 */
	protected ISchedulingRule createUpdateSchedulingRule(Object element) {
		return new SerialPerObjectRule(element);
	}
}
