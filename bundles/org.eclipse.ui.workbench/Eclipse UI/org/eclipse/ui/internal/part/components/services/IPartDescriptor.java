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
package org.eclipse.ui.internal.part.components.services;

import org.eclipse.jface.resource.ImageDescriptor;

/**
 * Service that provides information about a part.
 * 
 * Not intended to be implemented by clients
 * 
 * @since 3.1
 */
public interface IPartDescriptor {
    /**
     * ID for the part
     *
     * @return the ID for the part
     */
	String getId();
    
    /**
     * Returns the default name for the part 
     *
     * @return the default name for the part
     */
	String getLabel();
    
    /**
     * Returns the default image for the part
     * 
     * @return the default image for the part
     */
    ImageDescriptor getImage();
}
