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

import org.eclipse.ui.components.IServiceProvider;

/**
 * @since 3.1
 */
public class DefaultTestDirtyHandler extends TestComponent implements ITestDirtyHandler {
    
    /**
     * @param container
     */
    public DefaultTestDirtyHandler(IServiceProvider container) {
        super(container);
    }

    /* (non-Javadoc)
     * @see org.eclipse.core.examples.component.samplecomponents.ITestDirtyHandler#setDirty(boolean)
     */
    public void setDirty(boolean dirty) {
    
    }
}
