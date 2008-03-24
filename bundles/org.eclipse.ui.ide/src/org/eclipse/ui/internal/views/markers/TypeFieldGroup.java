/*******************************************************************************
 * Copyright (c) 2007, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.internal.views.markers;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.internal.ide.Policy;
import org.eclipse.ui.views.markers.internal.AbstractField;
import org.eclipse.ui.views.markers.internal.MarkerMessages;
import org.eclipse.ui.views.markers.internal.MarkerType;
import org.eclipse.ui.views.markers.internal.MarkerTypesModel;
import org.eclipse.ui.views.markers.internal.TableComparator;

/**
 * TypeFieldGroup is the field used to group by type.
 * 
 * @since 3.3
 * 
 */
public class TypeFieldGroup extends AbstractField {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.views.markers.internal.IField#getDescription()
	 */
	public String getDescription() {
		return MarkerMessages.description_type;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.views.markers.internal.IField#getDescriptionImage()
	 */
	public Image getDescriptionImage() {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.views.markers.internal.IField#getColumnHeaderText()
	 */
	public String getColumnHeaderText() {
		return getDescription();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.views.markers.internal.IField#getColumnHeaderImage()
	 */
	public Image getColumnHeaderImage() {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.views.markers.internal.IField#getValue(java.lang.Object)
	 */
	public String getValue(Object obj) {

		String typeId;
		try {
			typeId = ((IMarker) obj).getType();
		} catch (CoreException e) {
			Policy.handle(e);
			return MarkerMessages.FieldCategory_Uncategorized;
		}
		MarkerType type = MarkerTypesModel.getInstance().getType(typeId);
		return type.getLabel();

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.views.markers.internal.IField#getImage(java.lang.Object)
	 */
	public Image getImage(Object obj) {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.views.markers.internal.IField#compare(java.lang.Object,
	 *      java.lang.Object)
	 */
	public int compare(Object obj1, Object obj2) {
		return getValue(obj1).compareTo(getValue(obj2));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.views.markers.internal.IField#getDefaultDirection()
	 */
	public int getDefaultDirection() {
		return TableComparator.ASCENDING;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.views.markers.internal.IField#getPreferredWidth()
	 */
	public int getPreferredWidth() {
		return 200;
	}

}
