/*******************************************************************************
 * Copyright (c) 2005, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Andrew Gvozdev -  Bug 364039 - Add "Delete All Markers"
 *******************************************************************************/
package org.eclipse.ui.internal.views.markers;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.core.resources.IMarker;

/**
 * EditablePropertyTester is a property tester for the editable property of the
 * selected marker.
 *
 * @since 3.4
 *
 */
public class EditablePropertyTester extends PropertyTester {

	private static final Object EDITABLE = "editable"; //$NON-NLS-1$

	/**
	 * Create a new instance of the receiver.
	 */
	public EditablePropertyTester() {
		super();
	}

	@Override
	public boolean test(Object receiver, String property, Object[] args,
			Object expectedValue) {
		if (property.equals(EDITABLE)) {
			MarkerSupportItem item = (MarkerSupportItem) receiver;
			Set<IMarker> markers = new HashSet<>();
			if (item.isConcrete()) {
				markers.add(((MarkerEntry) receiver).getMarker());
			} else {
				for (MarkerSupportItem child : item.getChildren()) {
					if (child.isConcrete()) {
						markers.add(((MarkerEntry) child).getMarker());
					}
				}
			}

			if (!markers.isEmpty()) {
				Iterator<IMarker> elements = markers.iterator();
				while (elements.hasNext()) {
					IMarker marker = elements.next();
					if (!marker.getAttribute(IMarker.USER_EDITABLE, true)) {
						return false;
					}
				}
				return true;
			}
		}
		return false;
	}

}
