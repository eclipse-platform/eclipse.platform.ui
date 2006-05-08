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

import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.tests.harness.util.CallHistory;

public class MockPartListener implements IPartListener {
    private CallHistory callTrace;

    public MockPartListener() {
        callTrace = new CallHistory(this);
    }

    public CallHistory getCallHistory() {
        return callTrace;
    }

    /**
     * @see IPartListener#partActivated(IWorkbenchPart)
     */
    public void partActivated(IWorkbenchPart part) {
        callTrace.add("partActivated");
    }

    /**
     * @see IPartListener#partBroughtToTop(IWorkbenchPart)
     */
    public void partBroughtToTop(IWorkbenchPart part) {
        callTrace.add("partBroughtToTop");
    }

    /**
     * @see IPartListener#partClosed(IWorkbenchPart)
     */
    public void partClosed(IWorkbenchPart part) {
        callTrace.add("partClosed");
    }

    /**
     * @see IPartListener#partDeactivated(IWorkbenchPart)
     */
    public void partDeactivated(IWorkbenchPart part) {
        callTrace.add("partDeactivated");
    }

    /**
     * @see IPartListener#partOpened(IWorkbenchPart)
     */
    public void partOpened(IWorkbenchPart part) {
        callTrace.add("partOpened");
    }
}
