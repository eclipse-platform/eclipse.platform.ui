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
import org.eclipse.ui.internal.components.Assert;
import org.eclipse.ui.internal.components.framework.ComponentException;
import org.eclipse.ui.internal.components.framework.IServiceProvider;
import org.eclipse.ui.internal.part.Part;
import org.eclipse.ui.internal.part.components.services.INameable;
import org.eclipse.ui.internal.part.components.services.IPartDescriptor;
import org.eclipse.ui.internal.part.multiplexer.INestedComponent;
import org.eclipse.ui.internal.part.multiplexer.ISharedContext;

/**
 * Multiplexed version of the INameable interface.
 * 
 * @since 3.1
 */
public class ChildNameable implements INameable, INestedComponent {

    private String currentName = ""; //$NON-NLS-1$
    private String contentDescription = ""; //$NON-NLS-1$
    private ImageDescriptor image = ImageDescriptor.getMissingImageDescriptor();
    private String tooltip = ""; //$NON-NLS-1$
    private INameable parent;
    private boolean isActive = false;
    
    /**
     * Component constructor. Do not invoke directly.
     */
    public ChildNameable(IPartDescriptor descr, ISharedContext shared) throws ComponentException {
        Assert.isNotNull(descr);
        IServiceProvider sharedContainer = shared.getSharedComponents();
        
        currentName = descr.getLabel();
        Assert.isNotNull(currentName);
        image = descr.getImage();
        this.parent = (INameable)sharedContainer.getService(INameable.class);
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.ui.workbench.services.INameable#setName(java.lang.String)
     */
    public void setName(String newName) {
        Assert.isNotNull(newName);
        if (!newName.equals(currentName)) {
            currentName = newName;
            if (isActive) {
                parent.setName(newName);
            }
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.workbench.services.INameable#setContentDescription(java.lang.String)
     */
    public void setContentDescription(String contentDescription) {
        if (!this.contentDescription.equals(contentDescription)) {
            this.contentDescription = contentDescription;
            if (isActive) {
                parent.setContentDescription(contentDescription);
            }
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.workbench.services.INameable#setImage(org.eclipse.jface.resource.ImageDescriptor)
     */
    public void setImage(ImageDescriptor theImage) {
        if (theImage != image) {
            image = theImage;
            if (isActive) {
                parent.setImage(theImage);
            }
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.workbench.services.INameable#setTooltip(java.lang.String)
     */
    public void setTooltip(String toolTip) {
        if (!toolTip.equals(this.tooltip)) {
            this.tooltip = toolTip;
            if (isActive) {
                parent.setTooltip(toolTip);
            }
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.part.serviceimplementation.multiplexer.INestedComponent#activate()
     */
    public void activate(Part newActivePart) {
        if (isActive) {
            return;
        }

        if (parent != null) {
            parent.setName(currentName);
            parent.setImage(image);
            parent.setTooltip(tooltip);
            parent.setContentDescription(contentDescription);
        }
        
        isActive = true;
    }
    
    public void deactivate(Object newActive) {
        isActive = false;
    }
}
