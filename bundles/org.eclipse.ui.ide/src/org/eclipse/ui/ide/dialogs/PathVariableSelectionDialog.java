/*******************************************************************************
 * Copyright (c) 2005, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.ide.dialogs;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileInfo;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.filesystem.URIUtil;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.window.Window;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.SelectionDialog;
import org.eclipse.ui.internal.ide.IDEWorkbenchMessages;
import org.eclipse.ui.internal.ide.IIDEHelpContextIds;
import org.eclipse.ui.internal.ide.dialogs.FileFolderSelectionDialog;
import org.eclipse.ui.internal.ide.dialogs.IDEResourceInfoUtils;
import org.eclipse.ui.internal.ide.dialogs.PathVariablesGroup;

/**
 * A selection dialog which shows the path variables defined in the workspace.
 * The <code>getResult</code> method returns the name(s) of the selected path
 * variable(s).
 * <p>
 * This class may be instantiated; it is not intended to be subclassed.
 * </p>
 * <p>
 * Example:
 * </p>
 *
 * <pre>
 * PathVariableSelectionDialog dialog = new PathVariableSelectionDialog(getShell(), IResource.FOLDER);
 * dialog.open();
 * String[] result = (String[]) dialog.getResult();
 * </pre>
 *
 * @since 3.1
 */
public final class PathVariableSelectionDialog extends SelectionDialog {
	private static final int EXTEND_ID = IDialogConstants.CLIENT_ID + 1;

	private PathVariablesGroup pathVariablesGroup;

	private IResource currentResource = null;

	private int variableType;

	/**
	 * Creates a path variable selection dialog.
	 *
	 * @param parentShell the parent shell
	 * @param variableType the type of variables that are displayed in
	 * 	this dialog. <code>IResource.FILE</code> and/or <code>IResource.FOLDER</code>
	 * 	logically ORed together.
	 */
	public PathVariableSelectionDialog(Shell parentShell, int variableType) {
		super(parentShell);
		setTitle(IDEWorkbenchMessages.PathVariableSelectionDialog_title);
		this.variableType = variableType;
		pathVariablesGroup = new PathVariablesGroup(false, variableType,
				event -> updateExtendButtonState());
		pathVariablesGroup.setSaveVariablesOnChange(true);
		setShellStyle(getShellStyle() | SWT.SHEET);
	}

	@Override
	protected void buttonPressed(int buttonId) {
		if (buttonId == EXTEND_ID) {
			PathVariablesGroup.PathVariableElement selection = pathVariablesGroup
					.getSelection()[0];
			FileFolderSelectionDialog dialog = new FileFolderSelectionDialog(
					getShell(), false, variableType);
			dialog
					.setTitle(IDEWorkbenchMessages.PathVariableSelectionDialog_ExtensionDialog_title);
			dialog
					.setMessage(NLS
							.bind(
									IDEWorkbenchMessages.PathVariableSelectionDialog_ExtensionDialog_description,
									selection.name));
			// XXX This only works for variables that refer to local file
			// system locations
			IPath selectionPath = selection.path;
			if (currentResource != null)
				selectionPath = URIUtil.toPath(currentResource.getPathVariableManager()
						.resolveURI(URIUtil.toURI(selectionPath)));
			try {
					dialog.setInput(EFS.getStore(URIUtil.toURI(selectionPath)));
			} catch (CoreException e) {
				ErrorDialog.openError(getShell(), null, null, e.getStatus());
			}
			if (dialog.open() == Window.OK
					&& pathVariablesGroup.performOk()) {
				setExtensionResult(selection, (IFileStore) dialog.getResult()[0]);
				super.okPressed();
			}
		} else {
			super.buttonPressed(buttonId);
		}
	}

	@Override
	protected void configureShell(Shell shell) {
		super.configureShell(shell);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(shell,
				IIDEHelpContextIds.PATH_VARIABLE_SELECTION_DIALOG);
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL,
				true);
		createButton(parent, EXTEND_ID, IDEWorkbenchMessages.PathVariableSelectionDialog_extendButton, false);
		createButton(parent, IDialogConstants.CANCEL_ID,
				IDialogConstants.CANCEL_LABEL, false);
		updateExtendButtonState();
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		// create composite
		Composite dialogAreaComposite = (Composite) super.createDialogArea(parent);

		pathVariablesGroup.createContents(dialogAreaComposite);
		return dialogAreaComposite;
	}


	@Override
	public boolean close() {
		pathVariablesGroup.dispose();
		return super.close();
	}


	@Override
	protected void okPressed() {
		//Sets the dialog result to the selected path variable name(s).
		if (pathVariablesGroup.performOk()) {
			PathVariablesGroup.PathVariableElement[] selection = pathVariablesGroup
					.getSelection();
			String[] variableNames = new String[selection.length];

			for (int i = 0; i < selection.length; i++) {
				variableNames[i] = selection[i].name;
			}
			setSelectionResult(variableNames);
		} else {
			setSelectionResult(null);
		}
		super.okPressed();
	}

	/**
	 * Sets the dialog result to the concatenated variable name and extension.
	 *
	 * @param variable variable selected in the variables list and extended
	 * 	by <code>extensionFile</code>
	 * @param extensionFile file selected to extend the variable.
	 */
	private void setExtensionResult(
			PathVariablesGroup.PathVariableElement variable, IFileStore extensionFile) {
		IPath extensionPath = IPath.fromOSString(extensionFile.toString());
		IPath selectionPath = variable.path;
		if (currentResource != null)
			selectionPath = URIUtil.toPath(currentResource.getPathVariableManager().resolveURI(URIUtil.toURI(selectionPath)));
		int matchCount = extensionPath.matchingFirstSegments(selectionPath);
		IPath resultPath = IPath.fromOSString(variable.name);

		extensionPath = extensionPath.removeFirstSegments(matchCount);
		resultPath = resultPath.append(extensionPath);
		setSelectionResult(new String[] { resultPath.toPortableString() });
	}

	/**
	 * Updates the enabled state of the Extend button based on the
	 * current variable selection.
	 */
	private void updateExtendButtonState() {
		PathVariablesGroup.PathVariableElement[] selection = pathVariablesGroup
				.getSelection();
		Button extendButton = getButton(EXTEND_ID);

		if (extendButton == null) {
			return;
		}
		if (selection.length == 1) {
			IPath selectionPath = selection[0].path;
			if (currentResource != null)
				selectionPath = URIUtil.toPath(currentResource.getPathVariableManager().resolveURI(URIUtil.toURI(selectionPath)));
			IFileInfo info = IDEResourceInfoUtils.getFileInfo(selectionPath);
//			IPathVariable pathVariable = null;
//			if (currentResource != null)
//				pathVariable = currentResource.getPathVariableManager().getPathVariable(selection[0].name);
			if (info.exists() && info.isDirectory()
					/*|| (pathVariable != null && pathVariable.getExtensions() != null)*/) {
				extendButton.setEnabled(true);
			} else {
				extendButton.setEnabled(false);
			}

		} else {
			extendButton.setEnabled(false);
		}
	}

	/**
	 * Sets the resource for which the path variable is being edited.
	 *
	 * @param resource the resource
	 * @since 3.6
	 */
	public void setResource(IResource resource) {
		currentResource = resource;
		pathVariablesGroup.setResource(resource);
	}
}
