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
package org.eclipse.ui.internal.skins;


/**
 * @since 3.0
 */
public abstract class StackPresentation extends Presentation {
		
	/**
	 * Adds the given part to the stack.
	 * 
	 * @param newPart the new part to add (not null)
	 * @param position the position to insert the part. The new part will
	 * occupy the tab location currently occupied by the "position" part, and the
	 * "position" part will be moved to a new location. May be null if the caller
	 * does not care where the newly added part is inserted into the tab folder.
	 * @param isFixed true iff the part is considered "fixed" in this presentation.
	 * That is, the part cannot be dragged or closed.
	 */
	public abstract void addPart(IPresentablePart newPart, IPresentablePart position);
	
	/**
	 * Removes the given part from the stack.
	 * 
	 * @param oldPart the part to remove (not null)
	 */
	public abstract void removePart(IPresentablePart oldPart);
	
	/**
	 * Brings the specified part to the foreground. This should not affect
	 * the current focus.
	 * 
	 * @param toSelect the new active part (not null)
	 */
	public abstract void selectPart(IPresentablePart toSelect);
}
