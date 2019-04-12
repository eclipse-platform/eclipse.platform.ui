/*******************************************************************************
 * Copyright (c) 2004, 2015 IBM Corporation and others.
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

/**
 * @since 3.1
 */
public class DirtyPerspectiveMarker {
	/**
	 * @param id
	 */
	public DirtyPerspectiveMarker(String id) {
		perspectiveId = id;
	}

	public String perspectiveId;

	@Override
	public int hashCode() {
		return perspectiveId.hashCode();
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof DirtyPerspectiveMarker) {
			return perspectiveId.equals(((DirtyPerspectiveMarker) o).perspectiveId);
		}
		return false;
	}
}
