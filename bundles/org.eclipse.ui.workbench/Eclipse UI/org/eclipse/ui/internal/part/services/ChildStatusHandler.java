/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.part.services;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.internal.components.Assert;
import org.eclipse.ui.internal.components.framework.ComponentException;
import org.eclipse.ui.internal.components.framework.IServiceProvider;
import org.eclipse.ui.internal.part.Part;
import org.eclipse.ui.internal.part.components.services.IPartDescriptor;
import org.eclipse.ui.internal.part.components.services.IStatusHandler;
import org.eclipse.ui.internal.part.multiplexer.INestedComponent;
import org.eclipse.ui.internal.part.multiplexer.ISharedContext;

public class ChildStatusHandler implements IStatusHandler, INestedComponent {

    private boolean active = false;
    private IStatusHandler parent;
    private ImageDescriptor image;
    private IStatus message;
    
    /**
     * Component constructor. Do not invoke directly.
     */
    public ChildStatusHandler(IPartDescriptor descr, ISharedContext shared) throws ComponentException {
        Assert.isNotNull(descr);
        IServiceProvider sharedContainer = shared.getSharedComponents();
        
        this.parent = (IStatusHandler)sharedContainer.getService(IStatusHandler.class);
    }
    
    public void set(IStatus message, ImageDescriptor image) {
        this.image = image;
        this.message = message;
        
        if (active) {
            parent.set(message, image);
        }
    }

    public void activate(Part partBeingActivated) {
        parent.set(message, image);
        
        active = true;
    }

    public void deactivate(Object newActive) {
        parent.set(null, null);
        active = false;
    }

}
