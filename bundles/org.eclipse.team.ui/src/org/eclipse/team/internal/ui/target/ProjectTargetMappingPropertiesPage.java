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

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.internal.core.target.Site;
import org.eclipse.team.internal.core.target.TargetManager;
import org.eclipse.team.internal.core.target.TargetProvider;
import org.eclipse.team.internal.core.target.UrlUtil;
import org.eclipse.team.internal.ui.Policy;
import org.eclipse.ui.dialogs.PropertyPage;

public class ProjectTargetMappingPropertiesPage extends PropertyPage {
	public static final int WIDTH_HINT = 250;

	private IProject proj;
	private Site site = null;
	private IPath mapping;
	private TargetProvider origProvider=null;
	
	private Label serverText,folderText;

	/**
	 * @see org.eclipse.jface.preference.PreferencePage#createContents(Composite)
	 */
	protected Control createContents(Composite parent) {
		initialize();
		Composite container = new Composite(parent, SWT.NONE);
		container.setLayout(new GridLayout(2, false));

		Label serverLabel = new Label(container, SWT.NONE);
		serverLabel.setText(Policy.bind("ProjectTargetMappingPropertiesPage.URL")); //$NON-NLS-1$
		serverText = new Label(container, SWT.NONE);
		serverText.setText(site.getURL().toString());
		GridData data = new GridData();
		data.widthHint = WIDTH_HINT;
		serverText.setLayoutData(data);

		Label folderLabel = new Label(container, SWT.NONE);
		folderLabel.setText(Policy.bind("ProjectTargetMappingPropertiesPage.FolderName")); //$NON-NLS-1$
		folderText = new Label(container, SWT.NONE);
		folderText.setText(mapping.toString());
		data = new GridData();
		data.widthHint = WIDTH_HINT;
		folderText.setLayoutData(data);

		Button change = new Button(container, SWT.PUSH);
		change.setText(Policy.bind("ProjectTargetMappingPropertiesPage.ChangeBtn")); //$NON-NLS-1$
		change.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				ConfigureTargetWizard wizard = new ConfigureTargetWizard();
				wizard.init(null, proj);
				WizardDialog dialog = new WizardDialog(getShell(), wizard);
				dialog.open();
				refreshInfo();
			}
		});

		return container;
	}

	private void initialize() {
		IAdaptable element = getElement();
		if (element instanceof IProject) {
			proj = ((IProject) element).getProject();
		} else {
			IProject adapter = (IProject) element.getAdapter(IProject.class);
			if (adapter != null) {
				proj = adapter.getProject();
			}
		}
		try {
			TargetProvider provider = TargetManager.getProvider(proj);
			if (origProvider==null) origProvider=provider;
			site = provider.getSite();
			mapping = UrlUtil.getTrailingPath(provider.getURL(), site.getURL());
		} catch (TeamException e) {
			ErrorDialog.openError(getShell(), Policy.bind("ProjectTargetMappingPropertiesPage.Error"), null, e.getStatus()); //$NON-NLS-1$
		}
	}
	
	private void refreshInfo() {
		initialize();
		serverText.setText(site.getURL().toString());
		folderText.setText(mapping.toString());
	}

	/**
	 * @see org.eclipse.jface.preference.PreferencePage#performDefaults()
	 */
	protected void performDefaults() {
		super.performDefaults();
		IPath oldMapping = UrlUtil.getTrailingPath(origProvider.getURL(), origProvider.getSite().getURL());
		try {
			TargetManager.unmap(proj);
			TargetManager.map(proj, origProvider.getSite(), oldMapping);
		} catch (TeamException e) {
			ErrorDialog.openError(getShell(), Policy.bind("ProjectTargetMappingPropertiesPage.Error"), null, e.getStatus()); //$NON-NLS-1$
		}
		refreshInfo();
	}

}
