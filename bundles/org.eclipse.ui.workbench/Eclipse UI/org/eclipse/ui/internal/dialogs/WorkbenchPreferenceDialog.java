/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.dialogs;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.preference.IPreferenceNode;
import org.eclipse.jface.preference.PreferenceManager;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.activities.WorkbenchActivityHelper;
import org.eclipse.ui.internal.IWorkbenchHelpContextIds;
import org.eclipse.ui.internal.WorkbenchPlugin;

/**
 * Prefence dialog for the workbench including the ability to load/save
 * preferences.
 */
public class WorkbenchPreferenceDialog extends FilteredPreferenceDialog {
	/**
	 * There can only ever be one instance of the workbench's preference dialog.
	 * This keeps a handle on this instance, so that attempts to create a second
	 * dialog should just fail (or return the original instance).
	 * 
	 * @since 3.1
	 */
	private static WorkbenchPreferenceDialog instance = null;
	
	/**
	 * The bounds of this dialog will be persisted in the dialog settings.
	 * This is defined at the most concrete level of the hierarchy so that
	 * different concrete implementations don't necessarily store their bounds 
	 * in the same settings.
	 * 
	 * @since 3.2
	 */
	private static final String DIALOG_SETTINGS_SECTION = "WorkbenchPreferenceDialogSettings"; //$NON-NLS-1$

	
	/**
	 * Creates a workbench preference dialog to a particular preference page. It
	 * is the responsibility of the caller to then call <code>open()</code>.
	 * The call to <code>open()</code> will not return until the dialog
	 * closes, so this is the last chance to manipulate the dialog.
	 * 
	 * @param shell
	 * 			The Shell to parent the dialog off of if it is not
	 * 			already created. May be <code>null</code>
	 * 			in which case the active workbench window will be used
	 * 			if available.
	 * @param preferencePageId
	 *            The identifier of the preference page to open; may be
	 *            <code>null</code>. If it is <code>null</code>, then the
	 *            preference page is not selected or modified in any way.
	 * @return The selected preference page.
	 * @since 3.1
	 */
	public static final WorkbenchPreferenceDialog createDialogOn(Shell shell, final String preferencePageId) {
		final WorkbenchPreferenceDialog dialog;

		if (instance == null) {
			/*
			 * There is no existing preference dialog, so open a new one with
			 * the given selected page.
			 */

			Shell parentShell = shell;
			if (parentShell == null) {
				// Determine a decent parent shell.
				final IWorkbench workbench = PlatformUI.getWorkbench();
				final IWorkbenchWindow workbenchWindow = workbench.getActiveWorkbenchWindow();
				if (workbenchWindow != null) {
					parentShell = workbenchWindow.getShell();
				} else {
					parentShell = null;
				}
			}

			// Create the dialog
			final PreferenceManager preferenceManager = PlatformUI.getWorkbench()
					.getPreferenceManager();
			dialog = new WorkbenchPreferenceDialog(parentShell, preferenceManager);
			if (preferencePageId != null) {
				dialog.setSelectedNode(preferencePageId);
			}
			dialog.create();
			PlatformUI.getWorkbench().getHelpSystem().setHelp(
					dialog.getShell(),
					IWorkbenchHelpContextIds.PREFERENCE_DIALOG);

		} else {
			/*
			 * There is an existing preference dialog, so let's just select the
			 * given preference page.
			 */
			dialog = instance;
			if (preferencePageId != null) {
				dialog.setCurrentPageId(preferencePageId);
			}

		}

		// Get the selected node, and return it.
		return dialog;
	}

	/**
	 * Creates a new preference dialog under the control of the given preference
	 * manager.
	 * 
	 * @param parentShell
	 *            the parent shell
	 * @param manager
	 *            the preference manager
	 */
	public WorkbenchPreferenceDialog(Shell parentShell, PreferenceManager manager) {
		super(parentShell, manager);
		Assert.isTrue((instance == null),
				"There cannot be two preference dialogs at once in the workbench."); //$NON-NLS-1$
		instance = this;
		
	}


	/* (non-Javadoc)
	 * @see org.eclipse.jface.window.Window#close()
	 */
	public boolean close() {
		instance = null;
		return super.close();
	}


	/**
	 * Differs from super implementation in that if the node is found but should
	 * be filtered based on a call to
	 * <code>WorkbenchActivityHelper.filterItem()</code> then
	 * <code>null</code> is returned.
	 * 
	 * @see org.eclipse.jface.preference.PreferenceDialog#findNodeMatching(java.lang.String)
	 */
	protected IPreferenceNode findNodeMatching(String nodeId) {
		IPreferenceNode node = super.findNodeMatching(nodeId);
		if (WorkbenchActivityHelper.filterItem(node)) {
			return null;
		}
		return node;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#okPressed()
	 */
	protected void okPressed() {
		super.okPressed();
	}
	
	/* (non-Javadoc)
     * @see org.eclipse.jface.window.Dialog#getDialogBoundsSettings()
     * 
     * @since 3.2
     */
	protected IDialogSettings getDialogBoundsSettings() {
        IDialogSettings settings = WorkbenchPlugin.getDefault().getDialogSettings();
        IDialogSettings section = settings.getSection(DIALOG_SETTINGS_SECTION);
        if (section == null) {
            section = settings.addNewSection(DIALOG_SETTINGS_SECTION);
        } 
        return section;
	}
	
	/* (non-Javadoc)
     * @see org.eclipse.jface.window.Dialog#getDialogBoundsStrategy()
     * 
     * Overridden to persist only the location, not the size, since the current
     * page dictates the most appropriate size for the dialog.
     * @since 3.2
     */
	protected int getDialogBoundsStrategy() {
		return DIALOG_PERSISTLOCATION;
	}
}
