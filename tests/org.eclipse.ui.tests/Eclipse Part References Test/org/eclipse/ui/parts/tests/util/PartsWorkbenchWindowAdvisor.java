/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.parts.tests.util;

import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.application.ActionBarAdvisor;
import org.eclipse.ui.application.IActionBarConfigurer;
import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
import org.eclipse.ui.application.WorkbenchWindowAdvisor;
import org.eclipse.ui.internal.ide.WorkbenchActionBuilder;

/**
 * Workbench window advisor for the parts test suite.
 * 
 * @since 3.1
 */
public class PartsWorkbenchWindowAdvisor extends WorkbenchWindowAdvisor {

    private PartsWorkbenchAdvisor wbAdvisor;
    
    public PartsWorkbenchWindowAdvisor(PartsWorkbenchAdvisor advisor, IWorkbenchWindowConfigurer configurer) {
        super(configurer);
        this.wbAdvisor = advisor;
    }

    public ActionBarAdvisor createActionBarAdvisor(IActionBarConfigurer configurer) {
        return new WorkbenchActionBuilder(configurer);
    }
    
    public void openIntro() {
        // Do not open any intro part
    }
    
    /**
     * When the window has opened, validate the layout.  Tests override the validate method in the advisor.
     */
    public void postWindowOpen() {
        IWorkbenchPage page = getWindowConfigurer().getWindow().getActivePage();
        wbAdvisor.validate(page);
    }
}
