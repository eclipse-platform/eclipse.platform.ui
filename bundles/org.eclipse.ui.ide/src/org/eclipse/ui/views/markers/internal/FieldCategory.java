/*******************************************************************************
 * Copyright (c) 2005, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.views.markers.internal;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.internal.ide.Policy;

/**
 * FieldCategory is the field for showing categories of markers.
 * 
 */
public class FieldCategory extends AbstractField {

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

		if (obj instanceof ConcreteMarker) {
			ConcreteMarker marker = (ConcreteMarker) obj;

			if (marker.getGroup() == null) {
				if (!marker.getMarker().exists())
					return MarkerMessages.FieldCategory_Uncategorized;
				String groupName = MarkerSupportRegistry.getInstance()
						.getCategory(marker.getMarker());
				if (groupName == null) {

					String typeId;
					try {
						typeId = marker.getMarker().getType();
					} catch (CoreException e) {
						Policy.handle(e);
						return MarkerMessages.FieldCategory_Uncategorized;
					}
					MarkerType type = MarkerTypesModel.getInstance().getType(
							typeId);
					groupName = type.getLabel();
				}
				marker.setGroup(groupName);
			}

			return (String) marker.getGroup();

		}
		return Util.EMPTY_STRING;
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
