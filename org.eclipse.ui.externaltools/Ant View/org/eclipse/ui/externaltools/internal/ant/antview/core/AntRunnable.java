/*
 * Copyright (c) 2002, Roscoe Rush. All Rights Reserved.
 *
 * The contents of this file are subject to the Common Public License
 * Version 0.5 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.eclipse.org/
 *
 */
package org.eclipse.ui.externaltools.internal.ant.antview.core;

import java.lang.reflect.InvocationTargetException;
import java.util.ListIterator;
import java.util.Vector;

import org.apache.tools.ant.Project;
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
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.ui.externaltools.internal.ant.antview.tree.TreeNode;
import org.eclipse.ui.externaltools.internal.ant.antview.views.AntView;
import org.eclipse.ui.externaltools.internal.ant.antview.views.AntViewContentProvider;
import org.eclipse.ui.externaltools.model.IExternalToolConstants;

public class AntRunnable implements IRunnableWithProgress {
	private static final String ANT_LOGGER_CLASS = "org.eclipse.ui.externaltools.internal.ant.logger.AntBuildLogger";
	private static final String INPUT_HANDLER_CLASS = "org.eclipse.ui.externaltools.internal.ant.inputhandler.AntInputHandler"; //$NON-NLS-1$
	private static final int TOTAL_WORK_UNITS = 100;
	
	private AntView antView;
	
	public AntRunnable(AntView antView) {
		this.antView= antView;
	}
	
	/**
	 * Use launch configs
	 */
	public void run(IProgressMonitor monitor) throws InterruptedException, InvocationTargetException {
		monitor.beginTask(ResourceMgr.getString("Monitor.Title"), TOTAL_WORK_UNITS);

		AntViewContentProvider viewContentProvider = antView.getViewContentProvider();
		Vector targetVector = viewContentProvider.getTargetVector();

		if (0 == targetVector.size()) { 
			IStatus status = getStatus(ResourceMgr.getString("Error.EmptyTargetVector"), null);
			CoreException ce = new CoreException(status);
			throw new InvocationTargetException(ce);
		}
		
		int workunit = TOTAL_WORK_UNITS / targetVector.size();
		if (0 == workunit)
			workunit = 1;
		ListIterator targets = targetVector.listIterator();
		while (targets.hasNext()) {
			TreeNode targetNode = (TreeNode) targets.next();

			String filename = (String) targetNode.getProperty("BuildFile");
			IPath path = new Path(filename);
			path = path.setDevice("");
			int trimCount = path.matchingFirstSegments(Platform.getLocation());
			if (trimCount > 0)
				path = path.removeFirstSegments(trimCount);
			path.removeLastSegments(1);
			
			try {
				ILaunchConfigurationType type = DebugPlugin.getDefault().getLaunchManager().getLaunchConfigurationType(IExternalToolConstants.ID_ANT_LAUNCH_CONFIGURATION_TYPE);
				ILaunchConfigurationWorkingCopy workingCopy = type.newInstance(null, path.toString() + "-" + targetNode.getText());
				workingCopy.setAttribute(IExternalToolConstants.ATTR_LOCATION, filename);
				workingCopy.setAttribute(IExternalToolConstants.ATTR_ANT_TARGETS, targetNode.getText());
				workingCopy.setAttribute(IExternalToolConstants.ATTR_RUN_IN_BACKGROUND, false);
	
				monitor.subTask(path.toString() + " -> " + targetNode.getText());
				workingCopy.launch(ILaunchManager.RUN_MODE,new SubProgressMonitor(monitor, workunit));
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

	private int getAntDisplayLevel(String level) {
		if (level.equals(IAntViewConstants.ANT_DISPLAYLVL_ERROR))
			return Project.MSG_ERR;
		if (level.equals(IAntViewConstants.ANT_DISPLAYLVL_WARN))
			return Project.MSG_WARN;
		if (level.equals(IAntViewConstants.ANT_DISPLAYLVL_INFO))
			return Project.MSG_INFO;
		if (level.equals(IAntViewConstants.ANT_DISPLAYLVL_VERBOSE))
			return Project.MSG_VERBOSE;
		if (level.equals(IAntViewConstants.ANT_DISPLAYLVL_DEBUG))
			return Project.MSG_DEBUG;
		return Project.MSG_DEBUG;
	}
	
	private IStatus getStatus(String message, Throwable throwable) {
		IStatus status = null;
		if (throwable instanceof CoreException) {
			status = ((CoreException)throwable).getStatus();
		} else {
			status = new Status(IStatus.ERROR, IExternalToolConstants.PLUGIN_ID, 0, message, throwable);
		}
		return status;
	}	
}
