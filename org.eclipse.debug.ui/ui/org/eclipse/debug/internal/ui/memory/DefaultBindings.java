/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.memory;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.ui.IDebugUIConstants;

/**
 * Represents a defaultRenderings contribution.
 * 
 * @since 3.1
 */
class DefaultBindings extends RenderingBindings {

    public static final String ATTR_PRIMARY = "primaryId"; //$NON-NLS-1$
    
    /**
     * Constructs a defult bindings contribution.
     * 
     * @param element contribution
     */
    DefaultBindings(IConfigurationElement element) {
        super(element);
    }

    /**
     * Validates this contribution.
     * 
     * @exception CoreException if invalid
     */
    void validate() throws CoreException {
        verifyPresent(ATTR_RENDERING_IDS);
    }
    
    boolean hasPrimary() {
        return getPrimaryId() != null;
    }
    
    /**
     * Returns the primary rendering type id, or <code>null</code> if none.
     * 
     * @return the primary rendering type id, or <code>null</code> if none
     */
    String getPrimaryId() {
        return fConfigurationElement.getAttribute(ATTR_PRIMARY);
    }
    
    private void verifyPresent(String attrName) throws CoreException {
        if (fConfigurationElement.getAttribute(attrName) == null) {
            Status status = new Status(IStatus.ERROR, DebugUIPlugin.getUniqueIdentifier(), IDebugUIConstants.INTERNAL_ERROR,
                    "<defatutRenderings> element missing required attribute: " + attrName, null); //$NON-NLS-1$
            throw new CoreException(status);
        }
    }    
}
