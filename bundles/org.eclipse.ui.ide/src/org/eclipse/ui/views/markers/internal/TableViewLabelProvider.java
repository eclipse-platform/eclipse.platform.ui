/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.views.markers.internal;

import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

/**
 * The TableViewLabelProvider is the content provider for marker views.
 *
 */
public class TableViewLabelProvider extends LabelProvider implements
        ITableLabelProvider{

    IField[] fields;

    /**
     * Create a neew instance of the receiver.
     * @param fields
     */
    public TableViewLabelProvider(IField[] fields) {
        this.fields = fields;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnImage(java.lang.Object, int)
     */
    public Image getColumnImage(Object element, int columnIndex) {
        if (fields == null || columnIndex < 0 || columnIndex >= fields.length) {
            return null;
        }
        return fields[columnIndex].getImage(element);
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnText(java.lang.Object, int)
     */
    public String getColumnText(Object element, int columnIndex) {
        if (fields == null || columnIndex < 0 || columnIndex >= fields.length) {
            return null;
        }
        return fields[columnIndex].getValue(element);
    }

    
    

}
