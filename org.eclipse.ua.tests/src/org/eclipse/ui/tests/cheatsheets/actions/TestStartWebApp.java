/*******************************************************************************
 * Copyright (c) 2002, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.cheatsheets.actions;

import org.eclipse.core.runtime.*;
import org.eclipse.help.internal.appserver.WebappManager;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.*;

public class TestStartWebApp implements IWorkbenchWindowActionDelegate {
	private static String appServerHost = null;

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#dispose()
	 */
	public void dispose() {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#init(org.eclipse.ui.IWorkbenchWindow)
	 */
	public void init(IWorkbenchWindow window) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	public void run(IAction action) {
		int appServerPort = 0;
		try {
			Shell parent = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
			if(appServerHost == null) {
				if(MessageDialog.openQuestion(parent, "Web App", "Web application for cheat sheet testing is not started for this Run-time Workbench. Would you like to start it now?")) {
					WebappManager.start("cheatsheets.test", "org.eclipse.ui.tests.cheatsheets", new Path("cheatsheets/tests/webapp"));
				} else {
					return;
				}
			} else {
				MessageDialog.openInformation(parent, "Web App", "Web application for cheat sheet testing is already started for this Run-time Workbench.");
			}
			
			appServerHost = WebappManager.getHost();
			appServerPort = WebappManager.getPort();
			System.out.println("host: "+appServerHost);	
			System.out.println("port: "+appServerPort);
			System.out.println("http://"+appServerHost+":"+appServerPort+"/cheatsheets.test/TestOpeningURL.xml");
		} catch(CoreException e) {
			e.printStackTrace();
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {
	}
}
