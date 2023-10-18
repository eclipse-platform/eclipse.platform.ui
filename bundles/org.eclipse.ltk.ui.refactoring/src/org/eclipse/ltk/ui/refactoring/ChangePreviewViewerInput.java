/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
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
package org.eclipse.ltk.ui.refactoring;

import org.eclipse.core.runtime.Assert;

import org.eclipse.ltk.core.refactoring.Change;

/**
 * Instances of this class represent the input for an {@link IChangePreviewViewer}.
 * The input object manages the change object the viewer is associated with.
 * <p>
 * This class is not intended to be extended outside the refactoring framework.
 * </p>
 *
 * @since 3.0
 *
 * @noextend This class is not intended to be subclassed by clients.
 */
public class ChangePreviewViewerInput {
	private Change fChange;

	/**
	 * Creates a new input object for the given change.
	 *
	 * @param change the change object
	 */
	public ChangePreviewViewerInput(Change change) {
		Assert.isNotNull(change);
		fChange= change;
	}

	/**
	 * Returns the change of this input object.
	 *
	 * @return the change of this input object
	 */
	public Change getChange() {
		return fChange;
	}
}
