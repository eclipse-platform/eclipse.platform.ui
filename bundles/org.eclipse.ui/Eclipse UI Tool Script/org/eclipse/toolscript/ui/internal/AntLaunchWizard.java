package org.eclipse.toolscript.ui.internal;

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

import org.apache.tools.ant.Project;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.toolscript.core.internal.AntUtil;
import org.eclipse.toolscript.core.internal.ToolScript;
import org.eclipse.toolscript.core.internal.ToolScriptContext;
import org.eclipse.toolscript.core.internal.ToolScriptPlugin;
import org.eclipse.toolscript.core.internal.ToolUtil;
import org.eclipse.ui.IWorkbenchWindow;

/**
 * The wizard to run an Ant script file when the Run Ant...
 * context menu action is choosen by the user.
 * <p>
 * Note: Currently there is only one page in this wizard.
 * </p>
 */
public class AntLaunchWizard extends Wizard {
	/**
	 * The file that contains the Ant script.
	 */
	private IFile antFile = null;

	/**
	 * The Ant project described in the xml file.
	 */
	private Project antProject = null;

	/**
	 * The tool script representing Ant script
	 */
	private ToolScript antScript = null;

	/**
	 * Whether the tool script is new for this wizard
	 */
	private boolean isNewScript = false;
	
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
	public AntLaunchWizard(Project antProject, IFile antFile, IWorkbenchWindow window) {
		super();
		this.antProject = antProject;
		this.antFile = antFile;
		this.window = window;
		String name = antFile.getFullPath().toString();
		this.antScript = ToolScriptPlugin.getDefault().getRegistry().getToolScript(name);
		if (this.antScript == null) {
			this.antScript = new ToolScript();
			this.antScript.setName(name);
			this.antScript.setType(ToolScript.SCRIPT_TYPE_ANT);
			this.antScript.setLocation(antFile.getLocation().toString());
			this.isNewScript = true;
		}
		setWindowTitle(ToolScriptMessages.getString("AntLaunchWizard.shellTitle")); //$NON-NLS-1$;
	}
	
	/* (non-Javadoc)
	 * Method declared on IWizard.
	 */
	public void addPages() {
		page1 = new AntLaunchWizardPage(antProject);
		addPage(page1);
		String args = antScript.getArguments();

		page1.setInitialTargets(getInitialTargets());
		page1.setInitialArguments(getInitialArguments());
		page1.setInitialDisplayLog(antScript.getShowLog());
	}
	
	/**
	 * Returns the initial Ant targets to use. This information is
	 * restored from the tool script created the last time it was run.
	 * 
	 * @return String[] the name of the targets
	 */
	public String[] getInitialTargets() {
		return new String[0];
	}
	
	/**
	 * Returns the initial arguments to use. This information is
	 * restored from the tool script created the last time it was run.
	 * 
	 * @return String the arguments string
	 */
	public String getInitialArguments() {
		return ""; //$NON-NLS-1$;
	}
	
	/* (non-Javadoc)
	 * Method declared on IWizard.
	 */
	public boolean performFinish() {
		updateScript();
		if (antScript.getShowLog()) {
			ToolScriptPlugin.getDefault().showLogConsole(window);
			ToolScriptPlugin.getDefault().clearLogDocument();
		}
		
		IRunnableWithProgress runnable = new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
				try {
					ToolScriptContext context = new ToolScriptContext(antScript, antFile.getProject(), window.getWorkbench().getWorkingSetManager());
					context.run(monitor, window.getShell());
				} catch (BuildCanceledException e) {
					throw new InterruptedException();
				} catch (Exception e) {
					throw new InvocationTargetException(e, e.getMessage());
				}
			};
		};

		try {
			getContainer().run(true, true, runnable);
		} catch (InterruptedException e) {
			return false;
		} catch (InvocationTargetException e) {
			IStatus status = new Status(IStatus.ERROR, ToolScriptPlugin.PLUGIN_ID, 0, ToolScriptMessages.getString("AntLaunchWizard.runAntProblem"), e); //$NON-NLS-1$;
			ErrorDialog.openError(
				getShell(), 
				ToolScriptMessages.getString("AntLaunchWizard.runErrorTitle"), //$NON-NLS-1$;
				ToolScriptMessages.getString("AntLaunchWizard.runAntProblem"), //$NON-NLS-1$;
				status);
			return false;
		}

		return true;
	}

	/**
	 * Method updateScript.
	 */
	private void updateScript() {
		StringBuffer buf = new StringBuffer(page1.getArguments());
		String[] targets = page1.getSelectedTargets();
		ToolUtil.buildVariableTags(ToolScript.VAR_ANT_TARGET, targets, buf);
		
		antScript.setArguments(buf.toString());
		antScript.setShowLog(page1.getShowLog());

		if (isNewScript) {
			ArrayList scripts = ToolScriptPlugin.getDefault().getRegistry().getToolScripts();
			scripts.add(antScript);
			ToolScriptPlugin.getDefault().getRegistry().setToolScripts(scripts);
			isNewScript = false;
		}
	}
}