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
import org.eclipse.ui.IPageLayout;



/**
 * @since 3.0
 */
public class StickyViewDescriptor implements IStickyViewDescriptor {
    private static final String ATT_ID = "id"; //$NON-NLS-1$
    private static final String ATT_LOCATION = "location"; //$NON-NLS-1$
    
    private String id;
    private String namespace;
    private int direction = IPageLayout.RIGHT;
        
    public StickyViewDescriptor(IConfigurationElement singleton) throws CoreException {
        id = singleton.getAttribute(ATT_ID);
        if (id == null)
        	throw new CoreException(new Status(IStatus.ERROR, singleton.getDeclaringExtension().getNamespace(), 0, "Invalid extension (missing id) ", null));//$NON-NLS-1$
        namespace = singleton.getDeclaringExtension().getNamespace();
        
        String location =singleton.getAttribute(ATT_LOCATION);
        if (location != null) {
            if (location.equalsIgnoreCase("left"))
                direction = IPageLayout.LEFT;
            else if (location.equalsIgnoreCase("top"))
                direction = IPageLayout.TOP;
            else if (location.equalsIgnoreCase("bottom"))
                direction = IPageLayout.BOTTOM;
            //no else for right - it is the default value;
        }
    }

    
    public int getLocation() {
        return direction;
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
