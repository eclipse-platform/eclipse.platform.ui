package org.eclipse.update.internal.ui.wizards;

import java.net.*;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.update.internal.ui.UpdateUIPlugin;
import org.eclipse.update.internal.ui.model.*;

/**
 * @author dejan
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 */
public class NewSiteBookmarkWizardPage extends BaseNewWizardPage {
	private static final String KEY_TITLE = "NewSiteBookmarkWizardPage.title";
	private static final String KEY_DESC = "NewSiteBookmarkWizardPage.desc";
	private static final String KEY_URL = "NewSiteBookmarkWizardPage.url";
	private static final String KEY_HTTP = "NewSiteBookmarkWizardPage.http";
	private static final String KEY_INVALID = "NewSiteBookmarkWizardPage.invalid";
	private Text urlText;
	private URL url;
	private SiteBookmark localBookmark;
	/**
	 * Constructor for NewFolderWizardPage.
	 * @param folder
	 */
	public NewSiteBookmarkWizardPage(BookmarkFolder folder) {
		super(folder);
		setTitle(UpdateUIPlugin.getResourceString(KEY_TITLE));
		setDescription(UpdateUIPlugin.getResourceString(KEY_DESC));
	}

	public NewSiteBookmarkWizardPage(
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
		label.setText(UpdateUIPlugin.getResourceString(KEY_URL));
		urlText = new Text(parent, SWT.SINGLE | SWT.BORDER);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		urlText.setLayoutData(gd);
		if (localBookmark != null) {
			url = localBookmark.getURL();
			urlText.setText(url.toString());
			urlText.setEnabled(false);
		} else
			urlText.setText(UpdateUIPlugin.getResourceString(KEY_HTTP));
		urlText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				validatePage();
			}
		});
		WorkbenchHelp.setHelp(parent, "org.eclipse.update.ui.NewSiteBookmarkWizardPage");
	}

	protected void validatePage() {
		super.validatePage();
		if (isPageComplete() && localBookmark == null) {
			try {
				url = new URL(urlText.getText());
				super.validatePage();
			} catch (MalformedURLException e) {
				setErrorMessage(UpdateUIPlugin.getResourceString(KEY_INVALID));
				setPageComplete(false);
			}
		}
	}

	public boolean finish() {
		UpdateModel model = UpdateUIPlugin.getDefault().getUpdateModel();
		BookmarkFolder parentFolder = getFolder();
		SiteBookmark newBookmark = new SiteBookmark(getName(), url);
		addToModel(newBookmark);
		return true;
	}
}