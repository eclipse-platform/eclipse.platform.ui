/*******************************************************************************
 * Copyright (c) 2009 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.viewers.model;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.RGB;

/**
 * This interface must be implemented by the viewer which uses the
 * {@link TreeModelLabelProvider} label provider.  It allows the label
 * provider to update the viewer with information retrieved from the 
 * element-based label providers.
 * 
 * @since 3.5
 */
public interface ITreeModelLabelProviderTarget extends ITreeModelViewer {

    /**
     * Sets the element's display information.
     * <p>
     * This method should only be called by the viewer framework.
     * </p>
     * 
     * @param path Element path. 
     * @param numColumns Number of columns in the data.
     * @param labels Array of labels.  The array cannot to be 
     * <code>null</code>, but values within the array may be.
     * @param images Array of image descriptors, may be <code>null</code>.
     * @param fontDatas Array of fond data objects, may be <code>null</code>.
     * @param foregrounds Array of RGB values for foreground colors, may be 
     * <code>null</code>.
     * @param backgrounds Array of RGB values for background colors, may be 
     * <code>null</code>.
     */
    public void setElementData(TreePath path, int numColumns, String[] labels, ImageDescriptor[] images, FontData[] fontDatas, RGB[] foregrounds, RGB[] backgrounds); 

    /**
     * Returns identifiers of the visible columns in this viewer, or <code>null</code>
     * if there is currently no column presentation.
     *  
     * @return visible columns or <code>null</code>
     */
    public String[] getVisibleColumns();
}
