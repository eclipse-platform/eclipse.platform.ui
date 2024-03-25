/*******************************************************************************
 * Copyright (c) 2000, 2019 IBM Corporation and others.
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
 *     Alexander Fedorov <alexander.fedorov@arsysop.ru> - Bug 558623
 *******************************************************************************/

package org.eclipse.ui.views.markers;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.e4.ui.internal.workspace.markers.Translation;
import org.eclipse.ui.IMarkerResolution2;

/**
 * WorkbenchMarkerResolution is the resolution that can be grouped
 * with others that are similar to allow multi selection.
 * @since 3.2
 */
public abstract class WorkbenchMarkerResolution implements IMarkerResolution2 {

	/**
	 * Iterate through the list of supplied markers. Return any that can also have
	 * the receiver applied to them.
	 *
	 * @param markers the markers to check
	 * @return IMarker[]
	 */
	public abstract IMarker[] findOtherMarkers(IMarker[] markers);

	/**
	 * Runs this resolution. Resolve all <code>markers</code>.
	 * <code>markers</code> must be a subset of the markers returned
	 * by <code>findOtherMarkers(IMarker[])</code>.
	 *
	 * @param markers The markers to resolve, not null
	 * @param monitor The monitor to report progress
	 */
	public void run(IMarker[] markers, IProgressMonitor monitor) {
		Translation translation = new Translation();
		for (IMarker marker : markers) {
			monitor.subTask(translation.message(marker).orElse("")); //$NON-NLS-1$
			run(marker);
		}
	}
}
