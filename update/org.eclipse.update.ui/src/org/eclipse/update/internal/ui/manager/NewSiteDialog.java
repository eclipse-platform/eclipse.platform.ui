package org.eclipse.update.internal.ui.manager;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.swt.events.*;
import java.util.*;
import org.eclipse.ui.*;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.layout.*;
import org.eclipse.ui.dialogs.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.*;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.update.internal.ui.model.*;
import java.net.*;
import org.eclipse.update.internal.ui.UpdateUIPlugin;

public class NewSiteDialog extends Dialog {
	public static final String KEY_TITLE = "NewSiteDialog.title";
	private static final String KEY_URL = "NewSiteDialog.url";
	private static final String KEY_NAME = "NewSiteDialog.name";
	private Text urlText;
	private Text nameText;
	private String url;
	private String name;
	private Button okButton;
	private SiteBookmark localBookmark;

public NewSiteDialog(Shell parentShell) {
	this(parentShell, null);
}

public NewSiteDialog(Shell parentShell, SiteBookmark localBookmark) {
	super(parentShell);
	this.localBookmark = localBookmark;
}

public void okPressed() {
	url = urlText.getText();
	name = nameText.getText();
	if (name.length()==0) name = url;
	super.okPressed();
}

protected void createButtonsForButtonBar(Composite parent) {
	// create OK and Cancel buttons by default
	okButton = createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
	createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
	okButton.setEnabled(localBookmark!=null);
}

public Control createDialogArea(Composite parent) {
	Composite container = new Composite(parent, SWT.NULL);
	GridLayout layout = new GridLayout();
	layout.numColumns = 2;
	container.setLayout(layout);
	container.setLayoutData(new GridData(GridData.FILL_BOTH));
	
	Label label = new Label(container, SWT.NULL);
	label.setText(UpdateUIPlugin.getResourceString(KEY_NAME));
	nameText = new Text(container, SWT.SINGLE | SWT.BORDER);
	GridData gd = new GridData(GridData.FILL_HORIZONTAL);
	nameText.setLayoutData(gd);

	label = new Label(container, SWT.NULL);
	label.setText(UpdateUIPlugin.getResourceString(KEY_URL));
	urlText = new Text(container, SWT.SINGLE | SWT.BORDER);
	gd = new GridData(GridData.FILL_HORIZONTAL);
	urlText.setLayoutData(gd);
	if (localBookmark!=null) {
		nameText.setText(localBookmark.getName());
		nameText.selectAll();
		urlText.setText(localBookmark.getURL().toString());
		urlText.setEnabled(false);
	}
	urlText.addModifyListener(new ModifyListener() {
		public void modifyText(ModifyEvent e) {
			validateURL();
		}
	});
	return container;
}

private void validateURL() {
	boolean enable = true;
	String urlName = urlText.getText();
	if (urlName.length()==0) enable = false;
	try {
		new URL(urlName);
	}
	catch (MalformedURLException e) {
		enable = false;
	}
	okButton.setEnabled(enable);
}

public SiteBookmark getNewSite() {
	try {
		URL siteURL = new URL(url);
		return new SiteBookmark(name, siteURL);
	}
	catch (MalformedURLException e) {
		return null;
	}
}

}
