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
package org.eclipse.ui.views.markers.internal;

import org.eclipse.core.resources.IMarker;

public class ProblemMarker extends ConcreteMarker {

	private int severity;

	public ProblemMarker(IMarker toCopy) {
		super(toCopy);

	}

	@Override
	public void refresh() {
		super.refresh();
		severity = getMarker().getAttribute(IMarker.SEVERITY, -1);
	}

	public int getSeverity() {
		return severity;
	}
}
