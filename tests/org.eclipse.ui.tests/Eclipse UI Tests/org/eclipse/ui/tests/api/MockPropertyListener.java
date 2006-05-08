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

import org.eclipse.ui.IPropertyListener;
import org.eclipse.ui.tests.harness.util.CallHistory;

public class MockPropertyListener implements IPropertyListener {
    private CallHistory callTrace;

    private Object sourceMask;

    private int sourceId;

    /**
     * @param source the event source that fires the event to this listener
     * @param id the property id for the event
     */
    public MockPropertyListener(Object source, int id) {
        sourceMask = source;
        sourceId = id;
        callTrace = new CallHistory(this);
    }

    /**
     * @see IPropertyListener#propertyChanged(Object, int)
     */
    public void propertyChanged(Object source, int propId) {
        if (source == sourceMask && propId == sourceId)
            callTrace.add("propertyChanged");
    }

    public CallHistory getCallHistory() {
        return callTrace;
    }
}

