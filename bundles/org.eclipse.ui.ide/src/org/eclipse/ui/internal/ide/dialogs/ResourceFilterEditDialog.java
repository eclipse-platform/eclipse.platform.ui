/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Serge Beauchamp (Freescale Semiconductor) - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.ide.dialogs;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResourceFilterDescription;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.SelectionDialog;
import org.eclipse.ui.ide.dialogs.UIResourceFilterDescription;
import org.eclipse.ui.internal.ide.IDEWorkbenchMessages;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;
import org.eclipse.ui.internal.ide.IIDEHelpContextIds;

/**
 * @since 3.6
 */
public class ResourceFilterEditDialog extends SelectionDialog {

	private ResourceFilterGroup resourceFilterGroup;

	/**
	 * Creates a resource filter edit dialog.
	 *
	 * @param parentShell
	 *            the parent shell
	 */
	public ResourceFilterEditDialog(Shell parentShell) {
		super(parentShell);
		setTitle(IDEWorkbenchMessages.ResourceFilterEditDialog_title);
		resourceFilterGroup = new ResourceFilterGroup();
		setShellStyle(getShellStyle() | SWT.RESIZE);
	}

	/**
	 * Set the container resource to be edited.
	 */
	public void setContainer(IContainer container) {
		resourceFilterGroup.setContainer(container);
	}

	@Override
	protected void buttonPressed(int buttonId) {
		super.buttonPressed(buttonId);
	}

	@Override
	protected void configureShell(Shell shell) {
		super.configureShell(shell);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(shell,
				IIDEHelpContextIds.EDIT_RESOURCE_FILTER_DIALOG);
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL,
				true);
		createButton(parent, IDialogConstants.CANCEL_ID,
				IDialogConstants.CANCEL_LABEL, false);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite dialogAreaComposite = (Composite) super.createDialogArea(parent);
		resourceFilterGroup.createContents(dialogAreaComposite);
		return dialogAreaComposite;
	}

	@Override
	public boolean close() {
		resourceFilterGroup.dispose();
		return super.close();
	}

	/**
	 * @return the filters that were configured on this resource
	 */
	public UIResourceFilterDescription[] getFilters() {
		return resourceFilterGroup.getFilters();
	}

	/**
	 * @param filters
	 *            the initial filters of the dialog
	 */
	public void setFilters(UIResourceFilterDescription[] filters) {
		resourceFilterGroup.setFilters(filters);
	}

	/**
	 * @param filters
	 *            the initial filters of the dialog
	 */
	public void setFilters(IResourceFilterDescription[] filters) {
		resourceFilterGroup.setFilters(filters);
	}

	@Override
	protected void okPressed() {
		// Sets the dialog result to the selected path variable name(s).
		try {
			if (resourceFilterGroup.performOk())
				super.okPressed();
		} catch (Throwable t) {
			IDEWorkbenchPlugin.log(t.getMessage(), t);
		}
	}
}
