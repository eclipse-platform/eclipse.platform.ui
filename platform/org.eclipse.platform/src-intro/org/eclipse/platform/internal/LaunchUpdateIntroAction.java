/*******************************************************************************
 * Copyright (c) 200, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.platform.internal;

import java.util.Properties;

import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.intro.IIntroSite;
import org.eclipse.ui.intro.config.IIntroAction;
import org.eclipse.update.ui.UpdateManagerUI;

public class LaunchUpdateIntroAction implements IIntroAction {

    public LaunchUpdateIntroAction() {
    }

    public void run(IIntroSite site, Properties params) {

        Runnable r = new Runnable() {
            public void run() {
                Shell currentShell = PlatformUI.getWorkbench()
                    .getActiveWorkbenchWindow().getShell();
                UpdateManagerUI.openInstaller(currentShell);
            }
        };

        Shell currentShell = PlatformUI.getWorkbench()
            .getActiveWorkbenchWindow().getShell();
        currentShell.getDisplay().asyncExec(r);
    }
}
