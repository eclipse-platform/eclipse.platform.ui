/*******************************************************************************
 * Copyright (c) 2010, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.e4.ui.workbench.addons.dndaddon;

import java.util.List;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.advanced.impl.AdvancedFactoryImpl;
import org.eclipse.e4.ui.model.application.ui.basic.MPartStack;
import org.eclipse.e4.ui.model.application.ui.basic.MStackElement;
import org.eclipse.e4.ui.model.application.ui.basic.MTrimElement;
import org.eclipse.e4.ui.model.application.ui.basic.impl.BasicFactoryImpl;
import org.eclipse.e4.ui.model.application.ui.menu.impl.MenuFactoryImpl;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;

/**
 *
 */
abstract class DragAgent {
	private static final String DRAG_PLACEHOLDER_ID = "Drag Placerholder";
	protected MUIElement dragElement;

	protected DnDManager dndManager;
	private MUIElement dragPH = null;
	protected DropAgent dropAgent = null;

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
	 * @return true iff there is an element to drag
	 */
	public boolean canDrag(DnDInfo info) {
		dragElement = getElementToDrag(info);
		return dragElement != null;
	}

	/**
	 * Start a drag operation on the given element.
	 *
	 * @param element
	 *            The element to drag
	 */
	public void dragStart(DnDInfo info) {
		// cache a placeholder where the element started (NOTE: this also prevents the parent from
		// being auto-removed by going 'empty'
		if (dragElement.getParent() != null) {
			if (dragElement instanceof MStackElement)
				dragPH = AdvancedFactoryImpl.eINSTANCE.createPlaceholder();
			else if (dragElement instanceof MPartStack)
				dragPH = BasicFactoryImpl.eINSTANCE.createPartSashContainer();
			else if (dragElement instanceof MTrimElement)
				dragPH = MenuFactoryImpl.eINSTANCE.createToolControl();

			dragPH.setElementId(DRAG_PLACEHOLDER_ID);
			dragPH.setToBeRendered(false);

			List<MUIElement> kids = dragElement.getParent().getChildren();
			kids.add(kids.indexOf(dragElement), dragPH);
		}

		dropAgent = dndManager.getDropAgent(dragElement, info);
		if (dropAgent != null)
			dropAgent.dragEnter(dragElement, info);
	}

	public void track(DnDInfo info) {
		DropAgent curAgent = dropAgent;

		// Re-use the same dropAgent until it returns 'false' from track
		if (dropAgent != null)
			dropAgent = dropAgent.track(dragElement, info) ? dropAgent : null;

		// If we don't have a drop agent currently try to get one
		if (dropAgent == null) {
			if (curAgent != null)
				curAgent.dragLeave(dragElement, info);

			dropAgent = dndManager.getDropAgent(dragElement, info);

			if (dropAgent != null)
				dropAgent.dragEnter(dragElement, info);
			else {
				dndManager.setCursor(Display.getCurrent().getSystemCursor(SWT.CURSOR_NO));
			}
		}
	}

	/**
	 * Cancel the drag operation. The default implementation will return the dragElement to its
	 * original location in the model.
	 */
	public void cancelDrag() {
		if (dragPH == null)
			return;

		// if the dragElement is *not* directly after the placeholder we have to return it there
		List<MUIElement> phParentsKids = dragPH.getParent().getChildren();
		if (phParentsKids.indexOf(dragElement) != phParentsKids.indexOf(dragPH) + 1) {
			dragElement.setToBeRendered(false);
			if (dragElement.getParent() != null)
				dragElement.getParent().getChildren().remove(dragElement);
			phParentsKids.add(phParentsKids.indexOf(dragPH) + 1, dragElement);
			dragElement.setVisible(true);
			dragElement.setToBeRendered(true);
		}
	}

	/**
	 * Restore the DragAgent to a state where it will be ready to start a new drag
	 *
	 * @param performDrop
	 *            determines if a drop operation should be performed if possible
	 */
	public void dragFinished(boolean performDrop, DnDInfo info) {
		boolean isNoDrop = dndManager.getDragShell().getCursor() == Display.getCurrent()
				.getSystemCursor(SWT.CURSOR_NO);
		if (performDrop && dropAgent != null && !isNoDrop) {
			dropAgent.drop(dragElement, info);
		} else {
			cancelDrag();
		}

		if (dropAgent != null)
			dropAgent.dragLeave(dragElement, info);

		if (dragPH == null)
			return;

		if (dragPH != null) {
			dragPH.getParent().getChildren().remove(dragPH);
			dragPH = null;
		}

		dragElement = null;
	}

	/**
	 * This agent is being disposed
	 */
	public void dispose() {
	}
}
