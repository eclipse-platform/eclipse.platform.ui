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

import org.eclipse.jface.viewers.StructuredSelection;

public class ClearSelectionAction extends TestBrowserAction {

    public ClearSelectionAction(String label, TestBrowser browser) {
        super(label, browser);
    }

    public void run() {
        getBrowser().getViewer().setSelection(new StructuredSelection());
    }
}
