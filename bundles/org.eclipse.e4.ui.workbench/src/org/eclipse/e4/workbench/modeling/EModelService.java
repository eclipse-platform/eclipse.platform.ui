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

import java.util.List;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.ui.model.application.ui.MElementContainer;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.advanced.MPlaceholder;
import org.eclipse.e4.ui.model.application.ui.basic.MPartSashContainerElement;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;

/**
 * @since 1.0
 */
public interface EModelService {
	// Insertion constants
	public static final int ABOVE = 0;
	public static final int BELOW = 1;
	public static final int LEFT_OF = 2;
	public static final int RIGHT_OF = 3;

	/**
	 * Return a list of any elements that match the given search criteria. The search is recursive
	 * and includes the specified search root. Any of the search parameters may be specified as
	 * <code>null</code> in which case that field will always 'match'.
	 * <p>
	 * NOTE: This is a generically typed method with the List's generic type expected to be the
	 * value of the 'clazz' parameter. If the 'clazz' parameter is null then the returned list is
	 * untyped but may safely be assigned to List&lt;MUIElement&gt;.
	 * </p>
	 * 
	 * @param <T>
	 *            The generic type of the returned list
	 * @param searchRoot
	 *            The element at which to start the search. This element must be non-null and is
	 *            included in the search.
	 * @param id
	 *            The ID of the element. May be null to omit the test for this field.
	 * @param clazz
	 *            The class specifier determining the 'instanceof' type of the elements to be found.
	 *            If specified then the returned List will be generically specified as being of this
	 *            type.
	 * @param tagsToMatch
	 *            The list of tags to match. All the tags specified in this list must be defined in
	 *            the search element's tags in order to be a match.
	 * 
	 * @return The generically typed list of matching elements.
	 */
	public <T> List<T> findElements(MUIElement searchRoot, String id, Class<T> clazz,
			List<String> tagsToMatch);

	/**
	 * Returns the first element, recursively searching under the specified search root (inclusive)
	 * 
	 * @param id
	 *            The id to search for, must not be null
	 * @param searchRoot
	 *            The element at which to start the search
	 * @return The first element with a matching id or <code>null</code> if one is not found
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
	 * Ensures that the given element is visible in the UI
	 * 
	 * @param window
	 *            The containing MWindow
	 * @param element
	 *            The element to bring to the top
	 */
	public void bringToTop(MWindow window, MUIElement element);

	/**
	 * Given a containing MWindow find the MPlaceholder that is currently being used to host the
	 * given element (if any)
	 * 
	 * @param window
	 *            The containing window
	 * @param element
	 *            The element to find the MPlaceholder for
	 * @return the MPlaceholder or null if none is found
	 */
	public MPlaceholder findPlaceholderFor(MWindow window, MUIElement element);

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

	/**
	 * Inserts the given element into the UI Model by either creating a new sash or augmenting an
	 * existing sash if the orientation permits.
	 * 
	 * @param toInsert
	 *            The element to insert
	 * @param relTo
	 *            The element that the new one is to be relative to
	 * @param where
	 *            An SWT constant indicating where the inserted element should be placed
	 * @param ratio
	 *            The percentage of the area to be occupied by the inserted element
	 */
	public void insert(MPartSashContainerElement toInsert, MPartSashContainerElement relTo,
			int where, int ratio);

	/**
	 * Created a separate (detached) window containing the given element.
	 * 
	 * @param mPartSashContainerElement
	 *            The element to detach
	 */
	public void detach(MPartSashContainerElement mPartSashContainerElement);
}
