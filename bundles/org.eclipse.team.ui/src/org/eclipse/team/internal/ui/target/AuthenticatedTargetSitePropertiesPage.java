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
package org.eclipse.team.internal.ui.target;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.internal.core.target.AuthenticatedSite;
import org.eclipse.team.internal.core.target.Site;
import org.eclipse.team.internal.ui.Policy;
import org.eclipse.ui.dialogs.PropertyPage;

public class AuthenticatedTargetSitePropertiesPage extends PropertyPage {
	public static final int WIDTH_HINT = 250;
	
	private Site site;
	private Text nameText, passwordText;

	/**
	 * Initializes the page
	 */
	private void initialize() {
		IAdaptable element = getElement();
		if (element instanceof SiteElement) {
			site = ((SiteElement)element).getSite();
		} else {
			SiteElement adapter = (SiteElement)element.getAdapter(SiteElement.class);
			if (adapter != null) {
				site = adapter.getSite();
			}
		}
	}

	/**
	 * @see org.eclipse.jface.preference.PreferencePage#createContents(Composite)
	 */
	protected Control createContents(Composite parent) {
		initialize();
		Composite container = new Composite(parent, SWT.NONE);
		container.setLayout(new GridLayout(2, false));

		Label serverLabel = new Label(container, SWT.NONE);
		serverLabel.setText(Policy.bind("AuthenticatedTargetSitePropertiesPage.Server")); //$NON-NLS-1$
		serverLabel.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
		Label serverText = new Label(container, SWT.NONE);
		serverText.setText(site.getURL().getHost());
		GridData data = new GridData();
		data.widthHint = WIDTH_HINT;
		serverText.setLayoutData(data);

		Label nameLabel = new Label(container, SWT.NONE);
		nameLabel.setText(Policy.bind("AuthenticatedTargetSitePropertiesPage.User")); //$NON-NLS-1$
		nameLabel.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
		nameText = new Text(container, SWT.SINGLE | SWT.BORDER);
		data = new GridData();
		data.widthHint = WIDTH_HINT;
		nameText.setLayoutData(data);
		String name=((AuthenticatedSite)site).getUsername();
		if (name!=null) nameText.setText(name);

		Label passwordLabel = new Label(container, SWT.NONE);
		passwordLabel.setText(Policy.bind("AuthenticatedTargetSitePropertiesPage.Password")); //$NON-NLS-1$
		passwordLabel.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
		passwordText = new Text(container, SWT.SINGLE | SWT.BORDER);
		passwordText.setEchoChar('*');
		passwordText.setText(""); //$NON-NLS-1$
		data = new GridData();
		data.widthHint = WIDTH_HINT;
		passwordText.setLayoutData(data);

		return container;
	}

	/**
	 * @see org.eclipse.jface.preference.IPreferencePage#performOk()
	 */
	public boolean performOk() {
		String name = nameText.getText();
		String password = passwordText.getText();
		try {
			if (name!=null && name.length()>0) ((AuthenticatedSite)site).setUsername(name);
			if (password!=null && password.length()>0) ((AuthenticatedSite)site).setPassword(password);
			return true;
		} catch (TeamException e) {
			ErrorDialog.openError(getShell(), Policy.bind("AuthenticatedTargetSitePropertiesPage.Error"), null, e.getStatus()); //$NON-NLS-1$
			return false;
		}
	}

}
