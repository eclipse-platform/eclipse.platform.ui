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
package org.eclipse.ui.internal.layout;

import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Control;

/**
 * Caches the preferred sizes of an array of controls
 *
 * @since 3.0
 */
public class LayoutCache {
	private SizeCache[] caches = new SizeCache[0];

	/**
	 * Creates an empty layout cache
	 */
	public LayoutCache() {
	}

	/**
	 * Creates a cache for the given array of controls
	 */
	public LayoutCache(Control[] controls) {
		rebuildCache(controls);
	}

	/**
	 * Returns the size cache for the given control
	 */
	public SizeCache getCache(int idx) {
		return caches[idx];
	}

	/**
	 * Sets the controls that are being cached here. If these are the same controls
	 * that were used last time, this method does nothing. Otherwise, the cache is
	 * flushed and a new cache is created for the new controls.
	 */
	public void setControls(Control[] controls) {
		// If the number of controls has changed, discard the entire cache
		if (controls.length != caches.length) {
			rebuildCache(controls);
			return;
		}

		for (int idx = 0; idx < controls.length; idx++) {
			caches[idx].setControl(controls[idx]);
		}
	}

	/**
	 * Creates a new size cache for the given set of controls, discarding any
	 * existing cache.
	 *
	 * @param controls the controls whose size is being cached
	 */
	private void rebuildCache(Control[] controls) {
		SizeCache[] newCache = new SizeCache[controls.length];

		for (int idx = 0; idx < controls.length; idx++) {
			// Try to reuse existing caches if possible
			if (idx < caches.length) {
				newCache[idx] = caches[idx];
				newCache[idx].setControl(controls[idx]);
			} else {
				newCache[idx] = new SizeCache(controls[idx]);
			}
		}

		caches = newCache;
	}

	/**
	 * Computes the preferred size of the nth control
	 *
	 * @param controlIndex index of the control whose size will be computed
	 * @param widthHint    width of the control (or SWT.DEFAULT if unknown)
	 * @param heightHint   height of the control (or SWT.DEFAULT if unknown)
	 * @return the preferred size of the control
	 */
	public Point computeSize(int controlIndex, int widthHint, int heightHint) {
		return caches[controlIndex].computeSize(widthHint, heightHint);
	}

	/**
	 * Flushes the cache for the given control. This should be called if exactly one
	 * of the controls has changed but the remaining controls remain unmodified
	 */
	public void flush(int controlIndex) {
		caches[controlIndex].flush();
	}

	/**
	 * Flushes the cache.
	 */
	public void flush() {
		for (SizeCache cache : caches) {
			cache.flush();
		}
	}
}
