/*
 * Copyright (c) 2002, Roscoe Rush. All Rights Reserved.
 *
 * The contents of this file are subject to the Common Public License
 * Version 0.5 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.eclipse.org/
 *
 */
package org.eclipse.ui.externaltools.internal.ant.antview.actions;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.externaltools.internal.ant.antview.core.AntRunnable;
import org.eclipse.ui.externaltools.internal.ant.antview.views.AntView;
import org.eclipse.ui.externaltools.internal.ant.model.AntUtil;
import org.eclipse.ui.externaltools.internal.model.ExternalToolsPlugin;
import org.eclipse.ui.externaltools.model.IExternalToolConstants;

public class RunAction extends Action {
	/**
	 * Constructor for RunAction
	 */
	public RunAction(String label, ImageDescriptor imageDescriptor) {
		super(label, imageDescriptor);
		setToolTipText(label);
	}

	/**
	 * @see Action#run()
	 */
	public void run() {
		AntView view= AntUtil.getAntView();
		if (view == null) {
			reportError("An error occurred launching Ant. Could not find ant view.", null);
			return;
		}
		AntRunnable runnable= new AntRunnable(view);
		try {
			new ProgressMonitorDialog(AntUtil.getAntView().getSite().getShell()).run(true, true, runnable);
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
		ErrorDialog.openError(ExternalToolsPlugin.getActiveWorkbenchWindow().getShell(), "Error", message, status);
	}
}