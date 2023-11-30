/*******************************************************************************
 * Copyright (c) 2000, 2014 IBM Corporation and others.
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
package org.eclipse.ui.views.markers.internal;

import org.eclipse.core.resources.IMarker;

/**
 * Represents a marker visible in the Tasks view. Additional members should be added
 * to this class if new fields are added to the Tasks view. Such members should be
 * initialized in the constructor, and accessed via get methods rather than accessing
 * the IMarker instance directly. This is necessary to support sorting in a reasonable
 * time bound.
 */
public class TaskMarker extends ConcreteMarker {

	private int priority;

	private int done;

	public TaskMarker(IMarker toCopy) {
		super(toCopy);
	}

	@Override
	public void refresh() {
		super.refresh();
		priority = getMarker().getAttribute(IMarker.PRIORITY,
				IMarker.PRIORITY_NORMAL);
		done = -1;
		if (getMarker().getAttribute(IMarker.USER_EDITABLE, true)) {
			done = 0;
			if (getMarker().getAttribute(IMarker.DONE, false)) {
				done = 1;
			}
		}
	}

	public int getPriority() {
		return priority;
	}

	public int getDone() {
		return done;
	}
}
