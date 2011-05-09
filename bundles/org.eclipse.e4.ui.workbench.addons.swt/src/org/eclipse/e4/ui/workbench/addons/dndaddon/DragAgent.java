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
import org.eclipse.e4.ui.model.application.ui.advanced.MPlaceholder;
import org.eclipse.e4.ui.model.application.ui.advanced.impl.AdvancedFactoryImpl;

/**
 *
 */
abstract class DragAgent {
	private static final String DRAG_PLACEHOLDER_ID = "Drag Placerholder";
	protected MUIElement dragElement;

	protected MElementContainer<MUIElement> originalParent;
	protected int originalIndex;
	protected DnDManager dndManager;

	/**
	 * Return the element that your agent would start to drag given the current cursor info.
	 * 
	 * @param info
	 *            Information about which model element the cursor is over
	 * 
	 * @return The element that this agent would drag or null if the agent is not appropriate for
	 *         the given info
	 */
	public abstract MUIElement getElementToDrag(DnDInfo info);

	/**
	 * 
	 */
	public DragAgent(DnDManager manager) {
		dndManager = manager;
	}

	/**
	 * @return the dragElement
	 */
	public MUIElement getDragElement() {
		return dragElement;
	}

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
	public boolean canDrag(DnDInfo info) {
		return getElementToDrag(info) != null;
	}

	/**
	 * Start a drag operation on the given element.
	 * 
	 * @param element
	 *            The element to drag
	 */
	public void dragStart(MUIElement element, DnDInfo info) {
		// Cache the element's current location in the model.
		dragElement = element;
		originalParent = element.getParent();
		if (originalParent != null) {
			originalIndex = element.getParent().getChildren().indexOf(element);

			// If there's only one child, add a placeholder to prevent the stack
			// from being 'garbage collected'
			if (originalParent.getChildren().size() == 1
					&& dndManager.getFeedbackStyle() == DnDManager.HOSTED) {
				MPlaceholder dragPH = AdvancedFactoryImpl.eINSTANCE.createPlaceholder();
				dragPH.setElementId(DRAG_PLACEHOLDER_ID);
				dragPH.setToBeRendered(false);
				originalParent.getChildren().add(dragPH);
			}
		}
	}

	/**
	 * Cancel the drag operation. The default implementation will return the dragElement to its
	 * original location in the model.
	 */
	public void cancelDrag() {
		// if (dragElement.getParent() != null)
		// dragElement.getParent().getChildren().remove(dragElement);
		// originalParent.getChildren().add(originalIndex, dragElement);
		// dndManager.getModelService().bringToTop(dragElement);
	}

	/**
	 * Restore the DragAgent to a state where it will be ready to start a new drag
	 */
	public void dragFinished() {
		if (originalParent != null && dndManager.getFeedbackStyle() == DnDManager.HOSTED) {
			MUIElement dragPH = dndManager.getModelService().find(DRAG_PLACEHOLDER_ID,
					originalParent);
			if (dragPH != null)
				originalParent.getChildren().remove(dragPH);
		}

		dragElement = null;
		originalIndex = -1;
		originalParent = null;
	}
}
