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

package org.eclipse.e4.workbench.modeling;

import org.eclipse.e4.core.services.context.IEclipseContext;
import org.eclipse.e4.ui.model.application.MElementContainer;
import org.eclipse.e4.ui.model.application.MPlaceholder;
import org.eclipse.e4.ui.model.application.MUIElement;

/**
 *
 */
public interface EModelService {
	/**
	 * Find an element within a part of the model hierarchy.
	 * 
	 * @param id
	 *            The id to search for
	 * @param searchRoot
	 *            The element to search. If the element is a container then its children are also
	 *            searched (recursively).
	 * @return The element whose id matches the parameter
	 */
	public MUIElement find(String id, MUIElement searchRoot);

	/**
	 * Locate the context that is closest to the given element in the parent hierarchy. It does not
	 * include the context of the supplied element (should it have one).
	 * 
	 * @param element
	 *            the element to locate parent context for
	 * @return the containing context for this element
	 */
	public IEclipseContext getContainingContext(MUIElement element);

	/**
	 * Move the element to a new location. The element will be placed at the end of the new parent's
	 * list of children.
	 * 
	 * @param element
	 *            The element to move
	 * @param newParent
	 *            The new parent for the element.
	 */
	public void move(MUIElement element, MElementContainer<MUIElement> newParent);

	/**
	 * Move the element to a new location. The element will be placed at the end of the new parent's
	 * list of children. If 'leavePlaceholder is true then an instance of MPlaceholder will be
	 * inserted into the model at the element's original location.
	 * 
	 * @param element
	 *            The element to move
	 * @param newParent
	 *            The new parent for the element.
	 * @param leavePlaceholder
	 *            true if a placeholder for the element should be added
	 */
	public void move(MUIElement element, MElementContainer<MUIElement> newParent,
			boolean leavePlaceholder);

	/**
	 * Move the element to a new location. The element will be placed at the specified index in the
	 * new parent's list of children.
	 * 
	 * @param element
	 *            The element to move
	 * @param newParent
	 *            The new parent for the element.
	 * @param index
	 *            The index to insert the element at; -1 means at the end
	 */
	public void move(MUIElement element, MElementContainer<MUIElement> newParent, int index);

	/**
	 * Move the element to a new location. The element will be placed at the end of the new parent's
	 * list of children.
	 * 
	 * @param element
	 *            The element to move
	 * @param newParent
	 *            The new parent for the element.
	 * @param index
	 *            The index to insert the element at; -1 means at the end
	 * @param leavePlaceholder
	 *            true if a placeholder for the element should be added
	 */
	public void move(MUIElement element, MElementContainer<MUIElement> newParent, int index,
			boolean leavePlaceholder);

	/**
	 * Swaps the element and one of its place holders. This swap maintains the display information;
	 * for example if the original element was 'toBeRendered' == true and the placeholder was
	 * 'toBeRendered' == false then after the swap the element's value would be false and the
	 * placeholder's true.
	 * 
	 * The given place holder's 'ref' attribute <b>must</b> be the element.
	 * 
	 * @param element
	 *            The element to swap
	 * @param placeholder
	 *            The placeholder to swap it with
	 */
	public void swap(MPlaceholder placeholder);
}
