/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.ccvs.ui;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public class WorkInProgressPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {
	
	private BooleanFieldEditor bkgFetch = null;
	
	public WorkInProgressPreferencePage() {
		super(GRID);
		setTitle("CVS Work In Progress");
		setDescription("Preferences for enabling features that are not copmlete:");
		setPreferenceStore(CVSUIPlugin.getPlugin().getPreferenceStore());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createControl(Composite parent) {
		super.createControl(parent);
		//WorkbenchHelp.setHelp(getControl(), IDebugHelpContextIds.CONSOLE_PREFERENCE_PAGE);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.FieldEditorPreferencePage#createFieldEditors()
	 */
	public void createFieldEditors() {		
		bkgFetch = new BooleanFieldEditor(ICVSUIConstants.BACKGROUND_REPOVIEW, "Enable background fetching of resources in the CVS Repositories view", SWT.NONE, getFieldEditorParent());
		addField(bkgFetch);		
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	public void init(IWorkbench workbench) {
	}		
}