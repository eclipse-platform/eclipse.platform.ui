package org.eclipse.ui.externaltools.internal.ui;

/**********************************************************************
Copyright (c) 2002 IBM Corp. and others.
All rights reserved.   This program and the accompanying materials
are made available under the terms of the Common Public License v0.5
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v05.html
 
Contributors:
**********************************************************************/
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.*;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.externaltools.internal.core.*;

/**
 * The wizard to run an Ant file when the Run Ant...
 * context menu action is choosen by the user.
 * <p>
 * Note: Currently there is only one page in this wizard.
 * </p>
 */
public class AntLaunchWizard extends Wizard {
	/**
	 * The file that contains the Ant file.
	 */
	private IFile antFile = null;

	/**
	 * The Ant project described in the xml file.
	 */
	private AntTargetList targetList = null;

	/**
	 * The external tool representing the Ant file
	 */
	private ExternalTool antTool = null;

	/**
	 * Whether the external tool is new for this wizard
	 */
	private boolean isNewTool = false;
	
	/**
	 * The workbench window that the action launch
	 * this wizard.
	 */
	private IWorkbenchWindow window = null;

	/**
	 * The first page of the wizard.
	 */
	private AntLaunchWizardPage page1 = null;

	/**
	 * Creates a new wizard, given the project described in the
	 * file and the file itself.
	 * 
	 * @param antProject
	 * @param antFile
	 */
	public AntLaunchWizard(AntTargetList targetList, IFile antFile, IWorkbenchWindow window) {
		super();
		this.targetList = targetList;
		this.antFile = antFile;
		this.window = window;
		String antPath = antFile.getFullPath().toString();
		this.antTool = ExternalToolsPlugin.getDefault().getRegistry().getExternalTool(antPath);
		if (this.antTool == null) {
			this.antTool = new ExternalTool();
			this.antTool.setName(antPath);
			this.antTool.setType(ExternalTool.TOOL_TYPE_ANT);
			this.antTool.setLocation(ToolUtil.buildVariableTag(ExternalTool.VAR_WORKSPACE_LOC, antPath));
			this.isNewTool = true;
		}
		setWindowTitle(ToolMessages.getString("AntLaunchWizard.shellTitle")); //$NON-NLS-1$;
	}
	
	/* (non-Javadoc)
	 * Method declared on IWizard.
	 */
	public void addPages() {
		page1 = new AntLaunchWizardPage(targetList);
		addPage(page1);
		
		String args = antTool.getArguments();
		StringBuffer buf = new StringBuffer();
		String[] targets = ToolUtil.extractVariableArguments(args, ExternalTool.VAR_ANT_TARGET, buf);
		
		page1.setInitialTargets(targets);
		page1.setInitialArguments(buf.toString());
		page1.setInitialDisplayLog(antTool.getShowLog());
	}
	
	/* (non-Javadoc)
	 * Method declared on IWizard.
	 */
	public boolean performFinish() {
		updateTool();
		ToolUtil.saveDirtyEditors(window);
		if (antTool.getShowLog()) {
			ToolUtil.showLogConsole(window);
			ToolUtil.clearLogDocument();
		}
		
		IRunnableWithProgress runnable = new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
				DefaultRunnerContext context = new DefaultRunnerContext(antTool, antFile.getProject(), window.getWorkbench().getWorkingSetManager());
				context.run(monitor, window.getShell());
			};
		};

		try {
			getContainer().run(true, true, runnable);
		} catch (InterruptedException e) {
			return false;
		} catch (InvocationTargetException e) {
			IStatus status = null;
			if (e.getTargetException() instanceof CoreException)
				status = ((CoreException)e.getTargetException()).getStatus();
			else
				status = new Status(IStatus.ERROR, ExternalToolsPlugin.PLUGIN_ID, 0, ToolMessages.getString("AntLaunchWizard.internalAntError"), e.getTargetException()); //$NON-NLS-1$;
			ErrorDialog.openError(
				getShell(), 
				ToolMessages.getString("AntLaunchWizard.runErrorTitle"), //$NON-NLS-1$;
				ToolMessages.getString("AntLaunchWizard.runAntProblem"), //$NON-NLS-1$;
				status);
			return false;
		}

		return true;
	}

	/**
	 * Method updateTool.
	 */
	private void updateTool() {
		StringBuffer buf = new StringBuffer(page1.getArguments());
		String[] targets = page1.getSelectedTargets();
		ToolUtil.buildVariableTags(ExternalTool.VAR_ANT_TARGET, targets, buf);
		
		antTool.setArguments(buf.toString());
		antTool.setShowLog(page1.getShowLog());

		ArrayList tools = ExternalToolsPlugin.getDefault().getRegistry().getExternalTools();
		if (isNewTool) {
			tools.add(antTool);
			isNewTool = false;
		}
		ExternalToolsPlugin.getDefault().getRegistry().setExternalTools(tools);
	}
}