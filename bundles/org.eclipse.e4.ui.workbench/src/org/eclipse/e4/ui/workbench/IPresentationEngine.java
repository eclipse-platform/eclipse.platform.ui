/*******************************************************************************
 * Copyright (c) 2009, 2012 BestSolution.at and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tom Schindl<tom.schindl@bestsolution.at> - initial API and implementation
 *     IBM - ongoing development
 ******************************************************************************/

package org.eclipse.e4.ui.workbench;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.ui.model.application.MApplicationElement;
import org.eclipse.e4.ui.model.application.ui.MUIElement;

/**
 * The presentation engine is used to translate the generic workbench model into widgets.
 * Implementations of this service are responsible for creating or destroying widgets corresponding
 * to model elements, as well as for running any event loop required for handling user events on
 * those widgets.
 */
public interface IPresentationEngine {
	/**
	 * The ID to access the service in the {@link IEclipseContext}
	 */
	public static final String SERVICE_NAME = IPresentationEngine.class.getName();

	/**
	 * When applied as a tag to an MUILabel inhibits the display of the label text
	 */
	public static final String NO_TITLE = "NoTitle"; //$NON-NLS-1$

	/**
	 * When applied as a tag to an MPlaceholder inhibits the display of the close affordance. This
	 * allows a part to be closeable in one perspective but not in a different one.
	 * 
	 * <b>NOTE:</b> If you are not using perspectives then use the MPart's 'isCloseable' attribute
	 * to control the affordance.
	 */
	public static final String NO_CLOSE = "NoClose"; //$NON-NLS-1$

	/**
	 * Don't remove the element from the display even if it has no displayable children
	 */
	public static final String NO_AUTO_COLLAPSE = "NoAutoCollapse"; //$NON-NLS-1$

	/**
	 * When applied as a tag to an MUIElement inhibits moving the element (ie. through DnD...
	 */
	public static final String NO_MOVE = "NoMove"; //$NON-NLS-1$

	/**
	 * When added to an element's 'tags' this should cause the presentation to move that element to
	 * the trim. In the default implementation you can only apply this tag to an MPartStack or the
	 * MPlaceholder of the MArea.
	 */
	public static String MINIMIZED = "Minimized"; //$NON-NLS-1$

	/**
	 * When added to an element's 'tags' this should cause the presentation to minimize all other
	 * presentation elements. In the default implementation you can only apply this tag to an
	 * MPartStack or the MPlaceholder of the MArea.
	 */
	public static String MAXIMIZED = "Maximized"; //$NON-NLS-1$

	/**
	 * This tag should be applied to any element that had its MINIMIZED tag set due to a different
	 * element going maximized. This allows the restore operation to only restore elements that the
	 * user did not explicitly minimize.
	 */
	public static String MINIMIZED_BY_ZOOM = "MinimizedByZoom"; //$NON-NLS-1$

	/**
	 * This tag can be applied to an element as a hint to the renderers that the element would
	 * prefer to be horizontal. For an MPart this could be used both as a hint to how to show the
	 * view when it's in the trim but could also be used when picking a stack to add a newly opening
	 * part to. It could also be used for example to control where the tabs appear on an MPartStack.
	 */
	public static String ORIENTATION_HORIZONTAL = "Horizontal"; //$NON-NLS-1$

	/**
	 * This tag can be applied to an element as a hint to the renderers that the element would
	 * prefer to be vertical. For an MPart this could be used both as a hint to how to show the view
	 * when it's in the trim but could also be used when picking a stack to add a newly opening part
	 * to. It could also be used for example to control where the tabs appear on an MPartStack.
	 */
	public static String ORIENTATION_VERTICAL = "Vertical"; //$NON-NLS-1$

	/**
	 * This key should be used to add an optional String to an element that is a URI to the elements
	 * disabled icon. This is used, for example, by Toolbar Items which, in Eclipse SDK, provide a
	 * unique icon for disabled tool items that look better than the OS default graying on the
	 * default icon.
	 * 
	 * There is a strong argument to be made that this disabledIconURI actually be part of the model
	 */
	public static final String DISABLED_ICON_IMAGE_KEY = "e4_disabled_icon_image_key"; //$NON-NLS-1$

	/**
	 * This key should be used to add an optional org.eclipse.swt.graphics.Image to an elements
	 * TRANSIENTDATA. If present, the image will be used to override that elements iconURI. An
	 * example is drawing the error icon on a minimized problems view stack.
	 */
	public static final String OVERRIDE_ICON_IMAGE_KEY = "e4_override_icon_image_key"; //$NON-NLS-1$

	/**
	 * This key should be used to add an optional String to an elements TRANSIENTDATA. If present,
	 * the string will be used to override the elements TitleToolTip. An example is setting the
	 * ToolTip of a minimized problems view stack to the number of errors and warnings in the view.
	 */
	public static final String OVERRIDE_TITLE_TOOL_TIP_KEY = "e4_override_title_tool_tip_key"; //$NON-NLS-1$

	/**
	 * This is a <b>Boolean</b> preference used to control animations in the application
	 */
	public static final String ANIMATIONS_ENABLED = "Animations Enabled"; //$NON-NLS-1$

	/**
	 * This is a persistedState 'key' whose value is expected to be the URI of a subclass of
	 * ABstractPartRenderer that is to be used to render the element
	 */
	public static final String CUSTOM_RENDERER_KEY = "Custom Renderer"; //$NON-NLS-1$	

	/**
	 * Creates and returns the UI element for the given model element.
	 * 
	 * @param element
	 *            the model element
	 * @param parentWidget
	 *            the parent
	 * @param parentContext
	 *            the context within which this element is being rendered
	 * 
	 * @return the created UI element
	 */
	public Object createGui(MUIElement element, Object parentWidget, IEclipseContext parentContext);

	/**
	 * Creates and returns the UI element corresponding to the given model element. The resulting UI
	 * element sits at the top of a widget hierarchy
	 * 
	 * @param element
	 *            the model element
	 * @return the create UI element
	 */
	public Object createGui(MUIElement element);

	/**
	 * Remove the UI element created for this model element.
	 * 
	 * @param element
	 *            the model element whose UI element should removed
	 */
	public void removeGui(MUIElement element);

	/**
	 * Run the UI. This method is responsible for creating the initial UI and (if necessary)
	 * spinning the event loop for the life of the application.
	 * 
	 * @param uiRoot
	 * @param appContext
	 * 
	 * @return The application's return value
	 */
	public Object run(MApplicationElement uiRoot, IEclipseContext appContext);

	/**
	 * Shuts down the presentation engine
	 */
	public void stop();
}
