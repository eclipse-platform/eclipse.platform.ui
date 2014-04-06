/*******************************************************************************
 * Copyright (c) 2010, 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.e4.ui.workbench.modeling;

import java.util.List;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.ui.model.application.MApplicationElement;
import org.eclipse.e4.ui.model.application.descriptor.basic.MPartDescriptor;
import org.eclipse.e4.ui.model.application.ui.MElementContainer;
import org.eclipse.e4.ui.model.application.ui.MSnippetContainer;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.SideValue;
import org.eclipse.e4.ui.model.application.ui.advanced.MPerspective;
import org.eclipse.e4.ui.model.application.ui.advanced.MPlaceholder;
import org.eclipse.e4.ui.model.application.ui.basic.MPartSashContainerElement;
import org.eclipse.e4.ui.model.application.ui.basic.MTrimBar;
import org.eclipse.e4.ui.model.application.ui.basic.MTrimmedWindow;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.workbench.Selector;

/**
 * This service is used to find, create and handle model elements
 * 
 * @since 1.0
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface EModelService {
	// Insertion constants

	/** Insert the new element above the existing one */
	public static final int ABOVE = 0;

	/** Insert the new element below the existing one */
	public static final int BELOW = 1;

	/** Insert the new element to the left of the existing one */
	public static final int LEFT_OF = 2;

	/** Insert the new element to the right of the existing one */
	public static final int RIGHT_OF = 3;

	// Search modifiers / Location Constants

	/** Returned Location if the element's parent chain does not relate to the MApplication's model */
	public static final int NOT_IN_UI = 0x00;

	/** Returned Location if the element is in the UI but not in an MPerspective */
	public static final int OUTSIDE_PERSPECTIVE = 0x01;

	/** Returned Location if the element is in the currently active perspective */
	public static final int IN_ACTIVE_PERSPECTIVE = 0x02;

	/** Returned Location if the element is in a non-active perspective */
	public static final int IN_ANY_PERSPECTIVE = 0x04;

	/** Returned Location if the element is contained in an MArea */
	public static final int IN_SHARED_AREA = 0x08;

	/** Returned Location if the element is in an MTrimBar */
	public static final int IN_TRIM = 0x10;

	/**
	 * Returned Location if the element is in a main menu of an MWindow
	 * 
	 * @since 1.1
	 */
	public static final int IN_MAIN_MENU = 0x20;

	/**
	 * Returned Location if the element is in a MPart
	 * 
	 * @since 1.1
	 */
	public static final int IN_PART = 0x40;

	// 'Standard' searches

	/** Searches for elements in the UI that the user is currently seeing (excluding trim) */
	public static final int PRESENTATION = OUTSIDE_PERSPECTIVE | IN_ACTIVE_PERSPECTIVE
			| IN_SHARED_AREA;

	/** Searches for elements in the UI presentation, including all perspectives */
	public static final int ANYWHERE = OUTSIDE_PERSPECTIVE | IN_ANY_PERSPECTIVE | IN_SHARED_AREA
			| IN_TRIM;

	/**
	 * Searches for elements in the UI that the user is currently seeing that are OUTSIDE the
	 * perspective (i.e. visible regardless of the current perspective)
	 */
	public static final int GLOBAL = OUTSIDE_PERSPECTIVE | IN_SHARED_AREA;

	/**
	 * When invoking the 'cloneElement' method the newly cloned element's 'transientData' map will
	 * contain a reference to the original element using this as a key.
	 * 
	 * @since 1.1
	 */
	public static String CLONED_FROM_KEY = "Cloned From"; //$NON-NLS-1$

	/**
	 * Creates instances of model elements. The method supports any type extending
	 * {@link MApplicationElement}, both in the standard e4 UI model and in an extension models.
	 * 
	 * <p>
	 * <b>Caution:</b> To create model element instances of extension models you need to register
	 * them with the <code>the org.eclipse.e4.workbench.model.definition.enrichment</code>
	 * ExtensionPoint.
	 * </p>
	 * 
	 * @param elementType
	 *            the class to instantiate. Cannot be <code>null</code>
	 * @return a new instance
	 * @throws NullPointerException
	 *             if the passed class is <code>null</code>
	 * @throws IllegalArgumentException
	 *             if the passed class is not supported.
	 */
	public <T extends MApplicationElement> T createModelElement(Class<T> elementType);

	/**
	 * This is a convenience method that constructs a new Selector based on {@link ElementMatcher}
	 * and forwards the call on to the base API
	 * {@link EModelService#findElements(MApplicationElement, Class, int, Selector)}.
	 * 
	 * @see EModelService#findElements(MApplicationElement, Class, int, Selector)
	 */
	public <T> List<T> findElements(MUIElement searchRoot, String id, Class<T> clazz,
			List<String> tagsToMatch, int searchFlags);

	/**
	 * This is a convenience method that forwards the parameters on to
	 * {@link EModelService#findElements(MUIElement, String, Class, List, int)}, passing
	 * {@link EModelService#ANYWHERE} as the 'searchFlags'.
	 * 
	 */
	public <T> List<T> findElements(MUIElement searchRoot, String id, Class<T> clazz,
			List<String> tagsToMatch);

	/**
	 * Return a list of any elements that match the given search criteria. The search is recursive
	 * and includes the specified search root. Any of the search parameters may be specified as
	 * <code>null</code> in which case that field will always 'match'.
	 * <p>
	 * NOTE: This is a generically typed method with the List's generic type expected to be the
	 * value of the 'clazz' parameter. If the 'clazz' parameter is null then the returned list is
	 * untyped.
	 * </p>
	 * 
	 * @param <T>
	 *            The generic type of the returned list
	 * @param searchRoot
	 *            The element at which to start the search. This element must be non-null and is
	 *            included in the search.
	 * @param clazz
	 *            The type of element to be searched for. If non-null this also defines the return
	 *            type of the List.
	 * @param searchFlags
	 *            A bitwise combination of the following constants:
	 *            <ul>
	 *            <li><b>OUTSIDE_PERSPECTIVE</b> Include the elements in the window's model that are
	 *            not in a perspective</;i>
	 *            <li><b>IN_ANY_PERSPECTIVE</b> Include the elements in all perspectives</;i>
	 *            <li><b>IN_ACTIVE_PERSPECTIVE</b> Include the elements in the currently active
	 *            perspective only</;i>
	 *            <li><b>IN_MAIN_MENU</b> Include elements in an MWindow's main menu</;i>
	 *            <li><b>IN_PART</b> Include MMenu and MToolbar elements owned by parts</;i>
	 *            <li><b>IN_ACTIVE_PERSPECTIVE</b> Include the elements in the currently active
	 *            perspective only</;i>
	 *            <li><b>IN_SHARED_AREA</b> Include the elements in the shared area</;i>
	 *            <li><b>IN_TRIM</b> Include the elements in the window's trim</;i>
	 *            </ul>
	 *            Note that you may omit both perspective flags but still define
	 *            <b>IN_SHARED_AREA</b>; the flags <b>OUTSIDE_PERSPECTIVE | IN_SHARED_AREA</b> for
	 *            example will search the presentation <i>excluding</i> the elements in perspective
	 *            stacks.
	 * @param matcher
	 *            An implementation of a Selector that will return true for elements that it wants
	 *            in the returned list.
	 * @return The generically typed list of matching elements.
	 * 
	 * @since 1.1
	 */
	public <T> List<T> findElements(MApplicationElement searchRoot, Class<T> clazz,
			int searchFlags, Selector matcher);

	/**
	 * Returns the first element, recursively searching under the specified search root (inclusive)
	 * 
	 * @param id
	 *            The id to search for, must not be null
	 * @param searchRoot
	 *            The element at which to start the search, must not be null
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
	 * Brings the specified element to the top of its containment structure. If the specified
	 * element is a top-level window, then it will be selected as the application's currently active
	 * window. Otherwise, the element may merely be brought up to be seen by the user but not
	 * necessarily have its containing window become the application's active window.
	 * 
	 * @param element
	 *            The element to bring to the top
	 */
	public void bringToTop(MUIElement element);

	/**
	 * Clones the element, creating a deep copy of its structure.
	 * 
	 * NOTE: The cloned element gets the original element added into its 'transientData' map using
	 * the CLONED_FROM_KEY key. This is useful in cases where there may be other information the
	 * newly cloned element needs from the original.
	 * 
	 * @param element
	 *            The element to clone
	 * @param snippetContainer
	 *            An optional MUIElement where the cloned snippet is to be saved. null if the clone
	 *            need not be saved
	 * @return The newly cloned element
	 */
	public MUIElement cloneElement(MUIElement element, MSnippetContainer snippetContainer);

	/**
	 * If a snippet with the given id exists a clone is created and returned. returns
	 * <code>null</code> if no snippet can be found.
	 * 
	 * @param snippetContainer
	 *            The container of the snippet to clone used
	 * @param snippetId
	 *            The element id of the snippet to clone
	 * @param refWin
	 *            The window that Placeholder references should be resolved using
	 * 
	 * @return The cloned snippet or <code>null</code> if no snippet with the given id can be found
	 */
	public MUIElement cloneSnippet(MSnippetContainer snippetContainer, String snippetId,
			MWindow refWin);

	/**
	 * Convenience method to find a snippet by id in a particular container
	 * 
	 * @param snippetContainer
	 *            The container to look in
	 * @param id
	 *            The id of the root element of the snippet
	 * @return The root element of the snippet or <code>null</code> if none is found
	 */
	public MUIElement findSnippet(MSnippetContainer snippetContainer, String id);

	/**
	 * Return the count of the children whose 'toBeRendered' flag is true
	 * 
	 * @param element
	 *            The element to test
	 * @return the number of children with 'toBeRendered' == true
	 */
	public int countRenderableChildren(MUIElement element);

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
			int where, float ratio);

	/**
	 * Created a separate (detached) window containing the given element.
	 * 
	 * @param mPartSashContainerElement
	 *            The element to detach
	 * @param x
	 *            The X position of the new window
	 * @param y
	 *            The Y position of the new window
	 * @param width
	 *            The Width of the new window
	 * @param height
	 *            The Height of the new window
	 */
	public void detach(MPartSashContainerElement mPartSashContainerElement, int x, int y,
			int width, int height);

	/**
	 * Get the top-level window containing this UI element. A <code>null</code> return value
	 * indicates that the element is not directly contained in the UI model (but may, for example,
	 * be a model snippet hosted in a Dialog...)
	 * 
	 * @param element
	 *            The element to get the window for
	 * 
	 * @return the top-level window containing this UI element. A <code>null</code> return value
	 *         indicates that the element is not directly contained in the UI model (but may, for
	 *         example, be a model snippet hosted in a Dialog...)
	 */
	public MWindow getTopLevelWindowFor(MUIElement element);

	/**
	 * @param element
	 *            The element to get the perspective for
	 * @return The MPerspective containing this element or <code>null</code> if the element is not
	 *         in a perspective
	 */
	public MPerspective getPerspectiveFor(MUIElement element);

	/**
	 * Returns the window's MTrimBar for the specified side. If necessary the bar will be created.
	 * 
	 * @param window
	 *            The window to get the trim bar for
	 * @param sv
	 *            The value for the specified side
	 * 
	 * @return The appropriate trim bar
	 */
	public MTrimBar getTrim(MTrimmedWindow window, SideValue sv);

	/**
	 * Return the active perspective for the given window. This is a convenience method that just
	 * returns the MPerspectiveStack's selectedElement.
	 * 
	 * @param window
	 *            The window to determine the active perspective for.
	 * 
	 * @return The active perspective or <code>null</code> if there is no MPerspectiveStack, it's
	 *         empty or has no selected element.
	 */
	public MPerspective getActivePerspective(MWindow window);

	/**
	 * This is a convenience method that will clean the model of all traces of a given perspective.
	 * There may be elements (i.e. minimized stacks...) in the window's trim that are associated
	 * with a perspective as well as the need to properly clean up any detached windows associated
	 * with the perspective.
	 * 
	 * @param persp
	 *            the perspective to remove
	 * @param window
	 *            the window to remove it from
	 */
	public void resetPerspectiveModel(MPerspective persp, MWindow window);

	/**
	 * Remove the given perspective completely from the model.
	 * 
	 * @param persp
	 *            the perspective to remove
	 * @param window
	 *            the window to remove it from
	 */
	public void removePerspectiveModel(MPerspective persp, MWindow window);

	/**
	 * Count the number of 'toBeRendered' children
	 * 
	 * @param container
	 *            The container to check
	 * @return The number of children whose toBeRendered flag is <code>true</code>
	 * 
	 */
	public int toBeRenderedCount(MElementContainer<?> container);

	/**
	 * Get the container of the given element. This is a convenience method that will always return
	 * the actual container for the element, even where the element's 'getParent' might return null
	 * (trim, detached windows...)
	 * 
	 * @param element
	 *            The element to get the container for
	 * @return The element's container. This may be <code>null</code> if the element being checked
	 *         is a snippet unattached to the UI Model itself.
	 */
	public MUIElement getContainer(MUIElement element);

	/**
	 * Given an element this method responds with information about where the element exists within
	 * the current UI Model. This is used in cases where it is necessary to know if an element is in
	 * the 'shared area' or outside of any perspective.
	 * 
	 * @param element
	 * @return The location of the element in the UI, will be one of:
	 *         <ul>
	 *         <li><b>NOT_IN_UI:</b> The element is not in the UI model at all</li>
	 *         <li><b>OUTSIDE_PERSPECTIVE:</b> The element not within a perspective stack</li>
	 *         <li><b>IN_ACTIVE_PERSPECTIVE:</b> The element is within the currently active
	 *         perspective</li>
	 *         <li><b>IN_ANY_PERSPECTIVE:</b> The element is within a perspective but not the active
	 *         one</li>
	 *         <li><b>IN_SHARED_AREA:</b> The element is within an area that is shared between
	 *         different perspectives</li>
	 *         </ul>
	 */
	public int getElementLocation(MUIElement element);

	/**
	 * Returns the descriptor for the given part id.
	 * <p>
	 * <b>NOTE:</b> In order to support multiple instance parts there is a convention where the
	 * part's id may be in the form 'partId:secondaryId'. If the given id contains a ':' then only
	 * the substring before the ':' will be used to find the descriptor.
	 * </p>
	 * <p>
	 * In order to support this convention it's required that no descriptor contain a ':' in its id
	 * </p>
	 * 
	 * @param id
	 *            The id of the descriptor to return
	 * @return The descriptor matching the id or <code>null</code> if none exists
	 */
	public MPartDescriptor getPartDescriptor(String id);

	/**
	 * This method ensures that there will never be two placeholders for the same referenced element
	 * visible in the presentation at the same time. It does this by hiding placeholders which are
	 * contained in any MPerspective if there is a placeholder for the element in any 'shared' area
	 * (i.e. visible regardless of which perspective is visible) by setting its 'toBeRendered' state
	 * to <code>false</code>.
	 * 
	 * @param window
	 *            The window to modify
	 * @param perspective
	 *            if non-null specifies the specific perspective to modify, otherwise all
	 *            perspectives in the window are checked
	 */
	public void hideLocalPlaceholders(MWindow window, MPerspective perspective);

	/**
	 * Returns <code>true</code> iff the supplied element represents the single visible element in
	 * the shared area. This method is used to test for this condition since (by convention) there
	 * must be at least one stack in the shared area at all times.
	 * 
	 * @param stack
	 *            The element to test
	 * @return <code>true</code> iff the element is the last visible stack
	 */
	public boolean isLastEditorStack(MUIElement stack);

	/**
	 * Allows an element to be rendered in an arbitrary UI container (I.e. SWT Composite).
	 * 
	 * @param element
	 *            The element to be rendered.
	 * @param hostWindow
	 *            The MWindow the element is being hosted under. Must be non-nulland rendered.
	 * @param uiContainer
	 *            The UI container acting as the rendered element's parent. Must be non-null.
	 * @param hostContext
	 *            The IEclipseContext to use for hosting the element. Must be non-null.
	 */
	public void hostElement(MUIElement element, MWindow hostWindow, Object uiContainer,
			IEclipseContext hostContext);

	/**
	 * Tests whether the given element is being 'hosted'. This method is used to allow UI Elements
	 * to act as if they are contained within a given MWindow even though the element is not
	 * actually structurally contained in that window's UI Model.
	 * 
	 * @param element
	 *            The element to test. Must be non-null.
	 * @param hostWindow
	 *            The window to test the element against. Must be non-null.
	 * 
	 * @return <code>true</code> iff the given element or one of its ancestors is currently being
	 *         hosted in the given MWindow.
	 */
	public boolean isHostedElement(MUIElement element, MWindow hostWindow);
}
