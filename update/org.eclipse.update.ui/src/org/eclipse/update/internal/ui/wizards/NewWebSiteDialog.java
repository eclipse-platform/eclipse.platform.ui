/*
 * Created on Jun 4, 2003
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code Template
 */
package org.eclipse.update.internal.ui.wizards;

import java.net.*;

import org.eclipse.swt.widgets.*;
import org.eclipse.update.internal.ui.*;
import org.eclipse.update.internal.ui.model.*;

/**
 * @author Wassim Melhem
 */
public class NewWebSiteDialog extends NewUpdateSiteDialog {

	public NewWebSiteDialog(Shell parentShell) {
		super(parentShell);
	}
	
	protected void update() {
		try {
			UpdateModel model = UpdateUI.getDefault().getUpdateModel();
			SiteBookmark bookmark = new SiteBookmark(name.getText(), new URL(url.getText()), true);
			model.addBookmark(bookmark);
		} catch (MalformedURLException e) {
		}
	}

}
