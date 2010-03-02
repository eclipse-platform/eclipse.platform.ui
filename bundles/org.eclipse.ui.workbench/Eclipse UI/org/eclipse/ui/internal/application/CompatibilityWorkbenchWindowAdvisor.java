/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.application;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.application.ActionBarAdvisor;
import org.eclipse.ui.application.IActionBarConfigurer;
import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
import org.eclipse.ui.application.WorkbenchAdvisor;
import org.eclipse.ui.application.WorkbenchWindowAdvisor;

/**
 * An implementation of <code>WorkbenchWindowAdvisor</code> that
 * calls back to the 3.0 legacy methods on <code>WorkbenchAdvisor</code>
 * for backwards compatibility.
 * 
 * @since 3.1
 */
public class CompatibilityWorkbenchWindowAdvisor extends WorkbenchWindowAdvisor {

    private WorkbenchAdvisor wbAdvisor;

    /**
     * Creates a new compatibility workbench window advisor.
     * 
     * @param wbAdvisor the workbench advisor
     * @param windowConfigurer the window configurer
     */
    public CompatibilityWorkbenchWindowAdvisor(WorkbenchAdvisor wbAdvisor, IWorkbenchWindowConfigurer windowConfigurer) {
        super(windowConfigurer);
        this.wbAdvisor = wbAdvisor;
    }

    public void preWindowOpen() {
        wbAdvisor.preWindowOpen(getWindowConfigurer());
    }

    public ActionBarAdvisor createActionBarAdvisor(IActionBarConfigurer configurer) {
        return new CompatibilityActionBarAdvisor(wbAdvisor, configurer);
    }
    
    public void postWindowRestore() throws WorkbenchException {
        wbAdvisor.postWindowRestore(getWindowConfigurer());
    }

    public void openIntro() {
        wbAdvisor.openIntro(getWindowConfigurer());
    }

    public void postWindowCreate() {
        wbAdvisor.postWindowCreate(getWindowConfigurer());
    }

    public void postWindowOpen() {
        wbAdvisor.postWindowOpen(getWindowConfigurer());
    }

    public boolean preWindowShellClose() {
        return wbAdvisor.preWindowShellClose(getWindowConfigurer());
    }

    public void postWindowClose() {
        wbAdvisor.postWindowClose(getWindowConfigurer());
    }

    public boolean isApplicationMenu(String menuId) {
        return wbAdvisor.isApplicationMenu(getWindowConfigurer(), menuId);
    }

    public IAdaptable getDefaultPageInput() {
        return wbAdvisor.getDefaultPageInput();
    }

    public void createWindowContents(Shell shell) {
        wbAdvisor.createWindowContents(getWindowConfigurer(), shell);
    }

 
}
