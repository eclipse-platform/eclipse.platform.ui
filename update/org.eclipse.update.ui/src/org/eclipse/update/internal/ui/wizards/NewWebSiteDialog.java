/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.update.internal.ui.wizards;

import java.net.*;

import org.eclipse.swt.widgets.*;
import org.eclipse.update.internal.ui.*;
import org.eclipse.update.internal.ui.model.*;


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
