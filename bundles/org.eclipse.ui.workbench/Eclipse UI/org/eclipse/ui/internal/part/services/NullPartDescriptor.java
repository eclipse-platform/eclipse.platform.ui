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
package org.eclipse.ui.internal.part.services;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.internal.part.components.services.IPartDescriptor;

/**
 * @since 3.1
 */
public class NullPartDescriptor implements IPartDescriptor {

    /* (non-Javadoc)
     * @see org.eclipse.ui.workbench.services.IPartDescriptor#getId()
     */
    public String getId() {
        return ""; //$NON-NLS-1$
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.workbench.services.IPartDescriptor#getLabel()
     */
    public String getLabel() {
        return ""; //$NON-NLS-1$
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.ui.workbench.services.IPartDescriptor#getImage()
     */
    public ImageDescriptor getImage() {
        return ImageDescriptor.getMissingImageDescriptor();
    }
}
