/*******************************************************************************
 * Copyright (c) 2018 SAP SE and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
public class Hello2SchemeHandler implements IUriSchemeHandler {

	@Override
	public void handle(String uri) {
		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		MessageDialog.openInformation(
				window.getShell(),
				"Handler for 'hello2' URI scheme", //$NON-NLS-1$
				"Hello, Eclipse world!\nReceived URL: " + uri); //$NON-NLS-1$
	}
}