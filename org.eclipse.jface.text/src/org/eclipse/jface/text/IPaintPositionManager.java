/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.jface.text;


import org.eclipse.jface.text.Position;

/**
 * Manager that manages and updates positions used by <code>IPainter</code>
 * objects.
 */
public interface IPaintPositionManager {
	
	/**
	 * Starts managing the given position until
	 * <code>unmanagePosition</code> is called.
	 * 
	 * @param position
	 */
	void managePosition(Position position);
	
	/**
	 * Stops managing the given position. If the position is not managed 
	 * by this managed, this call has no effect.
	 * 
	 * @param position
	 */
	void unmanagePosition(Position position);
}
