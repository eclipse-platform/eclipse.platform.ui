package org.eclipse.update.internal.ui.wizards;

import java.net.*;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.*;
import org.eclipse.update.internal.ui.UpdateUIPlugin;
import org.eclipse.update.internal.ui.model.*;

/**
 * @author dejan
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 */
public class NewSiteBookmarkWizardPage extends BaseNewWizardPage {
	private Text urlText;
	private URL url;
	private SiteBookmark localBookmark;
	/**
	 * Constructor for NewFolderWizardPage.
	 * @param folder
	 */
	public NewSiteBookmarkWizardPage(BookmarkFolder folder) {
		super(folder);
		setTitle("New Update Site Bookmark");
		setDescription("Bookmark an update site. Use folders to organize your bookmarks.");
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
		label.setText("&URL:");
		urlText = new Text(parent, SWT.SINGLE | SWT.BORDER);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		urlText.setLayoutData(gd);
		if (localBookmark != null) {
			url = localBookmark.getURL();
			urlText.setText(url.toString());
			urlText.setEnabled(false);
		} else
			urlText.setText("http://");
		urlText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				validatePage();
			}
		});
	}

	protected void validatePage() {
		super.validatePage();
		if (isPageComplete() && localBookmark == null) {
			try {
				url = new URL(urlText.getText());
				super.validatePage();
			} catch (MalformedURLException e) {
				setErrorMessage("Invalid URL format");
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