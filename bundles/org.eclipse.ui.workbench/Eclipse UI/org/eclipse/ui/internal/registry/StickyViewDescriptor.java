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
package org.eclipse.ui.internal.registry;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;


/**
 * @since 3.0
 */
public class StickyViewDescriptor implements IStickyViewDescriptor {
    private static final String ATT_ID = "id"; //$NON-NLS-1$
    
    private String id;
    private String namespace;
        
    public StickyViewDescriptor(IConfigurationElement singleton) throws CoreException {
        id = singleton.getAttribute(ATT_ID);
        if (id == null)
        	throw new CoreException(new Status(IStatus.ERROR, singleton.getDeclaringExtension().getNamespace(), 0, "Invalid extension (missing id) ", null));//$NON-NLS-1$
        namespace = singleton.getDeclaringExtension().getNamespace();
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.registry.IStickyViewDescriptor#getId()
     */
    public String getId() {
        return id;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.registry.IStickyViewDescriptor#getNamespace()
     */
    public String getNamespace() {
        return namespace;
    }
}
