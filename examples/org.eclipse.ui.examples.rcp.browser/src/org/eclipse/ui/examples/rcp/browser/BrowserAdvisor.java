/*******************************************************************************
 * Copyright (c) 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.examples.rcp.browser;

import org.eclipse.jface.util.Assert;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.application.IActionBarConfigurer;
import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
import org.eclipse.ui.application.WorkbenchAdvisor;

/**
 * The workbench advisor for the browser example.
 * Configures the workbench as needed for the browser example, including
 * specifying the default perspective id.
 * Configures each new workbench window as it is being opened.
 * 
 * @since 3.0
 */
public class BrowserAdvisor extends WorkbenchAdvisor {

	/**
	 * Constructs a new <code>BrowserAdvisor</code>.
	 */
	public BrowserAdvisor() {
		// do nothing
	}

	public void createWindowContents(IWorkbenchWindowConfigurer configurer, Shell shell) {
	    Menu menuBar = configurer.createMenuBar();
	    shell.setMenuBar(menuBar);
	    
	    GridLayout shellLayout = new GridLayout();
	    shellLayout.marginWidth = 0;
	    shellLayout.marginHeight = 0;
	    shellLayout.verticalSpacing = 0;
	    shell.setLayout(shellLayout);

		if (!"carbon".equals(SWT.getPlatform())) { //$NON-NLS-1$
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
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.application.WorkbenchAdvisor
	 */
	public String getInitialWindowPerspectiveId() {
		return BrowserApp.PLUGIN_ID + ".browserPerspective"; //$NON-NLS-1$
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.application.WorkbenchAdvisor
	 */
	public void preWindowOpen(IWorkbenchWindowConfigurer configurer) {
		configurer.setShowFastViewBars(false);
		configurer.setShowPerspectiveBar(false);
		configurer.setTitle("Browser Example");
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.application.WorkbenchAdvisor
	 */
	public void fillActionBars(IWorkbenchWindow window, IActionBarConfigurer configurer, int flags) {
		// FILL_PROXY is not currently handled
		Assert.isTrue((flags & WorkbenchAdvisor.FILL_PROXY) == 0);
		BrowserActionBuilder builder = new BrowserActionBuilder(window);
		builder.fillActionBars(configurer, flags);
	}
}
