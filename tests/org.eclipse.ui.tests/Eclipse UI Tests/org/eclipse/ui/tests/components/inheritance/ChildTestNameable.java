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
public class ChildTestNameable extends TestComponent implements ITestNameable, ITestActivateListener {

    private ITestNameable parent;
    private String name = "";
    private boolean active = false;
    
    public ChildTestNameable(IServiceProvider parent) throws ComponentException {
        super(parent);
        this.parent = (ITestNameable)getDep(ITestNameable.class);
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.core.examples.component.samplecomponents.ITestNameable#setName(java.lang.String)
     */
    public void setName(String newName) {
        this.name = newName;
        if (active) {
            parent.setName(name);
        }
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.core.examples.component.samplecomponents.ITestActivateListener#activate()
     */
    public void activate() {
        active = true;
        parent.setName(name);
    }
    
    
    /* (non-Javadoc)
     * @see org.eclipse.core.examples.component.samplecomponents.ITestActivateListener#deactivate()
     */
    public void deactivate() {
        active = false;
    }
}
