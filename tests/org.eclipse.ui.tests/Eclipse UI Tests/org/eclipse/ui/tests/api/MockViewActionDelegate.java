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
package org.eclipse.ui.tests.api;

import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;

/**
 * This mock is used to test IViewActionDelegate lifecycle.
 */
public class MockViewActionDelegate extends MockActionDelegate implements
        IViewActionDelegate {
    /**
     * Constructor for MockWorkbenchWindowActionDelegate
     */
    public MockViewActionDelegate() {
        super();
    }

    /**
     * @see IViewActionDelegate#init(IViewPart)
     */
    public void init(IViewPart view) {
        callHistory.add("init");
    }
}

