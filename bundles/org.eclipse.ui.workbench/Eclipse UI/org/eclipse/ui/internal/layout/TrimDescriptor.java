/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.internal.layout;


/**
 * Manages the internal piece of trim for each trim area.
 * 
 * @since 3.2
 */
public class TrimDescriptor {

	/**
	 * The window trim object.
	 */
	private IWindowTrim fTrim;

	/**
	 * The cache for the window trim object's control.
	 */
	private SizeCache fCache;

	/**
	 * The cache for the common drag affordance, if available.
	 */
	private SizeCache fDockingHandle = null;

	/**
	 * The current area that we belong to.
	 * 
	 * @see TrimLayout#getAreaIds()
	 */
	private int fAreaId;

	/**
	 * Create a trim descriptor for the trim manager.
	 * 
	 * @param trim
	 *            the window trim
	 * @param areaId
	 *            the trim area we belong to.
	 */
	public TrimDescriptor(IWindowTrim trim, int areaId) {
		fTrim = trim;
		fAreaId = areaId;
	}

	/**
	 * @return Returns the fCache.
	 */
	public SizeCache getCache() {
		return fCache;
	}

	/**
	 * Set the trim cache. Because of the requirements of possibly changing
	 * orientation when docking on a different side, the same IWindowTrim
	 * sometimes needs to have it's control SizeCache replaced.
	 * 
	 * @param c
	 *            cache.
	 */
	public void setCache(SizeCache c) {
		fCache = c;
	}

	/**
	 * @return Returns the fTrim.
	 */
	public IWindowTrim getTrim() {
		return fTrim;
	}

	/**
	 * Return the cache for the common drag affordance.
	 * 
	 * @return return the docking handle cache
	 */
	public SizeCache getDockingCache() {
		return fDockingHandle;
	}

	/**
	 * The trim ID.
	 * 
	 * @return the trim ID. This should not be <code>null</code>.
	 */
	public String getId() {
		return fTrim.getId();
	}

	/**
	 * Returns whether the control for this trim is visible.
	 * 
	 * @return <code>true</code> if the control is visible.
	 */
	public boolean isVisible() {
		if (!fTrim.getControl().isDisposed()) {
			return fTrim.getControl().isVisible();
		}
		return false;
	}

	/**
	 * Set the cache for the common drag affordance.
	 * 
	 * @param cache
	 *            the sizecache for the docking control
	 */
	public void setDockingCache(SizeCache cache) {
		fDockingHandle = cache;
	}

	/**
	 * The area ID this descriptor belongs to.
	 * 
	 * @return the ID
	 * @see TrimLayout#getAreaIds()
	 */
	public int getAreaId() {
		return fAreaId;
	}

	/**
	 * Set the current area this descriptor belongs to.
	 * 
	 * @param id
	 *            the area ID.
	 * @see TrimLayout#getAreaIds()
	 */
	public void setAreaId(int id) {
		fAreaId = id;
	}

	/**
	 * Flush any contained size caches.
	 */
	public void flush() {
		if (fCache != null) {
			fCache.flush();
		}
		if (fDockingHandle != null) {
			fDockingHandle.flush();
		}
	}

	/**
	 * Update the visibility of the trim controls.
	 * 
	 * @param visible
	 *            visible or not.
	 */
	public void setVisible(boolean visible) {
		if (fTrim.getControl() != null && !fTrim.getControl().isDisposed()) {
			fTrim.getControl().setVisible(visible);
		}
		if (fDockingHandle != null && fDockingHandle.getControl() != null
				&& !fDockingHandle.getControl().isDisposed()) {
			fDockingHandle.getControl().setVisible(visible);
		}
	}
}
