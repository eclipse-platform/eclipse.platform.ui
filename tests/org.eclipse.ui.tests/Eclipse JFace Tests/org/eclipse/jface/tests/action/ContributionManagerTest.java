/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.tests.action;

import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import junit.framework.TestCase;


/**
 * Abstract superclass of all contribution manager tests.
 * 
 * @since 3.1
 */
public abstract class ContributionManagerTest extends TestCase {

    private Display display;
    private Shell shell;

    /**
     * Constructs a new contribution manager test with the given name
     * 
     * @param name the name of the test
     */
    protected ContributionManagerTest(String name) {
        super(name);
    }

    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception {
	    display = Display.getCurrent();
	    if (display == null) {
	        display = new Display();
	    }
	    shell = new Shell(display);
	    shell.setSize(500, 500);
	    shell.setLayout(new FillLayout());
	    shell.open();
    }

    protected Display getDisplay() {
        return display;
    }
    
    protected Shell getShell() {
        return shell;
    }
}
