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
import org.eclipse.ui.internal.part.components.services.INameable;

/**
 * Default implementation of the Nameable service. All methods are no-ops.
 * 
 * @since 3.1
 */
public class NullNameableService implements INameable {

    /**
     * Component constructor. Do not invoke directly.
     */
    public NullNameableService() {
        
    }
    
	/* (non-Javadoc)
	 * @see org.eclipse.ui.workbench.services.INameable#setName(java.lang.String)
	 */
	public void setName(String newName) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.workbench.services.INameable#setContentDescription(java.lang.String)
	 */
	public void setContentDescription(String contentDescription) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.workbench.services.INameable#setImage(org.eclipse.swt.graphics.Image)
	 */
	public void setImage(ImageDescriptor theImage) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.workbench.services.INameable#setTooltip(java.lang.String)
	 */
	public void setTooltip(String toolTip) {
	}

}
