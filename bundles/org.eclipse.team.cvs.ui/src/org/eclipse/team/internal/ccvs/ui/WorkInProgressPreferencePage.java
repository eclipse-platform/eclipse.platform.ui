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
import org.eclipse.team.internal.ccvs.ui.Policy;

public class WorkInProgressPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {
	
	public WorkInProgressPreferencePage() {
		super(GRID);
		setTitle(Policy.bind("WorkInProgressPreferencePage.0")); //$NON-NLS-1$
		setDescription(Policy.bind("WorkInProgressPreferencePage.1")); //$NON-NLS-1$
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
		addField(new BooleanFieldEditor(ICVSUIConstants.BACKGROUND_REPOVIEW, Policy.bind("WorkInProgressPreferencePage.2"), SWT.NONE, getFieldEditorParent()));	 //$NON-NLS-1$
		addField(new BooleanFieldEditor(ICVSUIConstants.BACKGROUND_OPERATIONS, Policy.bind("WorkInProgressPreferencePage.3"), SWT.NONE, getFieldEditorParent())); //$NON-NLS-1$
		addField(new BooleanFieldEditor(ICVSUIConstants.USE_NEW_SYNCVIEW, Policy.bind("WorkInProgressPreferencePage.4"), SWT.NONE, getFieldEditorParent())); //$NON-NLS-1$
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