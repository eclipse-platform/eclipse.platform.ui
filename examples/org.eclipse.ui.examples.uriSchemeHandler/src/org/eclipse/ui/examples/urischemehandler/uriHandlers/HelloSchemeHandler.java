/*******************************************************************************
 * Copyright (c) 2018 SAP SE and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     SAP SE - initial version
 *******************************************************************************/
package org.eclipse.ui.examples.urischemehandler.uriHandlers;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.urischeme.IUriSchemeHandler;

/**
 * URI scheme handler implementation of the <code>hello</code> URI scheme
 *
 */
public class HelloSchemeHandler implements IUriSchemeHandler {

	@Override
	public void handle(String uri) {
		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		MessageDialog.openInformation(
				window.getShell(),
				"UriSchemeHandler", //$NON-NLS-1$
				"Hello, Eclipse world! Received URL: " + uri); //$NON-NLS-1$
	}
}