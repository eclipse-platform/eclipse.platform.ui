/*
 * Created on May 13, 2003
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code Template
 */
package org.eclipse.update.internal.ui.wizards;

import java.net.*;

import org.eclipse.jface.dialogs.*;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.update.internal.ui.*;
import org.eclipse.update.internal.ui.model.*;
import org.eclipse.update.internal.ui.UpdateUI;

/**
 * @author wassimm
 */
public class NewUpdateSiteDialog extends Dialog {
	
	protected Text name;
	protected Text url;
	private Button okButton;
	/**
	 * @param parentShell
	 */
	public NewUpdateSiteDialog(Shell parentShell) {
		super(parentShell);
	}
	
	protected void createButtonsForButtonBar(Composite parent) {
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
		
		okButton.setEnabled(false);
		
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
		label.setText(UpdateUI.getString("NewUpdateSiteDialog.name")); //$NON-NLS-1$
		
		name = new Text(composite, SWT.BORDER);
		name.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		name.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				verifyComplete();
			}
		});
		
		label = new Label(composite, SWT.NONE);
		label.setText(UpdateUI.getString("NewUpdateSiteDialog.url")); //$NON-NLS-1$
		
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
		if (okButton == null)
			return;
			
		if (name.getText().trim().length() == 0 || url.getText().trim().length() == 0) {
			okButton.setEnabled(false);
			return;
		}
	
		try {
			URL newURL = new URL(URLDecoder.decode(url.getText().trim()));
			okButton.setEnabled(!newURL.getProtocol().equals("file")); //$NON-NLS-1$
		} catch (Exception e) {
			okButton.setEnabled(false);
		}
	}
	


}
