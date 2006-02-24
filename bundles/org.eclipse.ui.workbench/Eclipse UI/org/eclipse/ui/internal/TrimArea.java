/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal;

import java.util.List;

import org.eclipse.swt.graphics.Rectangle;

/**
 * This class represents a logical partition in the overall trim layout. The
 * <code>TrimLayoutData</code> will specify the id of the area to be used for
 * a particular trim element and the <code>WorkbenchLayout</code> will filter
 * each trim control into the appropriate area based on the id.
 * <p>
 * This is a utility class used to support the <code>WorkbenchLayout</code> so
 * the fields are all scoped to the package level to provide the layout full
 * access to the structure.
 * </p>
 * <p>
 * NOTE: This class is a part of a 'work in progress' and should not be used
 * without consulting the Platform UI group. No guarantees are made as to the
 * stability of the API (except that the javadoc will get better...;-).
 * </p>
 * <p>
 * 
 * @see WorkbenchLayout
 * @see TrimLayoutData
 *      </p>
 * 
 * @since 3.2
 * 
 */
public class TrimArea {

	/**
	 * The id of this area. Trim controls whose <code>TrimLayoutData</code>
	 * specifies an id that matches this area's id are slotted into this area by
	 * the workbench layout.
	 */
	String areaId;

	/**
	 * This
	 */
	int orientation;

	int defaultMinor;

	// 'Cache' variables
	boolean cacheOK;

	Rectangle areaBounds;

	List trimContents;

	List trimLines;

	public TrimArea(String id, int orientation, int defaultMinor) {
		this.areaId = id;
		this.orientation = orientation;
		this.defaultMinor = defaultMinor;

		areaBounds = new Rectangle(0, 0, 0, 0);

		// First use will fill the cache
		cacheOK = false;
	}
}
