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

package org.eclipse.ui.provisional.views.markers;


/**
 * IMarkerProvider is the specification of a contributed object that provides
 * marker support.
 * @since 3.4
 *
 */
public interface IMarkerProvider {

	/**
	 * Get the fields for the receiver.
	 * @return
	 */
	IMarkerField[] getFields();

	/**
	 * Return whether or not markerField is the primary sort field.
	 * @param markerField
	 * @return boolean
	 */
	boolean isPrimarySortField(IMarkerField markerField);

}
