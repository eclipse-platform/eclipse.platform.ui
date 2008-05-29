/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.ui;

import org.eclipse.debug.internal.ui.views.launch.DebugElementHelper;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.ui.model.IWorkbenchAdapter;
import org.eclipse.ui.model.IWorkbenchAdapter2;

/**
 * Common function for debug element workbench adapters.
 * <p>
 * Clients may subclass this class to provide custom adapters for elements in a debug
 * model. The debug platform provides <code>IWorkbenchAdapters</code> for the standard debug
 * elements. Clients may override the default content in the debug view by providing an
 * <code>IWorkbenchAdapter</code> or <code>IDeferredWorkbenchAdapter</code> for a debug
 * element.
 * </p>
 * @since 3.1
 * @deprecated Custom content in the debug views is no longer supported by
 * 	 {@link IWorkbenchAdapter}. Custom content is currently supported
 * 	 by a provisional internal viewer framework.
 */
public abstract class DebugElementWorkbenchAdapter implements IWorkbenchAdapter, IWorkbenchAdapter2 {
    
    /* (non-Javadoc)
     * @see org.eclipse.ui.model.IWorkbenchAdapter#getImageDescriptor(java.lang.Object)
     */
    public ImageDescriptor getImageDescriptor(Object object) {
        return DebugElementHelper.getImageDescriptor(object);
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.model.IWorkbenchAdapter#getLabel(java.lang.Object)
     */
    public String getLabel(Object o) {
        return DebugElementHelper.getLabel(o);
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.model.IWorkbenchAdapter2#getForeground(java.lang.Object)
     */
    public RGB getForeground(Object element) {
        return DebugElementHelper.getForeground(element);
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.model.IWorkbenchAdapter2#getBackground(java.lang.Object)
     */
    public RGB getBackground(Object element) {
        return DebugElementHelper.getBackground(element);
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.model.IWorkbenchAdapter2#getFont(java.lang.Object)
     */
    public FontData getFont(Object element) {
        return DebugElementHelper.getFont(element);
    }

}
