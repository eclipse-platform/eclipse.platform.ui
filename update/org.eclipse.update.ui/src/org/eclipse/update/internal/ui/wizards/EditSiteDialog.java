/*
 * Created on May 16, 2003
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code Template
 */
package org.eclipse.update.internal.ui.wizards;

import java.net.*;

import org.eclipse.swt.widgets.*;
import org.eclipse.update.internal.ui.model.*;
import org.eclipse.update.operations.*;

/**
 * @author Wassim Melhem
 */
public class EditSiteDialog extends NewUpdateSiteDialog {
	SiteBookmark bookmark;

	public EditSiteDialog(Shell parentShell, SiteBookmark bookmark) {
		super(parentShell);
		this.bookmark = bookmark;
	}

	protected void initializeFields() {
		name.setText(bookmark.getName());
		url.setText(bookmark.getURL().toString());
		url.setEditable(!bookmark.isLocal());
	}

	protected void update() {
		try {
			bookmark.setName(name.getText());
			bookmark.setURL(new URL(url.getText()));
			OperationsManager.fireObjectChanged(bookmark, null);
		} catch (MalformedURLException e) {
		}
	}
}
