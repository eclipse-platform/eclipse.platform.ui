/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.presentations;

import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Control;

/**
 * Caches the preferred sizes of an array of controls
 * 
 * @since 3.0
 */
public class LayoutCache {
	private SizeCache[] caches;
	
	/**
	 * Creates a cache for the given array of controls
	 * 
	 * @param controls
	 */
	public LayoutCache(Control[] controls) {
		caches = new SizeCache[controls.length];
		for (int idx = 0; idx < controls.length; idx++) {
			caches[idx] = new SizeCache(controls[idx]);
		}
	}
	
	/**
	 * Computes the preferred size of the nth control
	 * 
	 * @param controlIndex index of the control whose size will be computed
	 * @param widthHint width of the control (or SWT.DEFAULT if unknown)
	 * @param heightHint height of the control (or SWT.DEFAULT if unknown)
	 * @return the preferred size of the control
	 */
	public Point computeSize(int controlIndex, int widthHint, int heightHint) {
		return caches[controlIndex].computeSize(widthHint, heightHint);
	}
	
	/**
	 * Flushes the cache
	 */
	public void flush() {
		for (int idx = 0; idx < caches.length; idx++) {
			caches[idx].flush();
		}
	}
}
