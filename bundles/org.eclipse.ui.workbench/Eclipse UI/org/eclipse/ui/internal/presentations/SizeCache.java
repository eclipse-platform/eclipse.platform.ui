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

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Control;

/**
 * Caches the preferred size of an SWT control
 * 
 * @since 3.0
 */
public class SizeCache {
	private Control control;
	
	private Point preferredSize;	
	private Point cachedWidth;
	private Point cachedHeight;

	/**
	 * Creates a cache for size computations on the given control
	 * 
	 * @param control the control for which 
	 */
	public SizeCache(Control control) {
		this.control = control;		
	}
	
	/**
	 * Flush the cache (should be called if the control's contents may have changed since the
	 * last query)
	 */
	public void flush() {
		preferredSize = null;
		cachedWidth = null;
		cachedHeight = null;
	}
	
	/**
	 * Computes the preferred size of the control.
	 *  
	 * @param widthHint the known width of the control (pixels) or SWT.DEFAULT if unknown
	 * @param heightHint the known height of the control (pixels) or SWT.DEFAULT if unknown
	 * @return the preferred size of the control
	 */
	public Point computeSize(int widthHint, int heightHint) {
		// No hints given -- find the preferred size
		if (widthHint == SWT.DEFAULT && heightHint == SWT.DEFAULT) {
			if (preferredSize == null) {
				preferredSize = computeSize(control, widthHint, heightHint);
			}
			
			return preferredSize;
		}
		
		// Computing a width
		if (widthHint == SWT.DEFAULT) {
			if (preferredSize != null && heightHint == preferredSize.y) {
				return preferredSize;
			}

			if (cachedWidth == null || cachedWidth.y != heightHint) {
				cachedWidth = computeSize(control, widthHint, heightHint);
			}
			
			return cachedWidth;
		}
		
		// Computing a height
		if (heightHint == SWT.DEFAULT) {
			// Check if we're asking about the preferred width
			if (preferredSize != null && widthHint == preferredSize.x) {
				return preferredSize;
			}
			
			if (cachedHeight == null || cachedHeight.x != widthHint) {
				cachedHeight = computeSize(control, widthHint, heightHint);
			}
			
			return cachedHeight;
		}
		
		return computeSize(control, widthHint, heightHint);
	}
	
	/**
	 * Compute the control's size, and ensure that non-default hints are returned verbatim
	 * (this tries to compensate for SWT's hints, which aren't really the outer width of the
	 * control).
	 * 
	 * @param control
	 * @param widthHint
	 * @param heightHint
	 * @return
	 */
	private static Point computeSize(Control control, int widthHint, int heightHint) {
		Point result = control.computeSize(widthHint, heightHint);
		
		if (widthHint != SWT.DEFAULT) {
			result.x = widthHint;
		}
		
		if (heightHint != SWT.DEFAULT) {
			result.y = heightHint;
		}
		
		return result;
	}
	
}
