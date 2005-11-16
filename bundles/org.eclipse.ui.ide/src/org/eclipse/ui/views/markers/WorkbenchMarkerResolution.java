/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.views.markers;

import org.eclipse.ui.IMarkerResolution2;

/**
 * WorkbenchMarkerResolution is the resolution that can be grouped
 * with others that are similar to allow multi selection.
 * @since 3.2
 * <strong>NOTE:</strong> This API is experimental and subject to
 * change in the 3.2 development cycle.
 *
 */
public abstract class WorkbenchMarkerResolution implements IMarkerResolution2 {

	/**
	 * Return whether or not the receiver can be grouped with
	 * resolution in the Quick Fix dialog.
	 * @param resolution
	 * @return boolean <code>true</code> if the receiver can
	 * be grouped with resolution
	 */
	public abstract boolean canBeGroupedWith(WorkbenchMarkerResolution resolution);
	
	/**
	 * Return an updated WorkbenchMarkerResolution for the receiver.
	 * This is called after another WorkbenchMarkerResolution has been
	 * applied in changes are required.
	 * @return WorkbenchMarkerResolution
	 */
	public abstract WorkbenchMarkerResolution getUpdatedResolution();
}
