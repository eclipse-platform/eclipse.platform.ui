/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
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
package org.eclipse.ui.internal;

import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPart;

/**
 * Provides debug view selection management/notification for a debug view in a
 * specific workbench page. This selection provider shields clients from a debug
 * view opening and closing, and still provides selection
 * notification/information even when the debug view is not the active part.
 */
public class PagePartSelectionTracker {
	/**
	 * Returns the id for the given part, taking into account multi-view instances
	 * which may have a secondary id.
	 *
	 * @since 3.0
	 */
	static String getPartId(IWorkbenchPart part) {
		String id = part.getSite().getId();
		if (part instanceof IViewPart) {
			String secondaryId = ((IViewPart) part).getViewSite().getSecondaryId();
			if (secondaryId != null) {
				id = id + ':' + secondaryId;
			}
		}
		return id;
	}
}
