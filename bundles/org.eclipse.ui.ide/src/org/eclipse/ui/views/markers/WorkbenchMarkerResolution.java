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

import org.eclipse.core.resources.IMarker;
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
	 * @deprecated This method is no longer in use and will be deleted
	 * during 3.2 M5.
	 * @see #findOtherMarkers(IMarker[])
	 */
	public boolean canBeGroupedWith(WorkbenchMarkerResolution resolution){
		return false;
	}
	
	/**
	 * Return an updated WorkbenchMarkerResolution for the receiver.
	 * This is called after another WorkbenchMarkerResolution has been
	 * applied in changes are required.
	 * @return WorkbenchMarkerResolution
	 * @deprecated This method is no longer in use and will be deleted
	 * during 3.2 M5.
	 * @see #findOtherMarkers(IMarker[])
	 */
	public WorkbenchMarkerResolution getUpdatedResolution(){
		return this;
	}
	
	/**
	 * Iterate through the list of supplied markers. Return any that can also have
	 * the receiver applied to them.
	 * @param markers
	 * @return IMarker[]
	 * <strong>NOTE:</strong> This method will become abstract for
	 * 3.2 M5.
	 * 	 
	 * */
	public IMarker[] findOtherMarkers(IMarker[] markers){
		return new IMarker[0];
	}
}
