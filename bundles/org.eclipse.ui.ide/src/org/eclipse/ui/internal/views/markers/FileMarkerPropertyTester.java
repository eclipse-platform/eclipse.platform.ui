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

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.core.resources.IResource;

/**
 * FileMarkerPropertyTester is a property tester for a marker entry to see if it
 * is a marker that has a file behind it.
 *
 * @since 3.4
 *
 */
public class FileMarkerPropertyTester extends PropertyTester {

	private static final Object FILE_MARKER = "fileMarker"; //$NON-NLS-1$

	/**
	 * Create a new instance of the receiver.
	 */
	public FileMarkerPropertyTester() {
		super();
	}

	@Override
	public boolean test(Object receiver, String property, Object[] args,
			Object expectedValue) {
		if (property.equals(FILE_MARKER)) {
			if (((MarkerEntry) receiver).getMarker().getResource().getType() == IResource.FILE)
				return true;
		}
		return false;
	}

}
