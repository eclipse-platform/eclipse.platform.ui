/*
 * Created on May 13, 2003
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code Template
 */
package org.eclipse.update.internal.ui.wizards;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
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
import org.eclipse.update.internal.operations.UpdateManager;
import org.eclipse.update.internal.ui.UpdateUI;
import org.eclipse.update.internal.ui.model.SiteBookmark;
import org.eclipse.update.internal.ui.model.UpdateModel;

/**
 * @author wassimm
 */
public class NewSiteDialog extends Dialog {
	
	private Text name;
	private Text url;
	private Button okButton;
	/**
	 * @param parentShell
	 */
	public NewSiteDialog(Shell parentShell) {
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
		label.setText("Name: ");
		
		name = new Text(composite, SWT.BORDER);
		name.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		name.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				verifyComplete();
			}
		});
		
		label = new Label(composite, SWT.NONE);
		label.setText("URL: ");
		
		url = new Text(composite, SWT.BORDER);
		url.setText("http://");
		url.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		url.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				verifyComplete();
			}
		});
		
		Dialog.applyDialogFont(composite);
		return composite;
	}
	
	protected void okPressed() {
		try {
			UpdateModel model = UpdateUI.getDefault().getUpdateModel();
			SiteBookmark bookmark = new SiteBookmark(name.getText(), new URL(url.getText()), false);
			model.addBookmark(bookmark);
			UpdateManager.getOperationsManager().fireObjectsAdded(null, new Object[]{bookmark});
			model.saveBookmarks();
		} catch (MalformedURLException e) {
		}
		super.okPressed();
	}

	
	private void verifyComplete() {
		if (name.getText().trim().length() == 0 || url.getText().trim().length() == 0) {
			okButton.setEnabled(false);
			return;
		}
	
		try {
			new URL(URLDecoder.decode(url.getText().trim()));
			okButton.setEnabled(true);
		} catch (Exception e) {
			okButton.setEnabled(false);
		}
	}
	


}
