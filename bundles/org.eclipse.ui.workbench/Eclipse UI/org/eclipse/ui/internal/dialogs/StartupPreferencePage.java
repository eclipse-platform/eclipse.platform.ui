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
package org.eclipse.ui.internal.dialogs;

import org.eclipse.core.runtime.IPluginDescriptor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.internal.*;

public class StartupPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {
	private Table pluginsList;
	private Workbench workbench;
	
	/**
	 * @see PreferencePage#createContents(Composite)
	 */
	protected Control createContents(Composite parent) {
		WorkbenchHelp.setHelp(parent, IHelpContextIds.STARTUP_PREFERENCE_PAGE);
		
		Font font = parent.getFont();
		
		Composite composite = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		composite.setLayout(layout);
		GridData data = new GridData(GridData.FILL_BOTH | GridData.VERTICAL_ALIGN_FILL | GridData.HORIZONTAL_ALIGN_FILL);
		composite.setLayoutData(data);
		composite.setFont(font);

		Label label = new Label(composite,SWT.NONE);
		label.setText(WorkbenchMessages.getString("StartupPreferencePage.label")); //$NON-NLS-1$
		label.setFont(font);
		data = new GridData(GridData.FILL_HORIZONTAL);
		label.setLayoutData(data);		
		pluginsList = new Table(composite,SWT.BORDER | SWT.CHECK | SWT.H_SCROLL | SWT.V_SCROLL);
		data = new GridData(GridData.FILL_BOTH);
		pluginsList.setFont(font)	;
		pluginsList.setLayoutData(data);
		populatePluginsList();

		return composite;
	}
	private void populatePluginsList() {
		IPluginDescriptor descriptors[] = workbench.getEarlyActivatedPlugins();
		IPreferenceStore store = workbench.getPreferenceStore();
		String pref = store.getString(IPreferenceConstants.PLUGINS_NOT_ACTIVATED_ON_STARTUP);
		if(pref == null)
			pref = new String();
		for (int i = 0; i < descriptors.length; i++) {
			IPluginDescriptor desc = descriptors[i];
			TableItem item = new TableItem(pluginsList,SWT.NONE);
			item.setText(desc.getLabel());
			item.setData(desc);
			String id = desc.getUniqueIdentifier() + IPreferenceConstants.SEPARATOR;
			item.setChecked(pref.indexOf(id) < 0);
		}
	}
	/**
	 * @see IWorkbenchPreferencePage
	 */
	public void init(IWorkbench workbench) {
		this.workbench = (Workbench)workbench;
	}
	/**
	 * @see PreferencePage
	 */
	protected void performDefaults() {
		TableItem items[] = pluginsList.getItems();
		for (int i = 0; i < items.length; i++) {
			items[i].setChecked(true);
		}
	}
	/**
	 * @see PreferencePage
	 */
	public boolean performOk() {
		StringBuffer preference = new StringBuffer();
		TableItem items[] = pluginsList.getItems();
		for (int i = 0; i < items.length; i++) {
			if(!items[i].getChecked()) {
				IPluginDescriptor descriptor = (IPluginDescriptor)items[i].getData();
				preference.append(descriptor.getUniqueIdentifier());
				preference.append(IPreferenceConstants.SEPARATOR);
			}
		}
		String pref = preference.toString();
		IPreferenceStore store = workbench.getPreferenceStore();
		store.putValue(IPreferenceConstants.PLUGINS_NOT_ACTIVATED_ON_STARTUP,pref);
		return true;
	}
}
