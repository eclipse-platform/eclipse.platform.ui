package org.eclipse.ui.externaltools.internal.ant.view.actions;
/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp.  All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
**********************************************************************/
import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.externaltools.internal.ant.launchConfigurations.AntLaunchShortcut;
import org.eclipse.ui.externaltools.internal.ant.model.AntUtil;
import org.eclipse.ui.externaltools.internal.ant.view.AntView;
import org.eclipse.ui.externaltools.internal.ant.view.elements.TargetNode;
import org.eclipse.ui.externaltools.internal.model.ExternalToolsImages;
import org.eclipse.ui.externaltools.internal.model.ExternalToolsPlugin;
import org.eclipse.ui.externaltools.internal.model.IExternalToolConstants;
import org.eclipse.ui.externaltools.internal.ui.IExternalToolsUIConstants;
import org.eclipse.ui.texteditor.IUpdate;

/**
 * Action which runs the active targets in an AntView.
 */
public class RunActiveTargetsAction extends Action implements IUpdate {

	private AntView view;
	private static final int TOTAL_WORK_UNITS = 100;

	public RunActiveTargetsAction(AntView view) {
		super(AntViewActionMessages.getString("RunActiveTargetsAction.Run"), ExternalToolsImages.getImageDescriptor(IExternalToolsUIConstants.IMG_RUN)); //$NON-NLS-1$
		setToolTipText(AntViewActionMessages.getString("RunActiveTargetsAction.Run_2")); //$NON-NLS-1$
		this.view = view;
	}

	public void run() {
		try {
			new ProgressMonitorDialog(view.getSite().getShell()).run(true, true, new IRunnableWithProgress() {
				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					monitor.beginTask(AntViewActionMessages.getString("RunActiveTargetsAction.Running"), TOTAL_WORK_UNITS); //$NON-NLS-1$

					List targetList= getActiveTargets();
					if (targetList.isEmpty()) {
						return;
					}
					Iterator targets= targetList.iterator();

					int workunit = TOTAL_WORK_UNITS / targetList.size();
					if (workunit == 0) {
						workunit = 1;
					}
					StringBuffer targetString= new StringBuffer();
					// Unroll the loop once to prime the state
					TargetNode targetNode= (TargetNode) targets.next();
					IFile previousFile= AntUtil.getFile(targetNode.getProject().getBuildFileName());
					targetString.append(targetNode.getName());
					while (targets.hasNext() && !monitor.isCanceled()) {
						// This loop combines successive targets from the same file. When a target
						// is first examined in the loop, it is appended to the target string. The target string
						// is only launched when a target from a different file is encountered or the loop finishes.
						targetNode = (TargetNode) targets.next();
						
						IFile file= AntUtil.getFile(targetNode.getProject().getBuildFileName());
						if (file.equals(previousFile)) {
							targetString.append(',').append(targetNode.getName());
							previousFile= file;
							continue;
						} else {
							// A new file. Before we start a new target string, launch the previous file.
							if (targetString.length() > 0) {
								monitor.subTask(previousFile.getName() + " -> " + targetString.toString()); //$NON-NLS-1$
								launchFile(previousFile, targetString.toString());
							}
							// Start the target string for the new file
							targetString= new StringBuffer(targetNode.getName());
							previousFile= file;
						}
						
					}
					// At the end of the loop, we need to launch the last targets
					if (targetString.length() > 0) {
						monitor.subTask(previousFile.getName() + " -> " + targetString.toString()); //$NON-NLS-1$
						launchFile(previousFile, targetString.toString());
					}
					monitor.done();
				}
			});
		} catch (InvocationTargetException e) {
			String message;
			if (e.getTargetException() instanceof CoreException) {
				message = ((CoreException) e.getTargetException()).getMessage();
			} else {
				message = e.getMessage();
			}
			reportError(message, e.getTargetException());
		} catch (InterruptedException e) {
		}
	}
	
	/**
	 * Executes the given targets in the given build file.
	 */
	private void launchFile(final IFile file, final String targetString) {
		Display.getDefault().syncExec(new Runnable() {
			public void run() {
				AntLaunchShortcut shortcut= new AntLaunchShortcut();
				shortcut.launch(file, ILaunchManager.RUN_MODE, targetString);
			}
		});
	}
	
	/**
	 * Report the given exception to the user with the given message
	 */
	private void reportError(String message, Throwable throwable) {
		IStatus status = null;
		if (throwable instanceof CoreException) {
			status = ((CoreException)throwable).getStatus();
		} else {
			status = new Status(IStatus.ERROR, IExternalToolConstants.PLUGIN_ID, IStatus.ERROR, message, throwable);
		}
		if (message == null) {
			message= AntViewActionMessages.getString("RunActiveTargetsAction.Exception"); //$NON-NLS-1$
		}
		ErrorDialog.openError(ExternalToolsPlugin.getActiveWorkbenchWindow().getShell(), AntViewActionMessages.getString("RunActiveTargetsAction.Error"), message, status); //$NON-NLS-1$
	}

	/**
	 * Updates the enablement of this action based on whether or not there are
	 * active targets
	 */
	public void update() {
		setEnabled(!getActiveTargets().isEmpty());
	}

	/**
	 * Returns the list of targets that are currently active in the Ant View. 
	 */
	public List getActiveTargets() {
		return view.getActiveTargets();
	}

}
