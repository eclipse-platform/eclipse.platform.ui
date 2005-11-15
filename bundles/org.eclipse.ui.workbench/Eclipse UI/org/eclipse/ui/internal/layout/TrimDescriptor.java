/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal.layout;

import org.eclipse.ui.internal.IWindowTrim;

/**
 * Manages the internal pieces of trim for each trim area.
 * @since 3.2
 */
public class TrimDescriptor {

	private IWindowTrim fTrim;
	private SizeCache fCache;
	private SizeCache fDockingHandle = null;
	private int fAreaId;

	/**
	 * Create a trim descriptor for the trim manager.
	 * @param trim
	 * @param cache
	 * @param areaId
	 */
	public TrimDescriptor(IWindowTrim trim, SizeCache cache, int areaId) {
		fTrim = trim;
		fCache = cache;
		fAreaId = areaId;
	}

	/**
	 * @return Returns the fCache.
	 */
	public SizeCache getCache() {
		return fCache;
	}

	/**
	 * @return Returns the fTrim.
	 */
	public IWindowTrim getTrim() {
		return fTrim;
	}
	
	/**
	 * @return return the docking handle
	 */
	public SizeCache getDockingCache() {
		return fDockingHandle;
	}
	
	/**
	 * The trim ID.
	 * @return the trim ID. This should not be <code>null</code>.
	 */
	public String getId() {
		return fTrim.getId();
	}

	/**
	 * Returns whether the control for this trim is visible.
	 * @return <code>true</code> if the control is visible.
	 */
	public boolean isVisible() {
		if (!fTrim.getControl().isDisposed()) {
			return fTrim.getControl().isVisible();
		}
		return false;
	}

	/**
	 * Add the docking handle
	 * @param cache the sizecache for the docking control
	 */
	public void setDockingCache(SizeCache cache) {
		fDockingHandle = cache;
	}

	/**
	 * The area ID this descriptor belongs to
	 * @return the ID
	 */
	public int getAreaId() {
		return fAreaId;
	}

	/**
	 * Flush any contained size caches.
	 */
	public void flush() {
		fCache.flush();
		if (fDockingHandle!=null) {
			fDockingHandle.flush();
		}
	}
}
