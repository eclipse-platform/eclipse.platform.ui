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
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.externaltools.internal.ant.antview.core.AntRunnable;
import org.eclipse.ui.externaltools.internal.ant.antview.core.ResourceMgr;
import org.eclipse.ui.externaltools.internal.ant.antview.views.AntView;
import org.eclipse.ui.externaltools.internal.ant.model.AntUtil;
import org.eclipse.ui.externaltools.internal.model.ExternalToolsPlugin;
import org.eclipse.ui.externaltools.internal.ui.LogConsoleDocument;
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
		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		IWorkbenchPage page = window.getActivePage();
		try {
			if (page != null) {
				page.showView(IExternalToolConstants.LOG_CONSOLE_VIEW_ID);
			}
		} catch (PartInitException e) {
			ExternalToolsPlugin.getDefault().getLog().log(e.getStatus());
		}
		LogConsoleDocument.getInstance().clearOutput();
		AntView view= AntUtil.getAntView();
		
		if (view == null) {
			LogConsoleDocument.getInstance().append("An error occurred launching Ant. Could not find ant view.", LogConsoleDocument.MSG_ERR);
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
			LogConsoleDocument.getInstance().append(message + "\n", LogConsoleDocument.MSG_ERR);
		} catch (InterruptedException e) {
			LogConsoleDocument.getInstance().append(ResourceMgr.getString("Error.UserCancel") + "\n", LogConsoleDocument.MSG_ERR);
		}
	}

}