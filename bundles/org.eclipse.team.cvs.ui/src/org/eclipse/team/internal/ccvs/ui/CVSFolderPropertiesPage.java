/*******************************************************************************
 * Copyright (c) 2002 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 * IBM - Initial API and implementation
 ******************************************************************************/
package org.eclipse.team.internal.ccvs.ui;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.internal.ccvs.core.CVSTag;
import org.eclipse.team.internal.ccvs.core.ICVSFolder;
import org.eclipse.team.internal.ccvs.core.resources.CVSWorkspaceRoot;
import org.eclipse.team.internal.ccvs.core.syncinfo.FolderSyncInfo;
import org.eclipse.ui.dialogs.PropertyPage;
import org.eclipse.ui.help.WorkbenchHelp;

public class CVSFolderPropertiesPage extends PropertyPage {

	IFolder folder;
	
	/**
	 * @see PreferencePage#createContents(Composite)
	 */
	protected Control createContents(Composite parent) {
		initialize();
		noDefaultAndApplyButton();
		Composite composite = new Composite(parent, SWT.NULL);
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		composite.setLayout(layout);
		
		try {
			ICVSFolder cvsResource = CVSWorkspaceRoot.getCVSFolderFor(folder);
			if (!cvsResource.isManaged()) {
				if (cvsResource.isIgnored()) {
					createLabel(composite, Policy.bind("CVSFolderPropertiesPage.ignored"), 2); //$NON-NLS-1$
				} else {
					createLabel(composite, Policy.bind("CVSFolderPropertiesPage.notManaged"), 2); //$NON-NLS-1$
				}
			} else {
				FolderSyncInfo syncInfo = cvsResource.getFolderSyncInfo();
				createLabel(composite, Policy.bind("CVSFolderPropertiesPage.root")); //$NON-NLS-1$
				createLabel(composite, syncInfo.getRoot());
				createLabel(composite, Policy.bind("CVSFolderPropertiesPage.repository")); //$NON-NLS-1$
				createLabel(composite, syncInfo.getRepository());
			
				// Tag
				createLabel(composite, Policy.bind("CVSFilePropertiesPage.tag")); //$NON-NLS-1$
				CVSTag tag = syncInfo.getTag();
				if (tag == null) {
					createLabel(composite, Policy.bind("CVSFilePropertiesPage.none")); //$NON-NLS-1$
				} else {
					switch (tag.getType()) {
						case CVSTag.HEAD:
							createLabel(composite, tag.getName());
							break;
						case CVSTag.VERSION:
							createLabel(composite, Policy.bind("CVSFilePropertiesPage.version", tag.getName())); //$NON-NLS-1$
							break;
						case CVSTag.BRANCH:
							createLabel(composite, Policy.bind("CVSFilePropertiesPage.branch", tag.getName())); //$NON-NLS-1$
							break;
						case CVSTag.DATE:
							createLabel(composite, Policy.bind("CVSFilePropertiesPage.date", tag.getName())); //$NON-NLS-1$
							break;
					}
				}
				
				// Static-ness
				if (syncInfo.getIsStatic()) {
					createLabel(composite, Policy.bind("CVSFolderPropertiesPage.static")); //$NON-NLS-1$
					createLabel(composite, syncInfo.getIsStatic() ? Policy.bind("yes") : Policy.bind("no")); //$NON-NLS-1$ //$NON-NLS-2$
				}
				
			}
		} catch (TeamException e) {
			// Display error text
			createLabel(composite, Policy.bind("CVSFilePropertiesPage.error"), 2); //$NON-NLS-1$
		}
		WorkbenchHelp.setHelp(composite, IHelpContextIds.FOLDER_PROPERTY_PAGE);
		return composite;
	}

	/**
	 * Utility method that creates a label instance
	 * and sets the default layout data.
	 *
	 * @param parent  the parent for the new label
	 * @param text  the text for the new label
	 * @return the new label
	 */
	protected Label createLabel(Composite parent, String text, int span) {
		Label label = new Label(parent, SWT.LEFT);
		label.setText(text);
		GridData data = new GridData();
		data.horizontalSpan = span;
		data.horizontalAlignment = GridData.FILL;
		label.setLayoutData(data);
		return label;
	}
	
	protected Label createLabel(Composite parent, String text) {
		return createLabel(parent, text, 1);
	}
	/**
	 * Initializes the page
	 */
	private void initialize() {
		// Get the file that is the source of this property page
		folder = null;
		IAdaptable element = getElement();
		if (element instanceof IFolder) {
			folder = (IFolder)element;
		} else {
			Object adapter = element.getAdapter(IFolder.class);
			if (adapter instanceof IFolder) {
				folder = (IFolder)adapter;
			}
		}
	}

}
