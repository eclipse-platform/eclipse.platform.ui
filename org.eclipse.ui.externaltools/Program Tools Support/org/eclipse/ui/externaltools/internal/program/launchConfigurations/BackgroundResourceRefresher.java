/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.externaltools.internal.program.launchConfigurations;


import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IDebugEventSetListener;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.ui.externaltools.internal.launchConfigurations.ExternalToolsUtil;
import org.eclipse.ui.externaltools.internal.model.ExternalToolsPlugin;
import org.eclipse.debug.ui.variables.ExpandVariableContext;

/**
 * Refreshes resources as specified by a lanunch configuration, when 
 * an associated process terminates.
 */
public class BackgroundResourceRefresher implements IDebugEventSetListener, Runnable, IRunnableWithProgress  {

	private ExpandVariableContext fContext;
	private ILaunchConfiguration fConfiguration;
	private IProcess fProcess;
	
	public BackgroundResourceRefresher(ILaunchConfiguration configuration, IProcess process, ExpandVariableContext context) {
		fConfiguration = configuration;
		fProcess = process;
		fContext = context;
	}
	
	/**
	 * If the process has already terminated, resource refreshing is done
	 * immediately in the current thread. Otherwise, refreshing is done when the
	 * process terminates.
	 */
	public void startBackgroundRefresh() {
		synchronized (fProcess) {
			if (fProcess.isTerminated()) {
				refresh();
			} else {
				DebugPlugin.getDefault().addDebugEventListener(this);
			}
		}
	}
	
	/**
	 * @see org.eclipse.debug.core.IDebugEventSetListener#handleDebugEvents(org.eclipse.debug.core.DebugEvent)
	 */
	public void handleDebugEvents(DebugEvent[] events) {
		for (int i = 0; i < events.length; i++) {
			DebugEvent event = events[i];
			if (event.getSource() == fProcess && event.getKind() == DebugEvent.TERMINATE) {
				DebugPlugin.getDefault().removeDebugEventListener(this);
				refresh();
				break;
			}
		}
	}
	
	/**
	 * Submits a runnable to do the refresh
	 */
	protected void refresh() {
		ExternalToolsPlugin.getStandardDisplay().asyncExec(this);
	}
	
	/** 
	 * Creates a dialog to run the refresh
	 * 
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		ProgressMonitorDialog dialog = new ProgressMonitorDialog(ExternalToolsPlugin.getStandardDisplay().getActiveShell());
		try {
			dialog.run(true, true, this);
		} catch (InvocationTargetException e) {
			// report the exception
		} catch (InterruptedException e) {
		}
	}
	/**
	 * Peforms the refresh
	 * 
	 * @see org.eclipse.jface.operation.IRunnableWithProgress#run(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
		try {
			ExternalToolsUtil.refreshResources(fConfiguration, fContext, monitor);
		} catch (CoreException e) {
			throw new InvocationTargetException(e);
		}				
	}

}
