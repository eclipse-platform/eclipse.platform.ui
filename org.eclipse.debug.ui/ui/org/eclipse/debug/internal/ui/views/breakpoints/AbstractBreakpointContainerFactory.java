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
package org.eclipse.debug.internal.ui.views.breakpoints;

import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.jface.resource.ImageDescriptor;

/**
 * Abstract implementation of a breakpoint container factory.
 * 
 * Clients are intended to extend this class.
 * 
 * @since 3.1
 */
public abstract class AbstractBreakpointContainerFactory implements IBreakpointContainerFactory {
	
	protected String fLabel;
	protected String fIdentifier;
	private ImageDescriptor fImage;

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.views.breakpoints.IBreakpointContainerFactory#getLabel()
	 */
	public String getLabel() {
		return fLabel;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.views.breakpoints.IBreakpointContainerFactory#setLabel(java.lang.String)
	 */
	public void setLabel(String label) {
		fLabel= label;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.views.breakpoints.IBreakpointContainerFactory#getImage()
	 */
	public ImageDescriptor getImageDescriptor() {
		return fImage;
	}
	
	
	public void setImageDescriptor(ImageDescriptor image) {
		fImage= image;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.views.breakpoints.IBreakpointContainerFactory#setIdentifier(java.lang.String)
	 */
	public void setIdentifier(String identifier) {
		fIdentifier= identifier;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.views.breakpoints.IBreakpointContainerFactory#getIdentifier()
	 */
	public String getIdentifier() {
		return fIdentifier;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.views.breakpoints.IBreakpointContainerFactory#dispose()
	 */
	public void dispose() {
	}

    public final IBreakpointContainer[] getContainers(IBreakpointContainer parentContainer) {
        IBreakpointContainer[] containers = createContainers(parentContainer);
        if (parentContainer != null) {
            parentContainer.setContainers(containers);
        }
        return containers;
    }
    
    public IBreakpoint[] getBreakpoints(IBreakpointContainer container) {
        if (container != null) {
            return container.getBreakpoints();
        }
        return DebugPlugin.getDefault().getBreakpointManager().getBreakpoints();
    }
    
    public abstract IBreakpointContainer[] createContainers(IBreakpointContainer parentContainer);
}
