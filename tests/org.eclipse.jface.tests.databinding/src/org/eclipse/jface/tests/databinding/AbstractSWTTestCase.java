/*******************************************************************************
 * Copyright (c) 2007, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Ashley Cambrell - bug 198904
 ******************************************************************************/

package org.eclipse.jface.tests.databinding;

import org.eclipse.swt.widgets.Shell;

/**
 * Abstract test case that handles disposing of the Shell after each test.
 * 
 * @since 1.1
 */
public abstract class AbstractSWTTestCase extends AbstractDefaultRealmTestCase {
	private Shell shell;
	
	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.jface.tests.databinding.AbstractDefaultRealmTestCase#setUp()
	 */
	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}
	
	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.jface.tests.databinding.AbstractDefaultRealmTestCase#tearDown()
	 */
	@Override
	protected void tearDown() throws Exception {
		if (shell != null && !shell.isDisposed()) {
			shell.dispose();
		}
		super.tearDown();
	}
	
	/**
	 * Returns a Shell to be used in a test.
	 * 
	 * @return shell
	 */
	protected Shell getShell() {
		if (shell == null || shell.isDisposed()) {
			shell = new Shell();
		}
		
		return shell;
	}
}
