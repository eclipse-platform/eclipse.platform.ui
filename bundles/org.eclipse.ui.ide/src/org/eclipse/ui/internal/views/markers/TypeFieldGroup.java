/*******************************************************************************
 * Copyright (c) 2007, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
 */
public class TypeFieldGroup extends AbstractField {

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
