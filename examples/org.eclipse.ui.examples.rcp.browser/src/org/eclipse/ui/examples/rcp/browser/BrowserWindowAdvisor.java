/*******************************************************************************
 * Copyright (c) 2004, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.examples.rcp.browser;

import org.eclipse.swt.graphics.Point;
import org.eclipse.ui.application.ActionBarAdvisor;
import org.eclipse.ui.application.IActionBarConfigurer;
import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
import org.eclipse.ui.application.WorkbenchWindowAdvisor;

/**
 * Configures the browser window using the given window configurer.
 * 
 * @since 3.1
 */
public class BrowserWindowAdvisor extends WorkbenchWindowAdvisor {

    /**
     * Creates a new browser window advisor.
     * 
     * @param configurer the window configurer
     */
    public BrowserWindowAdvisor(IWorkbenchWindowConfigurer configurer) {
        super(configurer);
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.application.WorkbenchAdvisor
     */
    public void preWindowOpen() {
        IWorkbenchWindowConfigurer configurer = getWindowConfigurer();
        configurer.setInitialSize(new Point(800, 600));
        
        // Default window title is the product name, so don't need to set it
        // explicitly anymore.
//      configurer.setTitle("Browser Example");
        
//      configurer.setShowFastViewBars(true);
    }

    
    /* (non-Javadoc)
     * @see org.eclipse.ui.application.WorkbenchWindowAdvisor#createActionBarAdvisor(org.eclipse.ui.application.IActionBarConfigurer)
     */
    public ActionBarAdvisor createActionBarAdvisor(
            IActionBarConfigurer actionBarConfigurer) {
        return new BrowserActionBarAdvisor(actionBarConfigurer);
    }
    
//  Uncomment the code below for a custom window layout (add back the missing imports using Ctrl+Shift+O) 
/*    
      public void createWindowContents(Shell shell) {
          IWorkbenchWindowConfigurer configurer = getWindowConfigurer();
          Menu menuBar = configurer.createMenuBar();
          shell.setMenuBar(menuBar);
          
          GridLayout shellLayout = new GridLayout();
          shellLayout.marginWidth = 0;
          shellLayout.marginHeight = 0;
          shellLayout.verticalSpacing = 0;
          shell.setLayout(shellLayout);
    
          if (!Util.isMac()) {
              Label sep1 = new Label(shell, SWT.SEPARATOR | SWT.HORIZONTAL);
              sep1.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false));
          }
          Control coolBar = configurer.createCoolBarControl(shell);
          coolBar.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false));
    
          Label sep2 = new Label(shell, SWT.SEPARATOR | SWT.HORIZONTAL);
          sep2.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false));
          
          Control pageComposite = configurer.createPageComposite(shell);
          pageComposite.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, true));
    
          Label sep3 = new Label(shell, SWT.SEPARATOR | SWT.HORIZONTAL);
          sep3.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false));
          Control statusLine = configurer.createStatusLineControl(shell);
          statusLine.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false));
          shell.layout(true);
      }
*/
}
