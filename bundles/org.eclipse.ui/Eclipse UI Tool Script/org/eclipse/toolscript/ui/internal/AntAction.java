package org.eclipse.toolscript.ui.internal;

/**********************************************************************
Copyright (c) 2002 IBM Corp. and others.
All rights reserved.   This program and the accompanying materials
are made available under the terms of the Common Public License v0.5
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v05.html
 
Contributors:
**********************************************************************/
import java.io.File;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.ProjectHelper;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.toolscript.core.internal.AntUtil;
import org.eclipse.toolscript.core.internal.IPreferenceConstants;
import org.eclipse.toolscript.core.internal.ToolScriptPlugin;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

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
	}

	/* (non-Javadoc)
	 * Method declared on IAction.
	 */
	public void run() {
		if (file == null)
			return;

		saveDirtyEditors();

		Project project = AntUtil.createAntProject(file.getLocation());
		if (project == null) {
			MessageDialog.openError(
				window.getShell(),
				ToolScriptMessages.getString("AntAction.runErrorTitle"), //$NON-NLS-1$;
				ToolScriptMessages.getString("AntAction.runInvalidFile")); //$NON-NLS-1$;
			return;
		}

		AntLaunchWizard wizard = new AntLaunchWizard(project, file, window);
		wizard.setNeedsProgressMonitor(true);
		WizardDialog dialog = new WizardDialog(window.getShell(), wizard);
		dialog.create();
		dialog.open();
	}

	/**
	 * Saves any dirty editors if user preference
	 */
	private void saveDirtyEditors() {
		IPreferenceStore store = ToolScriptPlugin.getDefault().getPreferenceStore();
		boolean autoSave = store.getBoolean(IPreferenceConstants.AUTO_SAVE);
		if (autoSave)
			window.getActivePage().saveAllEditors(false);
	}
}