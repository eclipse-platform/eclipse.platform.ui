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
package org.eclipse.ui.console.actions;

import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsoleConstants;
import org.eclipse.ui.console.IConsoleFactory;
import org.eclipse.ui.internal.console.ConsoleMessages;

public class ConsoleViewConsoleFactory implements IConsoleFactory {

    int counter = 1;

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.console.IConsoleFactory#openConsole()
     */
    public void openConsole() {
        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        if (window != null) {
            IWorkbenchPage page = window.getActivePage();
            if (page != null) {
                try {
                    String secondaryId = ConsoleMessages.getString("ConsoleViewConsoleFactory.0") + counter; //$NON-NLS-1$
                    page.showView(IConsoleConstants.ID_CONSOLE_VIEW, secondaryId, 1);
                    counter++;
                } catch (PartInitException e) {
                    ConsolePlugin.log(e);
                }
            }
        }
    }

}
