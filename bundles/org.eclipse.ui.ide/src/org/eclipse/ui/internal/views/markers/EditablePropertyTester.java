/*******************************************************************************
 * Copyright (c) 2005, 2011 IBM Corporation and others.
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

	/* (non-Javadoc)
	 * @see org.eclipse.core.expressions.IPropertyTester#test(java.lang.Object, java.lang.String, java.lang.Object[], java.lang.Object)
	 */
	public boolean test(Object receiver, String property, Object[] args,
			Object expectedValue) {
		if (property.equals(EDITABLE)) {
			MarkerSupportItem item = (MarkerSupportItem) receiver;
			Set/*<IMarker>*/ markers = new HashSet();
			if (item.isConcrete()) {
				markers.add(((MarkerEntry) receiver).getMarker());
			} else {
				MarkerSupportItem[] children = item.getChildren();
				for (int i = 0; i < children.length; i++) {
					if (children[i].isConcrete())
						markers.add(((MarkerEntry) children[i]).getMarker());
				}
			}

			if (!markers.isEmpty()) {
				Iterator elements = markers.iterator();
				while (elements.hasNext()) {
					IMarker marker = (IMarker) elements.next();
					if (!marker.getAttribute(IMarker.USER_EDITABLE, true))
						return false;
				}
				return true;
			}
		}
		return false;
	}

}
