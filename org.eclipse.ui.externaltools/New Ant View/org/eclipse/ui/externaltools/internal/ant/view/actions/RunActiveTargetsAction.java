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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.externaltools.internal.ant.view.AntView;
import org.eclipse.ui.externaltools.internal.ant.view.elements.ProjectNode;
import org.eclipse.ui.externaltools.internal.ant.view.elements.TargetNode;
import org.eclipse.ui.externaltools.internal.model.ExternalToolsImages;
import org.eclipse.ui.externaltools.internal.model.ExternalToolsPlugin;
import org.eclipse.ui.externaltools.internal.ui.IExternalToolsUIConstants;
import org.eclipse.ui.externaltools.model.IExternalToolConstants;
import org.eclipse.ui.texteditor.IUpdate;

public class RunActiveTargetsAction extends Action implements IUpdate {

	private AntView view;
	private static final int TOTAL_WORK_UNITS = 100;

	public RunActiveTargetsAction(AntView view) {
		super("Run Targets", ExternalToolsImages.getImageDescriptor(IExternalToolsUIConstants.IMG_RUN));
		setToolTipText("Run the active targets or the currently selected targets if none are active");
		this.view = view;
	}

	public void run() {
		final TargetNode selectedTarget= getSelectedTarget();
		try {
			new ProgressMonitorDialog(view.getSite().getShell()).run(true, true, new IRunnableWithProgress() {
				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					monitor.beginTask("Running Ant Targets", TOTAL_WORK_UNITS);

					List targetList= getActiveTargets();
					if (targetList.isEmpty() && selectedTarget != null) {
						targetList.add(selectedTarget);
					}
					Iterator targets= targetList.iterator();

					int workunit = TOTAL_WORK_UNITS / targetList.size();
					if (0 == workunit)
						workunit = 1;
					while (targets.hasNext()) {
						TargetNode targetNode = (TargetNode) targets.next();

						String filename = ((ProjectNode) targetNode.getParent()).getBuildFileName();
						IPath path = new Path(filename);
						path = path.setDevice("");
						int trimCount = path.matchingFirstSegments(Platform.getLocation());
						if (trimCount > 0)
							path = path.removeFirstSegments(trimCount);
						path.removeLastSegments(1);

						try {
							ILaunchConfigurationType type = DebugPlugin.getDefault().getLaunchManager().getLaunchConfigurationType(IExternalToolConstants.ID_ANT_LAUNCH_CONFIGURATION_TYPE);
							ILaunchConfigurationWorkingCopy workingCopy = type.newInstance(null, path.toString() + "-" + targetNode.getName());
							workingCopy.setAttribute(IExternalToolConstants.ATTR_LOCATION, filename);
							workingCopy.setAttribute(IExternalToolConstants.ATTR_ANT_TARGETS, targetNode.getName());
							workingCopy.setAttribute(IExternalToolConstants.ATTR_RUN_IN_BACKGROUND, false);

							monitor.subTask(path.toString() + " -> " + targetNode.getName());
							workingCopy.launch(ILaunchManager.RUN_MODE, new SubProgressMonitor(monitor, workunit));
						} catch (CoreException e) {
							Throwable carriedException = e.getStatus().getException();
							if (carriedException instanceof OperationCanceledException) {
								throw new InterruptedException(carriedException.getMessage());
							} else {
								throw new InvocationTargetException(e);
							}
						} catch (OperationCanceledException e) {
							throw new InvocationTargetException(e);
						}
					}
					monitor.done();

				}
			});
		} catch (InvocationTargetException e) {
			String message;
			if (e.getTargetException() instanceof CoreException)
				message = ((CoreException) e.getTargetException()).getLocalizedMessage();
			else
				message = e.getLocalizedMessage();
			reportError(message, e.getTargetException());
		} catch (InterruptedException e) {
		}
	}
	
	private void reportError(String message, Throwable throwable) {
		IStatus status = null;
		if (throwable instanceof CoreException) {
			status = ((CoreException)throwable).getStatus();
		} else {
			status = new Status(IStatus.ERROR, IExternalToolConstants.PLUGIN_ID, 0, message, throwable);
		}
		ErrorDialog.openError(ExternalToolsPlugin.getActiveWorkbenchWindow().getShell(), "Error Running Targets", message, status);
	}

	/**
	 * Updates the enablement of this action based on the user's selection
	 */
	public void update() {
		setEnabled(getSelectedTarget() != null || !getActiveTargets().isEmpty());
	}

	/**
	 * Returns the list of targets that are currently active in the Ant View. 
	 */
	public List getActiveTargets() {
		return view.getActiveTargets();
	}

	/**
	 * Returns the selected target in the project viewer or <code>null</code> if
	 * no target is selected or more than one element is selected.
	 *
	 * @return TargetNode the selected target
	 */
	public TargetNode getSelectedTarget() {
		IStructuredSelection selection= (IStructuredSelection) view.getProjectViewer().getSelection();
		if (selection.isEmpty()) {
			return null;
		}
		Iterator iter = selection.iterator();
		while (iter.hasNext()) {
			Object data = iter.next();
			if (iter.hasNext() || !(data instanceof TargetNode)) {
				// Only enable for single selection of a TargetNode
				return null;
			}
		}
		return (TargetNode) selection.getFirstElement();
	}

}
