/*******************************************************************************
 * Copyright (c) 2005, 2015 IBM Corporation and others.
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

	@Override
	public String getDescription() {
		return MarkerMessages.description_type;
	}

	@Override
	public Image getDescriptionImage() {
		return null;
	}

	@Override
	public String getColumnHeaderText() {
		return getDescription();
	}

	@Override
	public Image getColumnHeaderImage() {
		return null;
	}

	@Override
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

	@Override
	public Image getImage(Object obj) {
		return null;
	}

	@Override
	public int compare(Object obj1, Object obj2) {
		return getValue(obj1).compareTo(getValue(obj2));
	}

	@Override
	public int getDefaultDirection() {
		return TableComparator.ASCENDING;
	}

	@Override
	public int getPreferredWidth() {
		return 200;
	}

}
