package org.eclipse.ui.externaltools.internal.ui;

/**********************************************************************
Copyright (c) 2002 IBM Corp. and others.
All rights reserved.   This program and the accompanying materials
are made available under the terms of the Common Public License v0.5
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v05.html
 
Contributors:
**********************************************************************/
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;
import org.eclipse.ui.externaltools.internal.ant.dialog.AntExternalToolNewWizard;
import org.eclipse.ui.externaltools.internal.model.DefaultRunnerContext;
import org.eclipse.ui.externaltools.internal.model.ExternalToolsPlugin;
import org.eclipse.ui.externaltools.internal.model.IHelpContextIds;
import org.eclipse.ui.externaltools.internal.registry.ExternalToolRegistry;
import org.eclipse.ui.externaltools.internal.view.ExternalToolLabelProvider;
import org.eclipse.ui.externaltools.model.ExternalTool;
import org.eclipse.ui.externaltools.model.ExternalToolStorage;
import org.eclipse.ui.externaltools.model.IExternalToolConstants;
import org.eclipse.ui.externaltools.model.IStorageListener;
import org.eclipse.ui.externaltools.model.ToolUtil;
import org.eclipse.ui.externaltools.variable.ExpandVariableContext;
import org.eclipse.ui.help.WorkbenchHelp;

/**
 * Action to run an ant build file.
 */
public class AntAction extends Action {
	private IFile file;
	private IWorkbenchWindow window;

	/**
	 * Creates an initialize action to run an
	 * Ant build file
	 * 
	 * @param file the ant build file to run
	 */
	public AntAction(IFile file, IWorkbenchWindow window) {
		super();
		this.file = file;
		this.window = window;
		setText(file.getName());
		setToolTipText(file.getFullPath().toOSString());
		WorkbenchHelp.setHelp(this, IHelpContextIds.ANT_ACTION);
	}
	
	private Shell getShell() {
		return PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
	}
	
	private ExternalTool chooseTool(List tools) {
		ILabelProvider labelProvider = new ExternalToolLabelProvider();
		ElementListSelectionDialog dialog= new ElementListSelectionDialog(getShell(), labelProvider);
		dialog.setElements((ExternalTool[]) tools.toArray(new ExternalTool[tools.size()]));
		dialog.setTitle("Ant Tool Selection");
		dialog.setMessage("Choose an ant tool to run");
		dialog.setMultipleSelection(false);
		int result = dialog.open();
		labelProvider.dispose();
		if (result == ElementListSelectionDialog.OK) {
			return (ExternalTool) dialog.getFirstResult();
		}
		return null;		
	}
	
	public void runTool(final ExternalTool tool) {
		final MultiStatus status = new MultiStatus(IExternalToolConstants.PLUGIN_ID, 0, "", null);
		BusyIndicator.showWhile(getShell().getDisplay(), new Runnable() {
			public void run() {
				new DefaultRunnerContext(tool, file).run(new NullProgressMonitor(), status);
			}
		});
		if (!status.isOK()) {
			StringBuffer message= new StringBuffer("An exception occurred while running ant: ");
			IStatus[] errors= status.getChildren();
			IStatus error;
			for (int i= 0, numErrors= errors.length; i < numErrors; i++) {
				error= errors[i];
				if (error.getSeverity() == IStatus.ERROR) {
					Throwable exception= error.getException();
					message.append('\n');
					if (exception != null) {
						message.append(exception.getClass().getName()).append(' ');
					}
					message.append(error.getMessage());
				}
			}
			MessageDialog.openError(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), "Ant Error", message.toString());
		}
	}

	/* (non-Javadoc)
	 * Method declared on IAction.
	 */
	public void run() {
		if (file == null) {
			return;
		}
		
		ExternalTool tool= null;
		ExternalToolRegistry registry= ExternalToolsPlugin.getDefault().getToolRegistry(getShell());
		ExternalTool[] antTools= registry.getToolsOfType(IExternalToolConstants.TOOL_TYPE_ANT_BUILD);
		MultiStatus status = new MultiStatus(IExternalToolConstants.PLUGIN_ID, 0, "An error occurred while expanding the location of one or more ant tools: ", null);
		List tools= new ArrayList();
		String toolLocation;
		for (int i= 0, numTools= antTools.length; i < numTools; i++) {
			toolLocation= ToolUtil.expandFileLocation(antTools[i].getLocation(), ExpandVariableContext.EMPTY_CONTEXT, status);
			if (toolLocation != null && toolLocation.equals(file.getLocation().toString())) {
				tools.add(antTools[i]);
			}
		}
		if (tools.size() == 1) {
			tool= (ExternalTool)tools.get(0);
		} else if (tools.size() > 1) {
			tool= chooseTool(tools);
			if (tool == null) {
				// User cancelled.
				return;
			}
		}
		
		if (tool != null) {
			runTool(tool);
			return;
		}
		WizardDialog dialog= new WizardDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), new AntExternalToolNewWizard(file));
		dialog.open();
		if (dialog.getReturnCode() == WizardDialog.CANCEL) {
			return;
		}
		ExternalToolStorage.addStorageListener(new IStorageListener() {
			public void toolDeleted(ExternalTool tool) {
			}

			public void toolCreated(ExternalTool tool) {
				if (tool.getLocation().equals(file.getLocation().toString())) { // Is this the tool we're expecting?
					runTool(tool);
				}
				ExternalToolStorage.removeStorageListener(this);
			}

			public void toolModified(ExternalTool tool) {
			}

			public void toolsRefreshed() {
			}
		});
	}
}