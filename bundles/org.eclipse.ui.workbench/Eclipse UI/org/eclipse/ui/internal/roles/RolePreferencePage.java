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
package org.eclipse.ui.internal.roles;

import java.util.Arrays;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.viewers.CheckboxTableViewer;

import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;


public class RolePreferencePage extends PreferencePage implements IWorkbenchPreferencePage {

	CheckboxTableViewer viewer;

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.PreferencePage#createContents(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createContents(Composite parent) {
		Composite composite = new Composite(parent, SWT.NULL);
		composite.setLayout(new FillLayout());						
		viewer = CheckboxTableViewer.newCheckList(composite, SWT.BORDER);
		viewer.setContentProvider(new RoleContentProvider());
		viewer.setLabelProvider(new RoleLabelProvider());	
		viewer.setInput(RoleManager.getInstance());
		loadRoleState();	
		return composite;
	}

	private void loadRoleState() {
		Role [] roles = RoleManager.getInstance().getRoles();	
		for (int i = 0; i < roles.length; i++) {
			viewer.setChecked(roles[i], roles[i].enabled);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	public void init(IWorkbench workbench) {
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.PreferencePage#performApply()
	 */
	public boolean performOk() {
		List checked = Arrays.asList(viewer.getCheckedElements());
		Role [] roles = RoleManager.getInstance().getRoles();
		for (int i = 0; i < roles.length; i++) {
			roles[i].enabled = checked.contains(roles[i]);
		}
		RoleManager.getInstance().saveEnabledStates();
		return super.performOk();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.PreferencePage#performDefaults()
	 */
	protected void performDefaults() {
		loadRoleState();
	}
}
