/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.components.inheritance;

import org.eclipse.ui.internal.components.framework.ComponentException;
import org.eclipse.ui.internal.components.framework.IServiceProvider;

/**
 * @since 3.1
 */
public class ChildDirtyHandler extends TestComponent implements ITestDirtyHandler, ITestActivateListener {

    ITestDirtyHandler parent;
    boolean dirty;
    boolean active = false;
    
    public ChildDirtyHandler(IServiceProvider serviceProvider) throws ComponentException {
        super(serviceProvider);
        this.parent = (ITestDirtyHandler)getDep(ITestDirtyHandler.class);
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.core.examples.component.samplecomponents.ITestDirtyHandler#setDirty(boolean)
     */
    public void setDirty(boolean dirty) {
        this.dirty = dirty;
        if (active) {
            parent.setDirty(dirty);
        }
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.core.examples.component.samplecomponents.ITestActivateListener#activate()
     */
    public void activate() {
        active = true;
        parent.setDirty(dirty);
    }
    
    public void deactivate() {
        active = false;
    }
    
}
