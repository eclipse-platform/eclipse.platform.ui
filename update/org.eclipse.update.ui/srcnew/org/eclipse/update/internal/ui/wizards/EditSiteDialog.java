/*
 * Created on May 16, 2003
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code Template
 */
package org.eclipse.update.internal.ui.wizards;

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.swt.widgets.Shell;
import org.eclipse.update.internal.operations.UpdateManager;
import org.eclipse.update.internal.ui.model.SiteBookmark;

/**
 * @author Wassim Melhem
 */
public class EditSiteDialog extends NewSiteDialog {
	SiteBookmark bookmark;
	
	public EditSiteDialog(Shell parentShell, SiteBookmark bookmark) {
		super(parentShell);
		this.bookmark = bookmark;
	}
	
	protected void initializeFields() {
		name.setText(bookmark.getName());
		url.setText(bookmark.getURL().toString());
	}

	protected void update() {
		try {
			bookmark.setName(name.getText());
			bookmark.setURL(new URL(url.getText()));
			UpdateManager.getOperationsManager().fireObjectChanged(
				bookmark,
				null);
		} catch (MalformedURLException e) {
		}
	}
}
