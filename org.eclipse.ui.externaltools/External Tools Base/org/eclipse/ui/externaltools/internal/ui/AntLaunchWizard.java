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

import org.eclipse.ant.core.TargetInfo;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.externaltools.internal.ant.model.AntUtil;
import org.eclipse.ui.externaltools.internal.model.DefaultRunnerContext;
import org.eclipse.ui.externaltools.internal.model.ExternalToolsPlugin;
import org.eclipse.ui.externaltools.internal.model.ToolMessages;
import org.eclipse.ui.externaltools.model.ExternalTool;
import org.eclipse.ui.externaltools.model.IExternalToolConstants;
import org.eclipse.ui.externaltools.model.ToolUtil;

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
	private TargetInfo[] targetList = null;

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
	public AntLaunchWizard(TargetInfo[] targetList, IFile antFile, IWorkbenchWindow window) {
		super();
		this.targetList = targetList;
		this.antFile = antFile;
		this.window = window;
		String antPath = antFile.getFullPath().toString();
		this.antTool = ExternalToolsPlugin.getDefault().getToolRegistry(getShell()).getToolNamed(antPath);
		if (this.antTool == null) {
			try {
				this.antTool = new ExternalTool(IExternalToolConstants.TOOL_TYPE_ANT_BUILD, antPath);
			} catch (CoreException exception) {
				MessageDialog.openError(getShell(), "Ant Error", "An exception occurred launching ant file");
				return;
			}
			this.antTool.setLocation(ToolUtil.buildVariableTag(IExternalToolConstants.VAR_WORKSPACE_LOC, antPath));
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
		
		StringBuffer buf = new StringBuffer();
		String[] targets = AntUtil.parseRunTargets(antTool.getExtraAttribute(AntUtil.RUN_TARGETS_ATTRIBUTE));
		
		page1.setInitialTargets(targets);
		page1.setInitialArguments(buf.toString());
		page1.setInitialDisplayLog(antTool.getShowConsole());
	}
	
	/* (non-Javadoc)
	 * Method declared on IWizard.
	 */
	public boolean performFinish() {
		updateTool();
//		ToolUtil.saveDirtyEditors(window);
//		if (antTool.getShowConsole()) {
//			ToolUtil.showLogConsole(window);
//			ToolUtil.clearLogDocument();
//		}
		
		IRunnableWithProgress runnable = new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
				DefaultRunnerContext context = new DefaultRunnerContext(antTool, antFile); //new DefaultRunnerContext(antTool, antFile.getProject(), window.getWorkbench().getWorkingSetManager());
				MultiStatus status = new MultiStatus(IExternalToolConstants.PLUGIN_ID, 0, "", null); //$NON-NLS-1$
				context.run(monitor, status);
				if (!status.isOK()) {
					MessageDialog.openError(getShell(), "Ant Error", "An exception occurred while running ant");
				}
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
				status = new Status(IStatus.ERROR, IExternalToolConstants.PLUGIN_ID, 0, ToolMessages.getString("AntLaunchWizard.internalAntError"), e.getTargetException()); //$NON-NLS-1$;
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
		ToolUtil.buildVariableTag(AntUtil.RUN_TARGETS_ATTRIBUTE, AntUtil.combineRunTargets(targets), buf);
		
		antTool.setArguments(buf.toString());
		antTool.setShowConsole(page1.getShowLog());

		if (isNewTool) {
			ExternalToolsPlugin.getDefault().getToolRegistry(getShell()).saveTool(antTool);
			isNewTool = false;
		}

	}
}