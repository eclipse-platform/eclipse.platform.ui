/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.dialogs;

import java.io.File;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.preference.IPreferenceNode;
import org.eclipse.jface.preference.IPreferencePage;
import org.eclipse.jface.preference.PreferenceManager;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.internal.IWorkbenchHelpContextIds;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.misc.Assert;

/**
 * Prefence dialog for the workbench including the ability to load/save
 * preferences.
 */
public class WorkbenchPreferenceDialog extends FilteredPreferenceDialog {
	/**
	 * The Load button id.
	 */
	private final static int LOAD_ID = IDialogConstants.CLIENT_ID + 1;

	/**
	 * The Save button id.
	 */
	private final static int SAVE_ID = IDialogConstants.CLIENT_ID + 2;

	/**
	 * The dialog settings key for the last used import/export path.
	 */
	final static String FILE_PATH_SETTING = "PreferenceImportExportFileSelectionPage.filePath"; //$NON-NLS-1$
	
    /**
     * There can only ever be one instance of the workbench's preference dialog.
     * This keeps a handle on this instance, so that attempts to create a second
     * dialog should just fail (or return the original instance).
     * 
     * @since 3.1
     */
    private static WorkbenchPreferenceDialog instance = null;

    /**
     * Creates a workbench preference dialog to a particular preference page. It
     * is the responsibility of the caller to then call <code>open()</code>.
     * The call to <code>open()</code> will not return until the dialog
     * closes, so this is the last chance to manipulate the dialog.
     * 
     * @param preferencePageId
     *            The identifier of the preference page to open; may be
     *            <code>null</code>. If it is <code>null</code>, then the
     *            preference page is not selected or modified in any way.
     * @return The selected preference page.
     * @since 3.1
     */
    public static final WorkbenchPreferenceDialog createDialogOn(
            final String preferencePageId) {
        final WorkbenchPreferenceDialog dialog;

        if (instance == null) {
            /*
             * There is no existing preference dialog, so open a new one with
             * the given selected page.
             */

            // Determine a decent parent shell.
            final IWorkbench workbench = PlatformUI.getWorkbench();
            final IWorkbenchWindow workbenchWindow = workbench
                    .getActiveWorkbenchWindow();
            final Shell parentShell;
            if (workbenchWindow != null) {
                parentShell = workbenchWindow.getShell();
            } else {
                parentShell = null;
            }

            // Create the dialog and open it.
            final PreferenceManager preferenceManager = PlatformUI
                    .getWorkbench().getPreferenceManager();
            dialog = new WorkbenchPreferenceDialog(parentShell,
                    preferenceManager);
            if (preferencePageId != null) {
                dialog.setSelectedNode(preferencePageId);
            }
            dialog.create();
            WorkbenchHelp.setHelp(dialog.getShell(),
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
	public WorkbenchPreferenceDialog(Shell parentShell,
            PreferenceManager manager) {
        super(parentShell, manager);
        Assert
                .isTrue((instance == null),
                        "There cannot be two preference dialogs at once in the workbench."); //$NON-NLS-1$
        instance = this;

    }

	/*
	 * (non-Javadoc) Method declared on Dialog.
	 */
	protected void buttonPressed(int buttonId) {
		switch (buttonId) {
		case LOAD_ID: {
			loadPressed();
			return;
		}
		case SAVE_ID: {
			savePressed();
			return;
		}
		}
		super.buttonPressed(buttonId);
	}
    
    /**
     * Closes the preference dialog. This clears out the singleton instance
     * before calling the super implementation.
     * 
     * @return <code>true</code> if the dialog is (or was already) closed, and
     *         <code>false</code> if it is still open
     * @since 3.1
     */
    public final boolean close() {
        instance = null;
        return super.close();
    }

	/*
	 * (non-Javadoc) Method declared on Dialog.
	 */
	protected void createButtonsForButtonBar(Composite parent) {
		parent.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		createButton(parent, LOAD_ID, WorkbenchMessages
				.getString("WorkbenchPreferenceDialog.load"), false); //$NON-NLS-1$
		createButton(parent, SAVE_ID, WorkbenchMessages
				.getString("WorkbenchPreferenceDialog.save"), false); //$NON-NLS-1$
		Label l = new Label(parent, SWT.NONE);
		l.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		GridLayout layout = (GridLayout) parent.getLayout();
		layout.numColumns++;
		layout.makeColumnsEqualWidth = false;

		super.createButtonsForButtonBar(parent);
	}

	/**
	 * Handle a request to load preferences
	 */
	protected void loadPressed() {
		final IPath filePath = getFilePath(false);
		if (filePath == null)
			return;
		BusyIndicator.showWhile(getShell().getDisplay(),new Runnable(){
			/* (non-Javadoc)
			 * @see java.lang.Runnable#run()
			 */
			public void run() {
				importPreferences(filePath);
			}
		});
		
		close();
	}

	/**
	 * Get the file name we are using. Set the button type flag depending on
	 * whether it is import or export operation.
	 * 
	 * @param export
	 *            <code>true</code> if an export file name is being looked
	 *            for.
	 * 
	 * @return IPath or <code>null</code> if no selection is mage.
	 */
	private IPath getFilePath(boolean export) {

		// Find the closest file/directory to what is currently entered.
		String currentFileName = getFileNameSetting(export);

		// Open a dialog allowing the user to choose.
		FileDialog fileDialog = null;
		if (export)
			fileDialog = new FileDialog(getShell(), SWT.SAVE);
		else
			fileDialog = new FileDialog(getShell(), SWT.OPEN);

		if (currentFileName != null)
			fileDialog.setFileName(currentFileName);
		fileDialog
				.setFilterExtensions(PreferenceImportExportFileSelectionPage.DIALOG_PREFERENCE_EXTENSIONS);
		currentFileName = fileDialog.open();

		if (currentFileName == null)
			return null;

		/*
		 * Append the default filename if none was specifed and such a file does
		 * not exist.
		 */
		String fileName = new File(currentFileName).getName();
		if (fileName.lastIndexOf(".") == -1) { //$NON-NLS-1$
			currentFileName += AbstractPreferenceImportExportPage.PREFERENCE_EXT;
		}
		setFileNameSetting(currentFileName);
		return new Path(currentFileName);

	}

	/**
	 * @param currentFileName
	 */
	private void setFileNameSetting(String currentFileName) {
		if (currentFileName != null)
			WorkbenchPlugin.getDefault().getDialogSettings().put(
					WorkbenchPreferenceDialog.FILE_PATH_SETTING,
					currentFileName);

	}

	/**
	 * Return the file name setting or a default value if there isn't one.
	 * 
	 * @param export
	 *            <code>true</code> if an export file name is being looked
	 *            for.
	 * 
	 * @return String if there is a good value to choose. Otherwise return
	 *         <code>null</code>.
	 */
	private String getFileNameSetting(boolean export) {

		String lastFileName = WorkbenchPlugin.getDefault().getDialogSettings()
				.get(WorkbenchPreferenceDialog.FILE_PATH_SETTING);
		if (lastFileName == null) {
			if (export)
				return System.getProperty("user.dir") + System.getProperty("file.separator") + WorkbenchMessages.getString("ImportExportPages.preferenceFileName") + AbstractPreferenceImportExportPage.PREFERENCE_EXT; //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$

		} else if ((export) || (new File(lastFileName).exists())) {
			return lastFileName;
		}

		return null;
	}

	/**
	 * Handle a request to save preferences
	 */
	protected void savePressed() {
		new PreferencesExportDialog(getShell()).open();
		close();
	}

	/**
	 * Import a preference file.
	 * 
	 * @param path
	 *            The file path.
	 * @return true if successful.
	 */
	private boolean importPreferences(IPath path) {
		IStatus status = Preferences.validatePreferenceVersions(path);
		if (status.getSeverity() == IStatus.ERROR) {
			// Show the error and about
			ErrorDialog.openError(getShell(), WorkbenchMessages
					.getString("WorkbenchPreferenceDialog.loadErrorTitle"), //$NON-NLS-1$
					WorkbenchMessages.format(
							"WorkbenchPreferenceDialog.verifyErrorMessage", //$NON-NLS-1$
							new Object[] { path.toOSString() }), status);
			return false;
		} else if (status.getSeverity() == IStatus.WARNING) {
			// Show the warning and give the option to continue
			int result = PreferenceErrorDialog
					.openError(
							getShell(),
							WorkbenchMessages
									.getString("WorkbenchPreferenceDialog.loadErrorTitle"), //$NON-NLS-1$
							WorkbenchMessages
									.format(
											"WorkbenchPreferenceDialog.verifyWarningMessage", //$NON-NLS-1$
											new Object[] { path.toOSString() }),
							status);
			if (result != Window.OK) {
				return false;
			}
		}

		try {
			Preferences.importPreferences(path);
		} catch (CoreException e) {
			ErrorDialog.openError(getShell(), WorkbenchMessages
					.getString("WorkbenchPreferenceDialog.loadErrorTitle"), //$NON-NLS-1$
					WorkbenchMessages.format(
							"WorkbenchPreferenceDialog.loadErrorMessage", //$NON-NLS-1$
							new Object[] { path.toOSString() }), e.getStatus());
			return false;
		}
		return true;
	}
   
    /**
     * Returns the currently selected page. This can be used in conjuction with
     * <code>createDialogOn</code> to create a dialog, manipulate the
     * preference page, and then display it to the user.
     * 
     * @return The currently selected page; this value may be <code>null</code>
     *         if there is no selected page.
     * @since 3.1
     */
    public final IPreferencePage getCurrentPage() {
        return super.getCurrentPage();
    }

    /**
     * Selects the current page based on the given preference page identifier.
     * If no node can be found, then nothing will change.
     * 
     * @param preferencePageId
     *            The preference page identifier to select; should not be
     *            <code>null</code>.
     */
    public final void setCurrentPageId(final String preferencePageId) {
        final IPreferenceNode node = findNodeMatching(preferencePageId);
        if (node != null) {
            getTreeViewer().setSelection(new StructuredSelection(node));
            showPage(node);
        }
    }
}