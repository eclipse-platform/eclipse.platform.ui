/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.tests.viewers.interactive;

import org.eclipse.jface.action.Action;

public abstract class TestBrowserAction extends Action {
    private TestBrowser browser;

    public TestBrowserAction(String label, TestBrowser browser) {
        super(label);
        this.browser = browser;
    }

    /**
     * Returns the test browser.
     */
    public TestBrowser getBrowser() {
        return browser;
    }
}
