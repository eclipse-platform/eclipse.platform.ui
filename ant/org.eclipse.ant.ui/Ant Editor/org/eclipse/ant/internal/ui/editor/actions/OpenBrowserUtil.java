/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ant.internal.ui.editor.actions;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Shell;

public class OpenBrowserUtil {
	
	public static void open(final String  urlString, final Shell shell, final String dialogTitle) {
		shell.getDisplay().syncExec(new Runnable() {
			public void run() {
				internalOpen(shell, urlString, dialogTitle);
			}
		});
	}
	
	private static void internalOpen(Shell shell, String urlString, String title) {
	    String platform= SWT.getPlatform();
	    boolean succeeded= true;
		if ("motif".equals(platform) || "gtk".equals(platform)) { //$NON-NLS-1$ //$NON-NLS-2$
			Program program= Program.findProgram("html"); //$NON-NLS-1$
			if (program == null) {
				program= Program.findProgram("htm"); //$NON-NLS-1$
			}
			if (program != null) {
			    succeeded= program.execute(urlString.toString());
			}
		} else {
		    succeeded= Program.launch(urlString.toString());
		}
		if (!succeeded) {
			MessageDialog.openInformation(shell, title, "Browser could not be opened"); //$NON-NLS-1$
		}
	}
}
