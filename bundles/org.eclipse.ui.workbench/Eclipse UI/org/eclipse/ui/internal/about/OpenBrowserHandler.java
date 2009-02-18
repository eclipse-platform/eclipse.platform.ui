/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal.about;

import java.net.URL;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.about.InstallationPage;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.internal.WorkbenchMessages;

public class OpenBrowserHandler extends ProductInfoPageHandler {

	protected Object execute(InstallationPage page, ExecutionEvent event) {
		Shell shell = HandlerUtil.getActiveShell(event);
		if (page instanceof TableListPage) {
			TableListPage p = (TableListPage) page;
			URL url = p.getURL();
			if (url != null && AboutUtils.openBrowser(shell, url))
				return null;
		}
		// nothing to show, but the user is expecting feedback.
		MessageDialog.openInformation(HandlerUtil.getActiveShell(event),
				WorkbenchMessages.OpenBrowserHandler_NoInfoDialogTitle,
				WorkbenchMessages.OpenBrowserHandler_NoInfoDialogMessage);
		return null;
	}
}