/*******************************************************************************
 * Copyright (c) 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.e4.ui.workbench.addons.dndaddon;

import org.eclipse.e4.ui.model.application.ui.MElementContainer;
import org.eclipse.e4.ui.model.application.ui.MUIElement;

/**
 *
 */
abstract class DragAgent {
	protected MUIElement dragElement;

	/**
	 * @return the dragElement
	 */
	public MUIElement getDragElement() {
		return dragElement;
	}

	protected MElementContainer<MUIElement> originalParent;
	protected int originalIndex;

	private DragHost dragHost = null;

	/**
	 * Return the element that your agent would start to drag given the current cursor info.
	 * 
	 * @param info
	 *            Information about which model element the cursor is over
	 * 
	 * @return The element that this agent would drag or null if the agent is not appropriate for
	 *         the given info
	 */
	public abstract MUIElement getElementToDrag(CursorInfo info);

	/**
	 * Determine if a drag can be started on the given info. This allows a subclass to restrict the
	 * ability of an agent to initiate a drag operation (i.e. in a 'fixed' perspective...).
	 * 
	 * The default implementation is to allow dragging if the agent can determine an element to
	 * drag.
	 * 
	 * @param info
	 *            Information about which model element the cursor is over
	 * 
	 * @return true iff it is OK to start a drag
	 */
	public boolean canDrag(CursorInfo info) {
		return getElementToDrag(info) != null;
	}

	/**
	 * Start a drag operation on the given element.
	 * 
	 * @param element
	 *            The element to drag
	 */
	public void dragStart(MUIElement element) {
		// Cache the element's current location in the model.
		dragElement = element;
		originalParent = element.getParent();
		if (originalParent != null)
			originalIndex = element.getParent().getChildren().indexOf(element);

		createDragFeedback();
	}

	/**
	 * Cancel the drag operation. The default implementation will return the dragElement to its
	 * original location in the model.
	 */
	public void cancelDrag() {
		removeDragFeedback();

		// MElementContainer<MUIElement> curParent = dragElement.getParent();
		// int curIndex = -1;
		// if (curParent != null)
		// curIndex = curParent.getChildren().indexOf(dragElement);
		//
		// if (curParent != originalParent || curIndex != originalIndex) {
		// if (curParent != null) {
		// curParent.getChildren().remove(dragElement);
		// }
		// originalParent.getChildren().add(originalIndex, curParent);
		// }
	}

	/**
	 * Restore the DragAgent to a state where it will be ready to start a new drag
	 */
	public void dragFinished() {
		dragElement = null;
		originalIndex = -1;
		originalParent = null;
	}

	/**
	 * Initialize the drag feedback. The default implementation will 'host' the drag element in a
	 * semi-transparent window that will track with the cursor.
	 */
	public void createDragFeedback() {
	}

	/**
	 * Remove any feedback used during the drag operation.
	 */
	public void removeDragFeedback() {
		if (dragHost != null)
			dragHost.dispose();
	}

	/**
	 * Track the drag feedback. This is called during a drag operation when the mouse moves.
	 * 
	 * @param cursorInfo
	 */
	public void trackDragFeedback(CursorInfo cursorInfo) {
		if (dragHost != null) {

		}
	}
}
