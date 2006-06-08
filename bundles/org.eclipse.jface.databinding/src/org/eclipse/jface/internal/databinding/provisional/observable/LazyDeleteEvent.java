/*******************************************************************************
 * Copyright (c) 2006 The Pampered Chef and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     The Pampered Chef - initial API and implementation
 ******************************************************************************/

package org.eclipse.jface.internal.databinding.provisional.observable;

/**
 * Represents the data required to delete a new object from a lazy list.
 * 
 * @since 3.3
 */
public class LazyDeleteEvent {
	/**
	 * The position of the object that must be deleted.
	 */
	public final int position;

	/**
	 * Construct a LazyDeleteEvent.
	 * 
	 * @param position The 0-based position of the object that must be deleted.
	 */
	public LazyDeleteEvent(final int position) {
		this.position = position;
	}
}
