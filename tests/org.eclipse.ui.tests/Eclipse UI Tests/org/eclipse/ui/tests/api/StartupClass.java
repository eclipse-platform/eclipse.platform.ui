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
package org.eclipse.ui.tests.api;

import org.eclipse.ui.IStartup;

public class StartupClass implements IStartup {

    // This boolean should only be true if the earlyStartup() method
    // has been called.
    private static boolean earlyStartupCalled = false;

    /* (non-Javadoc)
     * @see org.eclipse.ui.IStartup#earlyStartup()
     */
    public void earlyStartup() {
        earlyStartupCalled = true;
    }

    public static boolean wasEarlyStartupCalled() {
        return earlyStartupCalled;
    }

    public static void clearEarlyStartup() {
        earlyStartupCalled = false;
    }
}