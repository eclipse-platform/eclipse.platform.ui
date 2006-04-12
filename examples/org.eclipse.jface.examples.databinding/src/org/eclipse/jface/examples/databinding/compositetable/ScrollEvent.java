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
 * An event that is fired whenever the user scrolls a CompositeTable control.
 * 
 * @since 3.2
 */
public class ScrollEvent {
	/**
	 * Scroll direction == forward relative to the user's viewport.
	 */
	public static final int FORWARD = 1;
	
	/**
	 * Scroll direction == none relative to the user's viewport.
	 */
	public static final int NONE = 0;
	
	/**
	 * Scroll direction == backward relative to the user's viewport.
	 */
	public static final int BACKWARD = -1;
	
	/**
	 * The direction that the user scrolled relative to the viewport.  One
	 * of: {FORWARD, BACKWARD}.
	 */
	public final int userScrollDirection;
	
	/**
	 * The CompositeTable that sent this event.
	 */
	public final CompositeTable sender;

	/**
	 * @param userScrollDirection
	 * @param parent
	 */
	public ScrollEvent(int userScrollDirection, CompositeTable parent) {
		this.userScrollDirection = userScrollDirection;
		this.sender = parent;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		if (userScrollDirection == FORWARD) {
			return "FORWARD Scroll";
		}
		if (userScrollDirection == BACKWARD) {
			return "BACKWARD Scroll";
		}
		return "no scroll";
	}

}
