/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.views.markers.internal;

import org.eclipse.core.resources.IMarker;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

class MarkerLabelProvider extends LabelProvider implements ITableLabelProvider {

    IField[] properties;

    public MarkerLabelProvider(IField[] properties) {
        this.properties = properties;
    }

    public Image getColumnImage(Object element, int columnIndex) {
        if (element == null || !(element instanceof IMarker)
                || properties == null || columnIndex >= properties.length) {
			return null;
		}

        return properties[columnIndex].getImage(element);
    }

    public String getColumnText(Object element, int columnIndex) {
        if (element == null || !(element instanceof IMarker)
                || properties == null || columnIndex >= properties.length) {
			return ""; //$NON-NLS-1$
		}

        return properties[columnIndex].getValue(element);
    }
}
