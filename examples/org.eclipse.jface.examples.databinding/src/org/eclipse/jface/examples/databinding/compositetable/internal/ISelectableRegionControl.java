/*
 * Copyright (C) 2005 David Orme <djo@coconut-palm-software.com>
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     David Orme     - Initial API and implementation
 */
package org.eclipse.jface.examples.databinding.compositetable.internal;

import org.eclipse.swt.SWTException;

/**
 * A duck interface for controls that can select a specific range according to SWT 
 * range selection semantics.
 * 
 * @author djo
 */
public interface ISelectableRegionControl {
	/**
	 * Sets the selection to the range specified
	 * by the given start and end indices.
	 * <p>
	 * Indexing is zero based.  The range of
	 * a selection is from 0..N where N is
	 * the number of characters in the widget.
	 * </p><p>
	 * Text selections are specified in terms of
	 * caret positions.  In a text widget that
	 * contains N characters, there are N+1 caret
	 * positions, ranging from 0..N.  This differs
	 * from other functions that address character
	 * position such as getText () that use the
	 * usual array indexing rules.
	 * </p>
	 *
	 * @param start the start of the range
	 * @param end the end of the range
	 *
	 * @exception SWTException <ul>
	 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
	 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
	 * </ul>
	 */
	public void setSelection (int start, int end);
}
