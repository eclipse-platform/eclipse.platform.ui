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

import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.Viewer;

public class ResetSorterAction extends TestBrowserAction {

    public ResetSorterAction(String label, TestBrowser browser) {
        super(label, browser);
    }

    public void run() {
        Viewer viewer = getBrowser().getViewer();
        if (viewer instanceof StructuredViewer) {
            StructuredViewer v = (StructuredViewer) viewer;
            v.setSorter(null);
        }
    }
}
