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

import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.CompoundContributionItem;

/**
 * MarkersContribution is the abstract superclass of the compound contribution
 * items used in the Markers views.
 *
 * @since 3.4
 *
 */
abstract class MarkersContribution extends CompoundContributionItem {

	/**
	 * Create a new instance of the receiver.
	 */
	public MarkersContribution() {
		super();
	}

	/**
	 * Create a new instance of the receiver.
	 *
	 * @param id
	 */
	public MarkersContribution(String id) {
		super(id);
	}

	/**
	 * Get the view this contribution is working on.
	 *
	 * @return ExtendedMarkersView or <code>null</code> if the active view isn't a marker view
	 */
	ExtendedMarkersView getView() {
		IWorkbenchWindow active = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow();
		if (active == null)
			return null;
		IWorkbenchPage page = active.getActivePage();
		if (page == null)
			return null;
		IWorkbenchPart part = page.getActivePart();
		if (!(part instanceof ExtendedMarkersView))
			return null;

		return (ExtendedMarkersView) part;
	}

}
