/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal.provisional.views.markers;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.statushandlers.StatusManager;

/**
 * TaskPropertyTester is the property tester for whether or not a task is being
 * shown.
 * 
 * @since 3.4
 * 
 */
public class TaskPropertyTester extends PropertyTester {

	private static final String ATTRIBUTE_TYPE = "type"; //$NON-NLS-1$

	/**
	 * Create a new instance of the receiver.
	 */
	public TaskPropertyTester() {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.expressions.IPropertyTester#test(java.lang.Object, java.lang.String, java.lang.Object[], java.lang.Object)
	 */
	public boolean test(Object receiver, String property, Object[] args,
			Object expectedValue) {
		if (property.equals(ATTRIBUTE_TYPE)) {
			IMarker marker = ((MarkerEntry) receiver).getMarker();
			if (marker == null)
				return false;
			try {
				return marker.isSubtypeOf(IMarker.TASK);
			} catch (CoreException e) {
				StatusManager.getManager().handle(e.getStatus());
				return false;
			}
		}
		return false;
	}
}
