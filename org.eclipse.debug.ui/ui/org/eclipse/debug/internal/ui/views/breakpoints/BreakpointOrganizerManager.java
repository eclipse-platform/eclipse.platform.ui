/*******************************************************************************
 * Copyright (c) 2004, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.views.breakpoints;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Platform;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.breakpoints.provisional.IBreakpointOrganizer;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;

/**
 * Manager which provides access to the breakpoint organizers
 * contributed via the org.eclipse.debug.ui.breakpointOrganizers
 * extension point.
 * <p>
 * Manages the default breakpoint working set and places newly
 * create breakpoints in to that set.
 * </p>
 * @since 3.1
 */
public class BreakpointOrganizerManager {
	
	private static BreakpointOrganizerManager fgManager;
	
	// map for lookup by id
    private Map fOrganizers = new HashMap();
    // cached sorted list by label
    private List fSorted = null;

	/**
	 * Returns the singleton instance of the breakpoint container
	 * factory manager.
	 * @return the singleton {@link BreakpointOrganizerManager}
	 */
	public static BreakpointOrganizerManager getDefault() {
		if (fgManager == null) {
			fgManager= new BreakpointOrganizerManager();
		}
		return fgManager;
	}
	
	/**
	 * Creates and initializes a new breakpoint container factory.
	 */
	private BreakpointOrganizerManager() {
        loadOrganizers();
        // force the working set organizers to initialize their listeners
        start("org.eclipse.debug.ui.workingSetOrganizer"); //$NON-NLS-1$
        start("org.eclipse.debug.ui.breakpointWorkingSetOrganizer"); //$NON-NLS-1$
	}
	
	/**
	 * Forces instantiation of orgranizer delegate.
	 * 
	 * @param organizerId organizer to start
	 */
	private void start(String organizerId) {
        IBreakpointOrganizer organizer = getOrganizer(organizerId);
        IPropertyChangeListener listener = new IPropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent event) {
            }
        };
        organizer.addPropertyChangeListener(listener);
        organizer.removePropertyChangeListener(listener);		
	}
    
    /**
     * Loads all contributed breakpoint organizers.
     */
    private void loadOrganizers() {
        IExtensionPoint extensionPoint= Platform.getExtensionRegistry().getExtensionPoint(DebugUIPlugin.getUniqueIdentifier(), IDebugUIConstants.EXTENSION_POINT_BREAKPOINT_ORGANIZERS);
        IConfigurationElement[] configurationElements = extensionPoint.getConfigurationElements();
        for (int i = 0; i < configurationElements.length; i++) {
            IConfigurationElement element= configurationElements[i];
            IBreakpointOrganizer organizer = new BreakpointOrganizerExtension(element);
            if (validateOrganizer(organizer)) {
                fOrganizers.put(organizer.getIdentifier(), organizer);
            }
        }
    }    
	
    /**
     * Validates the given organizer. Checks that certain required attributes
     * are available.
     * @param organizer the organizer to check
     * @return whether the given organizer is valid
     */
    protected static boolean validateOrganizer(IBreakpointOrganizer organizer) {
        String id = organizer.getIdentifier();
        String label = organizer.getLabel();
        return id != null && id.length() > 0 && label != null && label.length() > 0;
    }    
	
    /**
     * Returns all contributed breakpoint organizers.
     * 
     * @return all contributed breakpoint organizers
     */
    public IBreakpointOrganizer[] getOrganizers() {
    	if (fSorted == null) {
	        Collection collection = fOrganizers.values();
	        fSorted = new ArrayList();
	        fSorted.addAll(collection);
	        Collections.sort(fSorted, new Comparator() {
				public int compare(Object o1, Object o2) {
					IBreakpointOrganizer b1 = (IBreakpointOrganizer)o1;
					IBreakpointOrganizer b2 = (IBreakpointOrganizer)o2;
					return b1.getLabel().compareTo(b2.getLabel());
				}

				public boolean equals(Object obj) {
					return this == obj;
				}
			});
    	}
    	return (IBreakpointOrganizer[]) fSorted.toArray(new IBreakpointOrganizer[fSorted.size()]);
    }
    
    /**
     * Returns the specified breakpoint organizer or <code>null</code>
     * @param id organizer identifier
     * @return breakpoint organizer or <code>null</code>
     */
    public IBreakpointOrganizer getOrganizer(String id) {
        return (IBreakpointOrganizer) fOrganizers.get(id);
    }
    
    /**
     * Shuts down the organizer manager, disposing organizers.
     */
    public void shutdown() {
        IBreakpointOrganizer[] organizers = getOrganizers();
        for (int i = 0; i < organizers.length; i++) {
            IBreakpointOrganizer organizer = organizers[i];
            organizer.dispose();
        }
    }

}
