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

import org.eclipse.jface.tests.viewers.TestElement;
import org.eclipse.jface.tests.viewers.TestModelChange;

public class AddElementAction extends TestBrowserAction {

    public AddElementAction(String label, TestBrowser browser) {
        super(label, browser);
        //		window.addFocusChangedListener(this);
    }

    public void run() {
        TestElement element = (TestElement) getBrowser().getViewer().getInput();
        element.addChild(TestModelChange.INSERT);
    }
}
