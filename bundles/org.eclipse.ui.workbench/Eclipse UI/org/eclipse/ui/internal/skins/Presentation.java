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

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;


/**
 * This represents the common interface to PartPresentation and StackPresentation.
 * 
 * Clients should implement PartPresentation or StackPresentation, and should not
 * implement this interface directly.
 * 
 * @since 3.0
 */
abstract class Presentation {
	/**
	 * Sets the bounding rectangle for this presentation. 
	 * 
	 * @param bounds new bounding rectangle (not null)
	 */
	public abstract void setBounds(Rectangle bounds);
	
	/**
	 * Returns the minimum size for this stack. The stack is prevented
	 * from being resized smaller than this amount, and this is used as
	 * the default size for the stack when it is minimized. Typically,
	 * this is the amount of space required to fit the minimize, close,
	 * and maximize buttons and one tab. 
	 * 
	 * @return the minimum size for this stack (not null)
	 */
	public abstract Point computeMinimumSize();
	
	/**
	 * Disposes all SWT resources being used by the stack. This is the
	 * last method that will be invoked on the stack. 
	 */
	public abstract void dispose();

	/**
	 * This is invoked to cause the presentation to become active. This
	 * is typically called as a result of focus moving to one of the parts
	 * in this presentation, so it should not change the part's focus.
	 * 
	 * @param isActive
	 */
	public abstract void setActive(boolean isActive);
	
	/**
	 * This causes the presentation to become visible or invisible. 
	 * When a presentation is invisible, it must not respond to user
	 * input or modify its parts. For example, a presentations will 
	 * be made invisible if it belongs to a perspective and the user
	 * switches to another perspective.
	 * 
	 * @since 3.0
	 */
	public abstract void setVisible(boolean isVisible);
	
	/**
	 * Returns the system menu manager. The workbench will insert global
	 * action contributions into this menu manager. 
	 * 
	 * @return the menu manager that this presentation uses to display
	 * system actions. Not null.
	 */
	public abstract IMenuManager getSystemMenuManager();
	
}
