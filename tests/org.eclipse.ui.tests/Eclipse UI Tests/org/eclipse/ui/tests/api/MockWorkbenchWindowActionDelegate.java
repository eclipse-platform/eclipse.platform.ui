/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.api;

import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

public class MockWorkbenchWindowActionDelegate extends MockActionDelegate
        implements IWorkbenchWindowActionDelegate {
    public static MockWorkbenchWindowActionDelegate lastDelegate;

    public static String SET_ID = "org.eclipse.ui.tests.api.MockActionSet";

    public static String ID = "org.eclipse.ui.tests.api.MockWindowAction";

    /**
     * Constructor for MockWorkbenchWindowActionDelegate
     */
    public MockWorkbenchWindowActionDelegate() {
        super();
        lastDelegate = this;
    }

    /**
     * @see IWorkbenchWindowActionDelegate#init(IWorkbenchWindow)
     */
    public void init(IWorkbenchWindow window) {
        callHistory.add("init");
    }

    /**
     * @see IWorkbenchWindowActionDelegate#dispose()
     */
    public void dispose() {
        callHistory.add("dispose");
    }
}

