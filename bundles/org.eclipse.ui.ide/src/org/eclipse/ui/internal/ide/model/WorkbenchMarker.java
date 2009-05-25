/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.internal.ide.model;

import org.eclipse.core.resources.IMarker;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IMarkerActionFilter;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;
import org.eclipse.ui.internal.views.markers.MarkerPropertyTester;
import org.eclipse.ui.model.WorkbenchAdapter;

/**
 * Model object for adapting IMarker objects to the IWorkbenchAdapter
 * interface.
 */
public class WorkbenchMarker extends WorkbenchAdapter implements
        IMarkerActionFilter {
    /*
     *  (non-Javadoc)
     * @see org.eclipse.ui.model.IWorkbenchAdapter#getImageDescriptor(java.lang.Object)
     */
    public ImageDescriptor getImageDescriptor(Object o) {
        if (!(o instanceof IMarker)) {
			return null;
		}
        return IDEWorkbenchPlugin.getDefault().getMarkerImageProviderRegistry()
                .getImageDescriptor((IMarker) o);
    }

    /*
     *  (non-Javadoc)
     * @see org.eclipse.ui.model.IWorkbenchAdapter#getLabel(java.lang.Object)
     */
    public String getLabel(Object o) {
        IMarker marker = (IMarker) o;
        return marker.getAttribute(IMarker.MESSAGE, "");//$NON-NLS-1$
    }

    /*
     *  (non-Javadoc)
     * @see org.eclipse.ui.model.IWorkbenchAdapter#getParent(java.lang.Object)
     */
    public Object getParent(Object o) {
        return ((IMarker) o).getResource();
    }

    /**
     * Returns whether the specific attribute matches the state of the target
     * object.
     *
     * @param target the target object
     * @param name the attribute name
     * @param value the attriute value
     * @return <code>true</code> if the attribute matches; <code>false</code> otherwise
     */
    public boolean testAttribute(Object target, String name, String value) {
        return MarkerPropertyTester.test((IMarker) target, name, value);
    }
}
