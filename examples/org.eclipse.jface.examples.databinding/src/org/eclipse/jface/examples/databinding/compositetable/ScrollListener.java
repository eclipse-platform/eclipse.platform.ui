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

package org.eclipse.jface.examples.databinding.compositetable;

/**
 * An "interface" for objects that need to listen to scrolling events on a
 * CompositeTable control.
 * 
 * @since 3.2
 */
public abstract class ScrollListener {
	/**
	 * Method tableScrolled.  Called after the CompositeTable has scrolled the
	 * visible range.
	 * @param scrollEvent TODO
	 */
	public abstract void tableScrolled(ScrollEvent scrollEvent);
}
