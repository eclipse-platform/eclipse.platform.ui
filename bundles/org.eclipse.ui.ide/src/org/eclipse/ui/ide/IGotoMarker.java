/*******************************************************************************
 * Copyright (c) 2003, 2015 IBM Corporation and others.
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
package org.eclipse.ui.ide;

import org.eclipse.core.resources.IMarker;

/**
 * An adapter interface for editors, which allows the editor
 * to reveal the position of a given marker.
 *
 * @since 3.0
 */
public interface IGotoMarker {
	/**
	 * Sets the cursor and selection state for an editor to
	 * reveal the position of the given marker.
	 *
	 * @param marker the marker
	 */
	public void gotoMarker(IMarker marker);
}
