/*
 * Created on Jun 4, 2003
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code Template
 */
package org.eclipse.update.internal.ui.wizards;

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.swt.widgets.Shell;
import org.eclipse.update.internal.ui.UpdateUI;
import org.eclipse.update.internal.ui.model.SiteBookmark;
import org.eclipse.update.internal.ui.model.UpdateModel;

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
