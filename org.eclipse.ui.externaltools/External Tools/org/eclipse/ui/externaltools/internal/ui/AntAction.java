package org.eclipse.ui.externaltools.internal.ui;

/**********************************************************************
Copyright (c) 2002 IBM Corp. and others.
All rights reserved.   This program and the accompanying materials
are made available under the terms of the Common Public License v0.5
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v05.html
 
Contributors:
**********************************************************************/
import org.eclipse.core.resources.IFile;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.externaltools.internal.core.*;

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

		AntTargetList targetList = AntUtil.getTargetList(file.getLocation());
		if (targetList == null) {
			MessageDialog.openError(
				window.getShell(),
				ToolMessages.getString("AntAction.runErrorTitle"), //$NON-NLS-1$;
				ToolMessages.getString("AntAction.runInvalidFile")); //$NON-NLS-1$;
			return;
		}

		AntLaunchWizard wizard = new AntLaunchWizard(targetList, file, window);
		wizard.setNeedsProgressMonitor(true);
		WizardDialog dialog = new WizardDialog(window.getShell(), wizard);
		dialog.create();
		dialog.open();
	}

	/**
	 * Saves any dirty editors if user preference
	 */
	private void saveDirtyEditors() {
		IPreferenceStore store = ExternalToolsPlugin.getDefault().getPreferenceStore();
		boolean autoSave = store.getBoolean(IPreferenceConstants.AUTO_SAVE);
		if (autoSave)
			window.getActivePage().saveAllEditors(false);
	}
}