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
package org.eclipse.debug.internal.ui.commands;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.viewers.AsynchronousSchedulingRuleFactory;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.debug.ui.commands.IBooleanStatusMonitor;
import org.eclipse.debug.ui.commands.IDebugCommand;
import org.eclipse.debug.ui.commands.IStatusMonitor;

/**
 * Common function for standard debug commands.
 * 
 * @since 3.3
 *
 */
public abstract class DebugCommand implements IDebugCommand {
	
	// debug flag
	public static boolean DEBUG_COMMANDS = false;
	
	static {
		DEBUG_COMMANDS = DebugUIPlugin.DEBUG && "true".equals( //$NON-NLS-1$
		 Platform.getDebugOption("org.eclipse.debug.ui/debug/commands")); //$NON-NLS-1$
	} 	

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.commands.IDebugCommand#performCapability(java.lang.Object, org.eclipse.debug.internal.ui.viewers.provisional.IStatusMonitor)
	 */
	public boolean execute(final Object element, final IStatusMonitor requestMonitor) {
		Job job = new Job("execute command") { //$NON-NLS-1$
			protected IStatus run(IProgressMonitor monitor) {
				if (DEBUG_COMMANDS) {
					System.out.println("execute: " + DebugCommand.this); //$NON-NLS-1$
				}
				Object target = getTarget(element);
				if (target != null) {
					try {
						doExecute(target, requestMonitor);
					} catch (CoreException e) {
						requestMonitor.setStatus(e.getStatus());
						if (DEBUG_COMMANDS) {
							System.out.println("\t" + e.getStatus().getMessage()); //$NON-NLS-1$
						}
					}
				} else {
					requestMonitor.setStatus(new Status(IStatus.ERROR, IDebugUIConstants.PLUGIN_ID, IDebugUIConstants.INTERNAL_ERROR, "element did not adapt to capability", //$NON-NLS-1$
							null));
				}
				requestMonitor.done();
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
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.commands.IDebugCommand#checkCapability(java.lang.Object, org.eclipse.debug.internal.ui.actions.provisional.IBooleanRequestMonitor)
	 */
	public void canExecute(final Object element, final IBooleanStatusMonitor requestMonitor) {
		Job job = new Job("check command") { //$NON-NLS-1$
			protected IStatus run(IProgressMonitor monitor) {
				if (DEBUG_COMMANDS) {
					System.out.print("can execute command: " + DebugCommand.this); //$NON-NLS-1$
				}
				Object target = getTarget(element);
				if (target != null) {
					try {
						boolean executable = isExecutable(target, requestMonitor);
						if (DEBUG_COMMANDS) {
							System.out.println(" >> " + executable); //$NON-NLS-1$
						}
						requestMonitor.setResult(executable);
					} catch (CoreException e) {
						requestMonitor.setStatus(e.getStatus());
						if (DEBUG_COMMANDS) {
							System.out.println(" >> ABORTED"); //$NON-NLS-1$
							System.out.println("\t" + e.getStatus().getMessage()); //$NON-NLS-1$
						}
					}
				} else {
					requestMonitor.setResult(false);
					if (DEBUG_COMMANDS) {
						System.out.println(" >> false (no adapter)"); //$NON-NLS-1$
					}
				}
				requestMonitor.done();
				return Status.OK_STATUS;
			}
		};
		job.setSystem(true);
		job.setRule(createUpdateSchedulingRule(element));
		job.schedule();
		
	}

	/**
	 * Executes the actual operation.
	 * 
	 * @param target object to perform on
	 * @param monitor progress monitor
	 */
	protected abstract void doExecute(Object target, IStatusMonitor monitor) throws CoreException;

	/**
	 * Returns whether this command is executable.
	 * 
	 * @param target object to check command for
	 * @param monitor progress monitor
	 * @return whether this command can be executed
	 */
	protected abstract boolean isExecutable(Object target, IStatusMonitor monitor) throws CoreException;
	
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
		return AsynchronousSchedulingRuleFactory.getDefault().newSerialPerObjectRule(element);
	}
}
