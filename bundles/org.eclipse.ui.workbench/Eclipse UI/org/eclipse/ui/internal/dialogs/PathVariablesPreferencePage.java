/**********************************************************************
 * Copyright (c) 2000,2002 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 *	 IBM - Initial API and implementation
 **********************************************************************/

package org.eclipse.ui.internal.dialogs;

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.internal.WorkbenchMessages;

/**
 * Preference page for path variables. This preference page shows all path
 * variables currently defined in the workspace's path variable manager. It
 * allows the user to add, edit and remove path variables. The changes are kept
 * in temporary collections, so only when the user confirms them (by confirming
 * when closing the "Preferences" dialog) all changes are effectively commited
 * to the path variable manager.
 * 
 * @see org.eclipse.ui.internal.dialogs.PathVariableDialog
 */
public class PathVariablesPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {

	private PathVariablesGroup pathVariablesGroup;

	/**
	 * Constructs a preference page of path variables.
	 * Omits "Restore Defaults"/"Apply Changes" buttons.
	 */
	public PathVariablesPreferencePage() {
		pathVariablesGroup = new PathVariablesGroup(true, IResource.FILE | IResource.FOLDER);

		this.noDefaultAndApplyButton();
	}
	/**
	 * Resets this page's internal state and creates its UI contents.
	 * 
	 * @see PreferencePage#createContents(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createContents(Composite parent) {
		Font font = parent.getFont();

		// define container & its gridding
		Composite pageComponent = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		pageComponent.setLayout(layout);
		GridData data = new GridData();
		data.verticalAlignment = GridData.FILL;
		data.horizontalAlignment = GridData.FILL;
		pageComponent.setLayoutData(data);
		pageComponent.setFont(font);

		//layout the contents
		Label topLabel = new Label(pageComponent, SWT.NONE);
		topLabel.setText(WorkbenchMessages.getString("PathVariablesPreference.explanation")); //$NON-NLS-1$
		data = new GridData();
		data.verticalAlignment = GridData.FILL;
		data.horizontalAlignment = GridData.FILL;
		topLabel.setLayoutData(data);
		topLabel.setFont(font);

		pathVariablesGroup.createContents(pageComponent);
		return pageComponent;
	}
	/**
	 * Disposes the path variables group.
	 * @see org.eclipse.jface.dialogs.IDialogPage#dispose()
	 */
	public void dispose() {
		pathVariablesGroup.dispose();
		super.dispose();
	}
	/**
	 * Empty implementation. This page does not use the workbench.
	 * 
	 * @see IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	public void init(IWorkbench workbench) {
	}
	/**
	 * Commits the temporary state to the path variable manager in response to user
	 * confirmation.
	 * 
	 * @see PreferencePage#performOk()
	 * @see PathVariablesGroup#performOk()
	 */
	public boolean performOk() {
		return pathVariablesGroup.performOk();
	}
}
