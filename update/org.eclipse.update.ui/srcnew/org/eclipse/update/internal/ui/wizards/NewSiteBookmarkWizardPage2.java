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
package org.eclipse.update.internal.ui.wizards;

import java.net.*;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.update.internal.ui.UpdateUI;
import org.eclipse.update.internal.ui.model.*;

/**
 * @author dejan
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 */
public class NewSiteBookmarkWizardPage2 extends BaseNewWizardPage2 {
	private static final String KEY_TITLE = "NewSiteBookmarkWizardPage.title";
	private static final String KEY_DESC = "NewSiteBookmarkWizardPage.desc";
	private static final String KEY_URL = "NewSiteBookmarkWizardPage.url";
	private static final String KEY_HTTP = "NewSiteBookmarkWizardPage.http";
	private static final String KEY_INVALID =
		"NewSiteBookmarkWizardPage.invalid";
	private static final String KEY_TYPE = "NewSiteBookmarkWizardPage.type";
	private static final String KEY_UPDATE_TYPE =
		"NewSiteBookmarkWizardPage.updateType";
	private static final String KEY_WEB_TYPE =
		"NewSiteBookmarkWizardPage.webType";
	private Text urlText;
	private URL url;
	private SiteBookmark localBookmark;
	private Button updateButton;
	private Button webButton;

	/**
	 * Constructor for NewFolderWizardPage.
	 * @param folder
	 */
	public NewSiteBookmarkWizardPage2(BookmarkFolder folder) {
		super(folder);
		setTitle(UpdateUI.getString(KEY_TITLE));
		setDescription(UpdateUI.getString(KEY_DESC));
	}

	public NewSiteBookmarkWizardPage2(
		BookmarkFolder folder,
		SiteBookmark localBookmark) {
		this(folder);
		this.localBookmark = localBookmark;
	}

	/**
	 * @see BaseNewWizardPage#createClientControl(Composite, int)
	 */
	protected void createClientControl(Composite parent, int span) {
		Label label = new Label(parent, SWT.NULL);
		label.setText(UpdateUI.getString(KEY_URL));
		urlText = new Text(parent, SWT.SINGLE | SWT.BORDER);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		urlText.setLayoutData(gd);
		if (localBookmark != null) {
			url = localBookmark.getURL();
			urlText.setText(url.toString());
			urlText.setEnabled(false);
		} else
			urlText.setText(UpdateUI.getString(KEY_HTTP));
		urlText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				validatePage();
			}
		});
		if (localBookmark == null) {
			label = new Label(parent, SWT.NULL);
			label.setText(UpdateUI.getString(KEY_TYPE));
			gd = new GridData();
			gd.horizontalSpan = span;
			label.setLayoutData(gd);
			updateButton = new Button(parent, SWT.RADIO);
			updateButton.setText(
				UpdateUI.getString(KEY_UPDATE_TYPE));
			gd = new GridData();
			gd.horizontalSpan = span;
			gd.horizontalIndent = 10;
			updateButton.setLayoutData(gd);
			updateButton.setSelection(true);

			webButton = new Button(parent, SWT.RADIO);
			webButton.setText(UpdateUI.getString(KEY_WEB_TYPE));
			gd = new GridData();
			gd.horizontalSpan = span;
			gd.horizontalIndent = 10;
			webButton.setLayoutData(gd);
		}

		WorkbenchHelp.setHelp(
			parent,
			"org.eclipse.update.ui.NewSiteBookmarkWizardPage");
	}

	protected void validatePage() {
		super.validatePage();
		if (isPageComplete() && localBookmark == null) {
			try {
				String decodedText = URLDecoder.decode(urlText.getText().trim());
				url = new URL(decodedText);
				super.validatePage();
			} catch (MalformedURLException e) {
				handleInvalidURL();
			} catch (ArrayIndexOutOfBoundsException e) {
				//33159
				handleInvalidURL();
			} catch (IllegalArgumentException e) {
				// 33159
				handleInvalidURL();
			}
		}
	}
	private void handleInvalidURL() {
		setDelayedErrorMessage(UpdateUI.getString(KEY_INVALID));
		setPageComplete(false);
	}

	public boolean finish() {
		//UpdateModel model = UpdateUI.getDefault().getUpdateModel();
		//BookmarkFolder parentFolder = getFolder();
		boolean webBookmark = false;
		if (localBookmark == null)
			webBookmark = webButton.getSelection();
		SiteBookmark newBookmark =
			new SiteBookmark(getName(), url, webBookmark);
		addToModel(newBookmark);
		return true;
	}
}