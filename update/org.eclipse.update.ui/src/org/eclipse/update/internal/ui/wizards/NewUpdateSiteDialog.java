/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.update.internal.ui.wizards;

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.StatusDialog;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.update.internal.ui.URLCoder;
import org.eclipse.update.internal.ui.UpdateUI;
import org.eclipse.update.internal.ui.UpdateUIMessages;
import org.eclipse.update.internal.ui.model.SiteBookmark;
import org.eclipse.update.internal.ui.model.UpdateModel;


public class NewUpdateSiteDialog extends StatusDialog {
	
	protected Text name;
	protected Text url;
	private Button okButton;
	private boolean enableOK = false;
	private SiteBookmark[] siteBookmarks;
	/**
	 * @param parentShell
	 */
	public NewUpdateSiteDialog(Shell parentShell) {
		super(parentShell);
		enableOK = false;
	}
	
	public NewUpdateSiteDialog(Shell parentShell, SiteBookmark[] siteBookmarks) {
		
		this(parentShell);		
		this.siteBookmarks = siteBookmarks;
	}
	
	public NewUpdateSiteDialog(Shell parentShell, boolean enableOkButtons) {
		super(parentShell);
		enableOK = enableOkButtons;
	}
	
	public NewUpdateSiteDialog(Shell parentShell, boolean enableOkButtons, SiteBookmark[] siteBookmarks) {
		this(parentShell, enableOkButtons);
		this.siteBookmarks = siteBookmarks;
	}
	
	protected void createButtonsForButtonBar(Composite parent) {
		
		//super.createButtonBar(parent);
		okButton = createButton(
				parent,
				IDialogConstants.OK_ID,
				IDialogConstants.OK_LABEL,
				true);
		createButton(
			parent,
			IDialogConstants.CANCEL_ID,
			IDialogConstants.CANCEL_LABEL,
			false);
		
		okButton.setEnabled(enableOK);
		
	}
	
	protected Control createDialogArea(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		composite.setLayout(layout);
		GridData data = new GridData();
		data.widthHint = 350;
		composite.setLayoutData(data);
		
		Label label = new Label(composite, SWT.NONE);
		label.setText(UpdateUIMessages.NewUpdateSiteDialog_name); 
		
		name = new Text(composite, SWT.BORDER);
		name.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		name.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				verifyComplete();
			}
		});
		
		label = new Label(composite, SWT.NONE);
		label.setText(UpdateUIMessages.NewUpdateSiteDialog_url); 
		
		url = new Text(composite, SWT.BORDER);
		url.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		url.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				verifyComplete();
			}
		});
		
		initializeFields();
		Dialog.applyDialogFont(composite);
		return composite;
	}
	
	protected void initializeFields() {
		url.setText("http://"); //$NON-NLS-1$
	}
	
	protected void okPressed() {
		update();
		super.okPressed();
	}

	protected void update() {
		try {
			UpdateModel model = UpdateUI.getDefault().getUpdateModel();
			SiteBookmark bookmark = new SiteBookmark(name.getText(), new URL(url.getText()), false);
            bookmark.setSelected(true);
			model.addBookmark(bookmark);
			model.saveBookmarks();
		} catch (MalformedURLException e) {
		}
	}
	
	private void verifyComplete() {
			
		if (okButton == null) {
			return;		
		}
		
		if (name.getText().trim().length() == 0 || url.getText().trim().length() == 0) {
			okButton.setEnabled(false);
			this.updateStatus( new Status(IStatus.ERROR, UpdateUI.getPluginId(), IStatus.OK, UpdateUIMessages.NewUpdateSiteDialog_error_nameOrUrlNotSpecified, null)); 
			return;
		}
	
		try {
			URL newURL = new URL(URLCoder.decode(url.getText().trim()));
			if (url.getEditable()) {
				okButton.setEnabled(!newURL.getProtocol().equals("file")); //$NON-NLS-1$
				if (newURL.getProtocol().equals("file")) { //$NON-NLS-1$
					okButton.setEnabled(false);
					this.updateStatus( new Status(IStatus.ERROR, UpdateUI.getPluginId(), IStatus.OK, UpdateUIMessages.NewUpdateSiteDialog_error_incorrectUrl, null)); 
					return;
				}
			}
		} catch (Exception e) {
			okButton.setEnabled(false);
			this.updateStatus( new Status(IStatus.ERROR, UpdateUI.getPluginId(), IStatus.OK, UpdateUIMessages.NewUpdateSiteDialog_error_incorrectUrl, null)); 
			return;
		}
		
		if (isDuplicate()) {
			return;
		} else {
			okButton.setEnabled(true);
			this.updateStatus( new Status(IStatus.OK, UpdateUI.getPluginId(), IStatus.OK, "", null));  //$NON-NLS-1$
		}
		
		
	}
	
	private boolean isDuplicate() {
		
		if ( siteBookmarks == null)
			return false;
		
		for( int i = 0; i < this.siteBookmarks.length; i++) {
			if ( !isCurrentlyEditedSiteBookmark(i)) {
				if (siteBookmarks[i].getLabel().equals(name.getText().trim())) {
					okButton.setEnabled(false);
					this.updateStatus( new Status(IStatus.ERROR, UpdateUI.getPluginId(), IStatus.OK, UpdateUIMessages.NewUpdateSiteDialog_error_duplicateName, null)); 
					return true;
				} else if (siteBookmarks[i].getURL().toString().trim().equals(url.getText().trim())) {
					okButton.setEnabled(false);
					this.updateStatus( new Status(IStatus.ERROR, UpdateUI.getPluginId(), IStatus.OK, NLS.bind(UpdateUIMessages.NewUpdateSiteDialog_error_duplicateUrl, siteBookmarks[i].getLabel()), null)); 
					return true;
				}
			}
		}
		return false;
	}
	
	protected boolean isCurrentlyEditedSiteBookmark( int index) {
		return false;
	}
	
	
	protected void updateButtonsEnableState(IStatus status) {
		if (okButton != null && !okButton.isDisposed())
			okButton.setEnabled(!status.matches(IStatus.ERROR));
	}
	


}
