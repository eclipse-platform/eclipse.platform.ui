/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.ui.internal;

/**
 * The layout data class used by the <code>WorkbenchLayout</code> to arrange
 * trim around the workbench edges.
 * <p>
 * NOTE: This class is a part of a 'work in progress' and should not be used
 * without consulting the Platform UI group. No guarantees are made as to the
 * stability of the API (except that the javadoc will get better...;-).
 * </p>
 * 
 * @since 3.2
 * 
 */
public class TrimLayoutData {

	// Constants
	/**
	 * The trim element will grow unbounded based on the amount of unused space
	 * in its TrimArea but will never be smaller than it's preferred size. Only
	 * one of <code>GROWABLE</code> or <code>SHRINKABLE</code> should be
	 * specified.
	 */
	public static final int GROWABLE = 0x1;

	/**
	 * The trim element will shrink to the <code>shrinkableSize</code> if
	 * needed to fit the element into the trim but will never grow larger than
	 */
	public static final int SHRINKABLE = 0x2;

	/**
	 * The trim element will grow in its 'minor' dimension (i.e. in Y if the
	 * trim is oriented horizontally) to match the size of the TrimArea that it
	 * is in.
	 */
	public static final int GRAB_EXCESS_MINOR = 0x4;

	// Fields
	public String trimId;

	public String areaId;

	public int flags;

	public ITrimAreaChangeListener listener;

	public int shrinkableSize;

	public TrimLayoutData(String trimId, String areaId, int flags,
			ITrimAreaChangeListener listener) {
		this.trimId = trimId;
		this.areaId = areaId;
		this.flags = flags;
		this.listener = listener;
	}

	public TrimLayoutData(String trimId, String areaId, int flags) {
		this(trimId, areaId, flags, null);
	}

	public TrimLayoutData(String trimId, String areaId) {
		this(trimId, areaId, 0);
	}

	public void setAreaId(String newAreaId) {
		if (listener != null) {
			listener.areaChanged(WorkbenchLayout.getOrientation(areaId),
					WorkbenchLayout.getOrientation(newAreaId));
		}

		areaId = newAreaId;
	}
}
