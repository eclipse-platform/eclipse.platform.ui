/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.components.inheritance;

import org.eclipse.core.components.ComponentException;
import org.eclipse.core.components.IServiceProvider;

/**
 * @since 3.1
 */
public class TestPartPropertyProvider extends TestComponent implements ITestNameable,
        ITestPartPropertyProvider, ITestDirtyHandler {

    private String name;
    private ITestDirtyHandler dirtyHandler;
    private ITestNameable nameable;
    private boolean dirty;
    
    public TestPartPropertyProvider(IServiceProvider container) throws ComponentException {
        super(container);
        this.dirtyHandler = (ITestDirtyHandler)getDep(ITestDirtyHandler.class);
        this.nameable = (ITestNameable)getDep(ITestNameable.class);
        this.name = "";
        dirty = false;
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.core.examples.component.samplecomponents.ITestNameable#setName(java.lang.String)
     */
    public void setName(String newName) {
        this.name = newName;
        nameable.setName(newName);
    }

    /* (non-Javadoc)
     * @see org.eclipse.core.examples.component.samplecomponents.ITestPartPropertyProvider#getName()
     */
    public String getName() {
        return name;
    }

    /* (non-Javadoc)
     * @see org.eclipse.core.examples.component.samplecomponents.ITestPartPropertyProvider#getDirty()
     */
    public boolean getDirty() {
        return dirty;
    }

    /* (non-Javadoc)
     * @see org.eclipse.core.examples.component.samplecomponents.ITestDirtyHandler#setDirty(boolean)
     */
    public void setDirty(boolean dirty) {
        this.dirty = dirty;
        dirtyHandler.setDirty(dirty);
    }

}
