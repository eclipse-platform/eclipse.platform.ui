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

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.views.DebugUIViewsMessages;
import org.eclipse.debug.ui.IBreakpointContainer;
import org.eclipse.debug.ui.IBreakpointContainerFactory;
import org.eclipse.debug.ui.IBreakpointContainerFactoryDelegate;
import org.eclipse.jface.resource.ImageDescriptor;
import org.osgi.framework.Bundle;

/**
 * Breakpoint container factories create containers by querying their
 * delegates. Delegates are only instantiated as necessary, to avoid
 * early plugin loading.
 */
public class BreakpointContainerFactory implements IBreakpointContainerFactory {
	
	private ImageDescriptor fImage;
	private IBreakpointContainerFactoryDelegate fDelegate;
	private IConfigurationElement fConfigurationElement;
	
	/**
	 * Creates a new breakpoint container factory from the given configuration
	 * element. This element will be used to supply all the information this
	 * factory needs.
	 * @param element the configuration element representing the extension
	 *  specified for this factory
	 */
	public BreakpointContainerFactory(IConfigurationElement element) {
		fConfigurationElement= element;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.views.breakpoints.IBreakpointContainerFactory#getLabel()
	 */
	public String getLabel() {
		return fConfigurationElement.getAttribute("label"); //$NON-NLS-1$
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.views.breakpoints.IBreakpointContainerFactory#getImage()
	 */
	public ImageDescriptor getImageDescriptor() {
		if (fImage == null) {
			fImage= loadImageDescriptor();
		}
		return fImage;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.views.breakpoints.IBreakpointContainerFactory#getIdentifier()
	 */
	public String getIdentifier() {
		return fConfigurationElement.getAttribute("id"); //$NON-NLS-1$
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.views.breakpoints.IBreakpointContainerFactory#getDelegate()
	 */
	public IBreakpointContainerFactoryDelegate getDelegate() {
		if (fDelegate == null) {
			try {
				fDelegate= (IBreakpointContainerFactoryDelegate) fConfigurationElement.createExecutableExtension("class"); //$NON-NLS-1$
			} catch (CoreException e) {
				DebugUIPlugin.log(e);
			}
		}
		return fDelegate;
	}

    /* (non-Javadoc)
     * @see org.eclipse.debug.internal.ui.views.breakpoints.IBreakpointContainerFactory#getContainers(org.eclipse.debug.internal.ui.views.breakpoints.IBreakpointContainer)
     */
    public IBreakpointContainer[] getContainers(IBreakpointContainer parentContainer) {
    	IBreakpoint[] breakpoints= getBreakpoints(parentContainer);
    	IBreakpointContainerFactoryDelegate delegate= getDelegate();
    	if (delegate == null) {
    		return new IBreakpointContainer[] { new BreakpointContainer(breakpoints, this, DebugUIViewsMessages.getString("BreakpointContainerFactory.0")) }; //$NON-NLS-1$
    	}
        IBreakpointContainer[] containers = delegate.createContainers(breakpoints, this);
        if (parentContainer != null) {
            parentContainer.setContainers(containers);
            for (int i = 0; i < containers.length; i++) {
				containers[i].setParentContainer(parentContainer);
			}
        }
        return containers;
    }

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.views.breakpoints.IBreakpointContainerFactory#dispose()
	 */
	public void dispose() {
		if (fDelegate != null) {
			fDelegate.dispose();
		}
	}
    
    /**
     * Returns the breakpoints associated with the given container, which can
     * be <code>null</code>. If <code>null</code>, all breakpoints are returned
     * from the breakpoint manager
     * @param container the given container or <code>null</code>
     * @return the breakpoints associated with the given container
     */
    public IBreakpoint[] getBreakpoints(IBreakpointContainer container) {
        if (container != null) {
            return container.getBreakpoints();
        }
        return DebugPlugin.getDefault().getBreakpointManager().getBreakpoints();
    }
	
	/**
	 * Returns the image for this factory, or <code>null</code> if none
	 * @return the image for this factory, or <code>null</code> if none
	 */
	protected ImageDescriptor loadImageDescriptor() {
		ImageDescriptor descriptor= null;
		String iconPath = fConfigurationElement.getAttribute("icon"); //$NON-NLS-1$
		// iconPath may be null because icon is optional
		if (iconPath != null) {
			try {
				Bundle bundle = Platform.getBundle(fConfigurationElement.getDeclaringExtension().getNamespace());
				URL iconURL = bundle.getEntry("/"); //$NON-NLS-1$
				iconURL = new URL(iconURL, iconPath);
				descriptor = ImageDescriptor.createFromURL(iconURL);
			} catch (MalformedURLException e) {
				DebugUIPlugin.log(e);
			}
		}
		return descriptor;
	}
}
