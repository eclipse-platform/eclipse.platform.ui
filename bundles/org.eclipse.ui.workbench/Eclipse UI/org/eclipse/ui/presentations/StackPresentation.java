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
package org.eclipse.ui.presentations;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.internal.misc.Assert;


/**
 * This represents an object that can supply trim around a IPresentablePart. 
 * Clients can implement subclasses to provide the behavior for editor workbooks,
 * view folders, fast views, and detached windows.
 * 
 * @since 3.0
 */
public abstract class StackPresentation {

    /**
     * The presentation site.
     */
	private IStackPresentationSite site;
	
	/**
	 * Constructs a new stack presentation with the given site.
	 * 
	 * @param stackSite the stack site
	 */
	protected StackPresentation(IStackPresentationSite stackSite) {
	    Assert.isNotNull(stackSite);
	    site = stackSite;
	}

	/**
	 * Returns the presentation site (not null).
	 */
	protected IStackPresentationSite getSite() {
	    return site;
	}
	
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
	 * This is invoked notify the presentation that one of its parts
	 * has gained or lost keyboard focus. It should not change the part's focus.
	 * Only one presentation may be active at a time.
	 * <p>
	 * This method relates specifically to keyboard focus, and should
	 * not be confused with the notion of an "active" editor (which would
	 * be the editor that most recently had focus if no editor currently
	 * has focus). 
	 * </p> 
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
	
	/**
	 * Sets the state of the presentation. That is, notifies the presentation
	 * that is has been minimized, maximized, or restored. Note that this method
	 * is the only way that a presentation is allowed to change its state.
	 * <p>
	 * If a presentation wishes to minimize itself, it must call setState
	 * on its associated IPresentationSite. If the site chooses to respond
	 * to the state change, it will call this method at the correct time.
	 * The presentation should not call this method directly. 
	 * </p>
	 * 
	 * @param state one of the IPresentationSite.STATE_* constants.
	 */
	public abstract void setState(int state);
	
	/**
	 * Returns the control for this presentation
	 * 
	 * @return the control for this presentation (not null)
	 */
	public abstract Control getControl();	
	
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
	
	/**
	 * This method is invoked whenever a part is dragged over the stack's control.
	 * It returns a StackDropResult if and only if the part may be dropped in this
	 * location.
	 *
	 * @param currentControl the control being dragged over
	 * @param location cursor location (display coordinates)
	 * @return a StackDropResult or null if the presentation does not have
	 * a drop target in this location.
	 */
	public abstract StackDropResult dragOver(Control currentControl, Point location);
	
	/**
	 * Instructs the presentation to display the system menu
	 *
	 */
	public abstract void showSystemMenu();

	/**
	 * 
	 */
	public abstract void showPaneMenu();
}
