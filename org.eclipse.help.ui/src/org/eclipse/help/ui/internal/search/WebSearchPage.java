/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.help.ui.internal.search;

import org.eclipse.help.ui.RootScopePage;
import org.eclipse.help.ui.internal.Messages;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/**
 * Web serch participant in the federated search.
 */
public class WebSearchPage extends RootScopePage {
	private Text urlText;

	/**
	 * Default constructor.
	 */
	public WebSearchPage() {
	}

	protected int createScopeContents(Composite parent) {
		//Font font = parent.getFont();
		initializeDialogUnits(parent);

		Label label = new Label(parent, SWT.NULL);
		label.setText(Messages.WebSearchPage_label); 
		GridData gd = new GridData(GridData.VERTICAL_ALIGN_BEGINNING);
		label.setLayoutData(gd);
		urlText = new Text(parent, SWT.MULTI | SWT.WRAP | SWT.V_SCROLL
				| SWT.BORDER);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.heightHint = 64;
		gd.widthHint = 200;
		urlText.setLayoutData(gd);
		urlText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				validate();
			}
		});
		urlText.setEditable(getEngineDescriptor().isUserDefined());
		new Label(parent, SWT.NULL);
		label = new Label(parent, SWT.WRAP);
		label.setText(Messages.WebSearchPage_info); 
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.widthHint = 100;
		label.setLayoutData(gd);
		applyDialogFont(parent);
		updateControls();
		return 2;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.help.ui.RootScopePage#initializeDefaults(org.eclipse.jface.preference.IPreferenceStore)
	 */
	protected void initializeDefaults(IPreferenceStore store) {
		super.initializeDefaults(store);
		String template = (String) getEngineDescriptor().getParameters().get(
				WebSearchScopeFactory.P_URL);
		if (template != null)
			store
					.setDefault(getStoreKey(WebSearchScopeFactory.P_URL),
							template);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.PreferencePage#performDefaults()
	 */
	protected void performDefaults() {
		getPreferenceStore().setToDefault(
				getStoreKey(WebSearchScopeFactory.P_URL));
		updateControls();
		super.performDefaults();
	}

	private void updateControls() {
		String template = getPreferenceStore().getString(
				getStoreKey(WebSearchScopeFactory.P_URL));
		urlText.setText(template != null ? template : "http://"); //$NON-NLS-1$
		validate();
	}

	private void validate() {
		String text = urlText.getText();
		setValid(text.length() > 0);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.IPreferencePage#performOk()
	 */
	public boolean performOk() {
		String urlTemplate = urlText.getText();
		getPreferenceStore().setValue(getStoreKey(WebSearchScopeFactory.P_URL),
				urlTemplate);
		return super.performOk();
	}

	private String getStoreKey(String key) {
		return getEngineDescriptor().getId() + "." + key; //$NON-NLS-1$
	}
}
