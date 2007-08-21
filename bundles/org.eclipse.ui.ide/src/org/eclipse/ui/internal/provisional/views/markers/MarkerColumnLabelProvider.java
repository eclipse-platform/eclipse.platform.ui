/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.internal.provisional.views.markers;

import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.internal.provisional.views.markers.api.MarkerField;
import org.eclipse.ui.internal.provisional.views.markers.api.MarkerItem;

/**
 * The MarkerColumnLabelProvider is a label provider for an individual column.
 * 
 * @since 3.4
 * 
 */
public class MarkerColumnLabelProvider extends ColumnLabelProvider {

	MarkerField field;

	/**
	 * Create a MarkerViewLabelProvider on a field.
	 * 
	 * @param field
	 */
	MarkerColumnLabelProvider(MarkerField field) {
		this.field = field;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.ColumnLabelProvider#getText(java.lang.Object)
	 */
	public String getText(Object element) {
		return field.getValue((MarkerItem) element);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.ColumnLabelProvider#getImage(java.lang.Object)
	 */
	public Image getImage(Object element) {
		return field.getImage((MarkerItem)element);
	}

	
}
