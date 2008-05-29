/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.console.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.internal.console.ConsoleMessages;
import org.eclipse.ui.internal.console.ConsolePluginImages;
import org.eclipse.ui.internal.console.IInternalConsoleConstants;

/**
 * Removes a console from the console manager.
 * <p>
 * Clients may instantiate this class.
 * </p>
 * @since 3.1
 * @noextend This class is not intended to be subclassed by clients.
 */
public class CloseConsoleAction extends Action {
    
    private IConsole fConsole;
    
    public CloseConsoleAction(IConsole console) {
        super(ConsoleMessages.CloseConsoleAction_0, ConsolePluginImages.getImageDescriptor(IInternalConsoleConstants.IMG_ELCL_CLOSE)); 
        setToolTipText(ConsoleMessages.CloseConsoleAction_1); 
        fConsole = console;
    }

    public void run() {
        ConsolePlugin.getDefault().getConsoleManager().removeConsoles(new IConsole[]{fConsole});
    }
}
