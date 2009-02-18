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

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.about.InstallationPage;
import org.eclipse.ui.handlers.HandlerUtil;

public class ViewErrorLogHandler extends ProductInfoPageHandler {

	protected Object execute(InstallationPage page, ExecutionEvent event) {
		Shell shell = HandlerUtil.getActiveShell(event);
		if (shell == null)
			shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
		AboutUtils.openErrorLogBrowser(shell);
		return null;
	}
}