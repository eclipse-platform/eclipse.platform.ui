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
 * This service allows a part to change its name, content description, image, and tooltip.
 * Parts can take an INameable in their constructor.
 * <p>
 * This interface is typically implemented by an anonymous class in the owner of a part if it 
 * cares about name changes in its children.
 * </p>
 * 
 * @since 3.1
 */
public interface INameable {
    /**
     * Sets the name of the part. The part name is typically displayed in the part's tab. It is
     * possible, but not recommended, to set a part's name to the empty string. 
     *
     * @param newName new part name
     */
	public void setName(String newName);
    
    /**
     * Sets the content description. This is a user-readable description of the current contents
     * of the part. This may be appended to the part name in brackets (or some locale-specific equivalent), 
     * so it should not contain the part name or brackets. Use the empty string (not null) to clear. 
     *
     * @param contentDescription new content description
     */
	public void setContentDescription(String contentDescription);
    
    /**
     * Sets the title image, or null to clear.  
     *
     * @param theImage new title image or null to clear
     */
	public void setImage(ImageDescriptor theImage);
    
    /**
     * Sets the tooltip. Use the empty string (not null) to clear.
     *
     * @param toolTip
     */
	public void setTooltip(String toolTip);
}
