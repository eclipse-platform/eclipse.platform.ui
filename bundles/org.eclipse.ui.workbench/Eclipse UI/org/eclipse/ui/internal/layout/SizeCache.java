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
package org.eclipse.ui.internal.layout;

import java.util.List;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Sash;
import org.eclipse.swt.widgets.Scale;
import org.eclipse.swt.widgets.Slider;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;

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
	private boolean independentDimensions = false;

	public SizeCache() {
		this(null);
	}
	
	/**
	 * Creates a cache for size computations on the given control
	 * 
	 * @param control the control for which sizes will be calculated, 
	 * or null to always return (0,0) 
	 */
	public SizeCache(Control control) {
		this.control = control;		
		independentDimensions = independentLengthAndWidth(control); 
	}
	
	/**
	 * Sets the control whose size is being cached. Does nothing (will not
	 * even flush the cache) if this is the same control as last time. 
	 * 
	 * @param newControl the control whose size is being cached, or null to always return (0,0)
	 */
	public void setControl(Control newControl) {
		if (newControl != control) {
			control = newControl;
			independentDimensions = independentLengthAndWidth(control);
			flush();
		}
	}
	
	/**
	 * Returns the control whose size is being cached
	 * 
	 * @return the control whose size is being cached, or null if this cache always returns (0,0)
	 */
	public Control getControl() {
		return control;
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
		if (control == null) {
			return new Point(0,0);
		}
		
		// No hints given -- find the preferred size
		if (widthHint == SWT.DEFAULT && heightHint == SWT.DEFAULT) {
			if (preferredSize == null) {
				preferredSize = computeSize(control, widthHint, heightHint);
			}
			
			return preferredSize;
		}
		
		// Computing a height
		if (heightHint == SWT.DEFAULT) {
			// If we know the control's preferred size
			if (preferredSize != null) {
				// If the given width is the preferred width, then return the preferred size
				if (widthHint == preferredSize.x) {
					return preferredSize;
				}
				
				// If the preferred width is independent of the height hint, we can compute
				// the result trivially from the preferred size
				if (independentDimensions) {
					return new Point(widthHint, preferredSize.y);
				}
			}

			// If we have a cached height measurement
			if (cachedHeight != null) {
				// If this was measured with the same width hint
				if (cachedHeight.x == widthHint) {
					return cachedHeight;
				}
				
				// Else, if the height is independent of the width hint, we can compute the
				// size trivially
				if (independentDimensions) {
					return new Point(widthHint, cachedHeight.y);
				}
			}
			
			// Compute the control's height and cache the result
			cachedHeight = computeSize(control, widthHint, heightHint);
			
			return cachedHeight;
		}
		
		// Computing a width
		if (widthHint == SWT.DEFAULT) {
			// If we know the control's preferred size
			if (preferredSize != null) {
				// If the given height is the preferred height, then return the preferred size
				if (heightHint == preferredSize.y) {
					return preferredSize;
				}
				
				// If the preferred height is independent of the width hint, return the preferred
				// width and the height hint
				if (independentDimensions) {
					return new Point(preferredSize.x, heightHint);
				}
			}

			// If we have a cached width measurement
			if (cachedWidth != null) {
				// If this was measured with the same height hint
				if (cachedWidth.y == heightHint) {
					return cachedWidth;
				}
				
				// Else, if the width is independent of the height hint, we can compute the
				// size trivially
				if (independentDimensions) {
					return new Point(cachedWidth.x, heightHint);
				}
			}
			
			cachedWidth = computeSize(control, widthHint, heightHint);
			
			return cachedWidth;
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
	
	/**
	 * Returns true if the preferred length of the given control is 
	 * independent of the width and visa-versa. If this returns true,
	 * then changing the widthHint argument to control.computeSize will
	 * never change the resulting height and changing the heightHint
	 * will never change the resulting width. Returns false if unknown.
	 * <p>
	 * This information can be used to improve caching. Incorrectly returning
	 * a value of false may decrease performance, but incorrectly returning 
	 * a value of true will generate incorrect layouts... so always return
	 * false if unsure.
	 * </p>
	 * 
	 * @param control
	 * @return
	 */
	static boolean independentLengthAndWidth(Control control) {
		if (control == null) {
			return true;
		}
		
		if (control instanceof Button
				|| control instanceof ProgressBar
				|| control instanceof Sash
				|| control instanceof Scale
				|| control instanceof Slider
				|| control instanceof List
				|| control instanceof Combo
				|| control instanceof Tree) {
			return true;
		}
		
		if (control instanceof Label || control instanceof Text) {
			return (control.getStyle() & SWT.WRAP) == 0;
		}
		
		// Unless we're certain that the control has this property, we should
		// return false.
		
		return false;
	}
	
}
