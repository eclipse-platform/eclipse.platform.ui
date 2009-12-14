/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.views.breakpoints;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.breakpoints.provisional.IBreakpointOrganizer;
import org.eclipse.debug.internal.ui.views.DebugUIViewsMessages;
import org.eclipse.debug.ui.IBreakpointOrganizerDelegate;
import org.eclipse.debug.ui.IBreakpointOrganizerDelegateExtension;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.util.IPropertyChangeListener;

/**
 * A contributed breakpoint organizer.
 */
public class BreakpointOrganizerExtension implements IBreakpointOrganizer, IBreakpointOrganizerDelegateExtension {
	
	private IConfigurationElement fElement;
	private IBreakpointOrganizerDelegate fDelegate;
	private ImageDescriptor fDescriptor;
	
	// attributes
	public static final String ATTR_LABEL = "label"; //$NON-NLS-1$
	public static final String ATTR_CLASS = "class"; //$NON-NLS-1$
	public static final String ATTR_ID = "id"; //$NON-NLS-1$
	public static final String ATTR_ICON = "icon"; //$NON-NLS-1$
    public static final String ATTR_OTHERS_LABEL = "othersLabel"; //$NON-NLS-1$
	
	public BreakpointOrganizerExtension(IConfigurationElement element) {
		fElement = element;
	}
	
	/**
	 * Returns the image descriptor for this organizer.
	 * 
	 * @return image descriptor
	 */
	public ImageDescriptor getImageDescriptor() {
		if (fDescriptor == null) {
			fDescriptor = DebugUIPlugin.getImageDescriptor(fElement, ATTR_ICON);
			if (fDescriptor == null) {
				fDescriptor = ImageDescriptor.getMissingImageDescriptor();
			}
		}
		return fDescriptor;		
	}
	
	/**
	 * Returns this organizer's label.
	 * 
	 * @return this organizer's label
	 */
	public String getLabel() {
		return fElement.getAttribute(ATTR_LABEL);
	}
    
    /**
     * Returns this organizer's identifier.
     * 
     * @return this organizer's identifier
     */
    public String getIdentifier() {
        return fElement.getAttribute(ATTR_ID);
    }
	
	/**
	 * Returns this organizer's delegate, instantiating it if required.
	 * 
	 * @return this organizer's delegate
	 */
	protected IBreakpointOrganizerDelegate getOrganizer() {
		if (fDelegate == null) {
			try {
				fDelegate = (IBreakpointOrganizerDelegate) fElement.createExecutableExtension(ATTR_CLASS);
			} catch (CoreException e) {
				DebugUIPlugin.log(e);
			}
		}
		return fDelegate;
	}

    /* (non-Javadoc)
     * @see org.eclipse.debug.ui.IBreakpointOrganizerDelegate#getCategories(org.eclipse.debug.core.model.IBreakpoint)
     */
    public IAdaptable[] getCategories(IBreakpoint breakpoint) {
        return getOrganizer().getCategories(breakpoint);
    }

    /* (non-Javadoc)
     * @see org.eclipse.debug.ui.IBreakpointOrganizerDelegate#addPropertyChangeListener(org.eclipse.jface.util.IPropertyChangeListener)
     */
    public void addPropertyChangeListener(IPropertyChangeListener listener) {
        getOrganizer().addPropertyChangeListener(listener);
    }

    /* (non-Javadoc)
     * @see org.eclipse.debug.ui.IBreakpointOrganizerDelegate#removePropertyChangeListener(org.eclipse.jface.util.IPropertyChangeListener)
     */
    public void removePropertyChangeListener(IPropertyChangeListener listener) {
        getOrganizer().removePropertyChangeListener(listener);
    }

    /* (non-Javadoc)
     * @see org.eclipse.debug.ui.IBreakpointOrganizerDelegate#addBreakpoint(org.eclipse.debug.core.model.IBreakpoint, org.eclipse.core.runtime.IAdaptable)
     */
    public void addBreakpoint(IBreakpoint breakpoint, IAdaptable category) {
        getOrganizer().addBreakpoint(breakpoint, category);
    }

    /* (non-Javadoc)
     * @see org.eclipse.debug.ui.IBreakpointOrganizerDelegate#removeBreakpoint(org.eclipse.debug.core.model.IBreakpoint, org.eclipse.core.runtime.IAdaptable)
     */
    public void removeBreakpoint(IBreakpoint breakpoint, IAdaptable category) {
        getOrganizer().removeBreakpoint(breakpoint, category);
    }

    /* (non-Javadoc)
     * @see org.eclipse.debug.ui.IBreakpointOrganizerDelegate#canAdd(org.eclipse.debug.core.model.IBreakpoint, org.eclipse.core.runtime.IAdaptable)
     */
    public boolean canAdd(IBreakpoint breakpoint, IAdaptable category) {
        return getOrganizer().canAdd(breakpoint, category);
    }

    /* (non-Javadoc)
     * @see org.eclipse.debug.ui.IBreakpointOrganizerDelegate#canRemove(org.eclipse.debug.core.model.IBreakpoint, org.eclipse.core.runtime.IAdaptable)
     */
    public boolean canRemove(IBreakpoint breakpoint, IAdaptable category) {
        return getOrganizer().canRemove(breakpoint, category);
    }

    /* (non-Javadoc)
     * @see org.eclipse.debug.ui.IBreakpointOrganizerDelegate#dispose()
     */
    public void dispose() {
    	// don't instantiate the delegate if it has not been used
    	if (fDelegate != null) {
    		fDelegate.dispose();
    	}
    }

    /* (non-Javadoc)
     * @see org.eclipse.debug.internal.ui.views.breakpoints.IBreakpointOrganizer#getOthersLabel()
     */
    public String getOthersLabel() {
        String attribute = fElement.getAttribute(ATTR_OTHERS_LABEL);
        if (attribute == null) {
            return DebugUIViewsMessages.OtherBreakpointOrganizer_0; 
        }
        return attribute;
    }

    /* (non-Javadoc)
     * @see org.eclipse.debug.ui.IBreakpointOrganizerDelegate#getCategories()
     */
    public IAdaptable[] getCategories() {
        return getOrganizer().getCategories();
    }

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.IBreakpointOrganizerDelegateExtension#addBreakpoints(org.eclipse.debug.core.model.IBreakpoint[], org.eclipse.core.runtime.IAdaptable)
	 */
	public void addBreakpoints(IBreakpoint[] breakpoints, IAdaptable category) {
		IBreakpointOrganizerDelegate organizer = getOrganizer();
		if (organizer instanceof IBreakpointOrganizerDelegateExtension) {
			((IBreakpointOrganizerDelegateExtension)organizer).addBreakpoints(breakpoints, category);
		} else {
			for (int i = 0; i < breakpoints.length; i++) {
				addBreakpoint(breakpoints[i], category);
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.IBreakpointOrganizerDelegateExtension#removeBreakpoints(org.eclipse.debug.core.model.IBreakpoint[], org.eclipse.core.runtime.IAdaptable)
	 */
	public void removeBreakpoints(IBreakpoint[] breakpoints, IAdaptable category) {
		IBreakpointOrganizerDelegate organizer = getOrganizer();
		if (organizer instanceof IBreakpointOrganizerDelegateExtension) {
			((IBreakpointOrganizerDelegateExtension)organizer).removeBreakpoints(breakpoints, category);
		} else {
			for (int i = 0; i < breakpoints.length; i++) {
				removeBreakpoint(breakpoints[i], category);
			}
		}
		
	}
}
