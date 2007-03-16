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

public class CreateModelAction extends TestBrowserAction {
    int fLevel;

    int fChildCount;

    public CreateModelAction(String label, TestBrowser browser, int level,
            int childCount) {
        super(label, browser);
        fLevel = level;
        fChildCount = childCount;
    }

    public void run() {
        // Clear input since TestElement.equals does only
        // check the id, not the size of the TestElement.
        getBrowser().setInput(null);
        getBrowser().setInput(TestElement.createModel(fLevel, fChildCount));
    }
}
