/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.ccvs.ui;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.*;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.internal.ccvs.core.*;
import org.eclipse.team.internal.ccvs.core.resources.CVSWorkspaceRoot;
import org.eclipse.team.internal.ccvs.core.syncinfo.FolderSyncInfo;
import org.eclipse.team.internal.ccvs.core.util.Util;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.help.WorkbenchHelp;

public class CVSFolderPropertiesPage extends CVSPropertiesPage {

	IFolder folder;
	private Label root;
	private Label repository;
	
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
			} else if (!cvsResource.isCVSFolder()) {
				// We have a managed folder which is not a cvs folder. 
				// This is really an invalid state but it does happen once in a while
				createLabel(composite, Policy.bind("CVSFolderPropertiesPage.notCVSFolder"), 2); //$NON-NLS-1$
			} else {
				FolderSyncInfo syncInfo = cvsResource.getFolderSyncInfo();
				createLabel(composite, Policy.bind("CVSFolderPropertiesPage.root")); //$NON-NLS-1$
				root = createLabel(composite, syncInfo.getRoot());
				createLabel(composite, Policy.bind("CVSFolderPropertiesPage.repository")); //$NON-NLS-1$
				repository = createLabel(composite, syncInfo.getRepository());
			
				// Tag
				createLabel(composite, Policy.bind("CVSFilePropertiesPage.tag")); //$NON-NLS-1$
				CVSTag tag = syncInfo.getTag();

				if (tag != null && tag.getType() == CVSTag.BRANCH) {
					tag = Util.getAccurateFolderTag(folder, tag);				
				}
			
				createLabel(composite, getTagLabel(tag));
				
				// Static-ness
				if (syncInfo.getIsStatic()) {
					createLabel(composite, Policy.bind("CVSFolderPropertiesPage.static")); //$NON-NLS-1$
					createLabel(composite, syncInfo.getIsStatic() ? Policy.bind("yes") : Policy.bind("no")); //$NON-NLS-1$ //$NON-NLS-2$
				}
				
				createLabel(composite, "", 2); // spacer //$NON-NLS-1$
				
				// Allow the folder to be disconnected from CVS control
				final Button disconnect = new Button(composite, SWT.NONE);
				disconnect.setText(Policy.bind("CVSFolderPropertiesPage.disconnect")); //$NON-NLS-1$
				GridData data = new GridData(GridData.HORIZONTAL_ALIGN_END);
				data.heightHint = convertVerticalDLUsToPixels(IDialogConstants.BUTTON_HEIGHT);
				int widthHint = convertHorizontalDLUsToPixels(IDialogConstants.BUTTON_WIDTH);
				data.widthHint = Math.max(widthHint, disconnect.computeSize(SWT.DEFAULT, SWT.DEFAULT, true).x);
				disconnect.setLayoutData(data);
				disconnect.addListener(SWT.Selection, new Listener() {
					public void handleEvent (Event event) {
						// perform a disconnect
						if (disconnectFolder()) {
							root.setText(Policy.bind("CVSFilePropertiesPage.none")); //$NON-NLS-1$
							repository.setText(Policy.bind("CVSFilePropertiesPage.none")); //$NON-NLS-1$
							disconnect.setEnabled(false);
						}
					}
				});
			}
		} catch (TeamException e) {
			// Display error text
			createLabel(composite, Policy.bind("CVSFilePropertiesPage.error"), 2); //$NON-NLS-1$
		}
		WorkbenchHelp.setHelp(getControl(), IHelpContextIds.FOLDER_PROPERTY_PAGE);
        Dialog.applyDialogFont(parent);
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

	private boolean disconnectFolder() {
		if (MessageDialog.openQuestion(getShell(), Policy.bind("CVSFolderPropertiesPage.disconnectTitle"), Policy.bind("CVSFolderPropertiesPage.disconnectQuestion"))) { //$NON-NLS-1$ //$NON-NLS-2$
			final ICVSFolder cvsFolder = CVSWorkspaceRoot.getCVSFolderFor(folder);
			try {
				PlatformUI.getWorkbench().getProgressService().run(true, false, new IRunnableWithProgress() {
					public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
						try {
							cvsFolder.unmanage(null);
						} catch (CVSException e) {
							throw new InvocationTargetException(e);
						}
					}
				});
			} catch (InvocationTargetException e) {
				CVSUIPlugin.openError(getShell(), null, null, e);
				return false;
			} catch (InterruptedException e) {
				// Ignore
			}
			return true;
		} else {
			return false;
		}
	}			
}
