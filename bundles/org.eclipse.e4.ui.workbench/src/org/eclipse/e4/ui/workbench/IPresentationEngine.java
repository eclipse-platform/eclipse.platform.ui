/*******************************************************************************
 * Copyright (c) 2009, 2015 BestSolution.at and others.
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
import org.eclipse.e4.ui.model.application.ui.basic.MPartSashContainer;

/**
 * The presentation engine is used to translate the generic workbench model into widgets.
 * Implementations of this service are responsible for creating or destroying widgets corresponding
 * to model elements, as well as for running any event loop required for handling user events on
 * those widgets.
 *
 * @noimplement This interface is not intended to be implemented by clients.
 * @since 1.0
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
	 * Declare the stack as containing a singe 'standalone' view. These stacks will not allow either
	 * dragging the view out of the stack nor dragging other views in.
	 *
	 * @since 1.1
	 */
	public static final String STANDALONE = "Standalone"; //$NON-NLS-1$

	/**
	 * Don't remove the element from the display even if it has no displayable children
	 */
	public static final String NO_AUTO_COLLAPSE = "NoAutoCollapse"; //$NON-NLS-1$

	/**
	 * When applied as a tag to an MUIElement inhibits moving the element (ie. through DnD...
	 */
	public static final String NO_MOVE = "NoMove"; //$NON-NLS-1$

	/**
	 * This tag can be used by the renderer implementation to decide that the user interface element
	 * has been hidden.
	 *
	 * @since 1.1
	 */
	public static final String HIDDEN_EXPLICITLY = "HIDDEN_EXPLICITLY"; //$NON-NLS-1$

	/**
	 * This key is used to store information in the 'persistentData' map which will be used to
	 * override the initial style of an element at rendering time. For example the SWT renderer will
	 * expect to see an integer (as a string) which defines the initial SWT style bits.
	 *
	 * @since 1.1
	 */
	public static String STYLE_OVERRIDE_KEY = "styleOverride"; //$NON-NLS-1$

	/**
	 * When applied to an MWindow causes the renderer to minimize the resulting control.
	 *
	 * @since 1.1
	 */
	public static String WINDOW_MINIMIZED_TAG = "shellMinimized"; //$NON-NLS-1$

	/**
	 * When applied to an MWindow causes the renderer to maximize the resulting control.
	 *
	 * @since 1.1
	 */
	public static String WINDOW_MAXIMIZED_TAG = "shellMaximized"; //$NON-NLS-1$

	/**
	 * When applied to an MArea causes it to behave like a
	 * {@link MPartSashContainer} allowing the different parts to be
	 * minimized/maximized separately.
	 *
	 * @since 1.5
	 */
	public static String MIN_MAXIMIZEABLE_CHILDREN_AREA_TAG = "MinMaximizeableChildrenArea"; //$NON-NLS-1$

	/**
	 * When applied to an MWindow causes the renderer to render the resulting control as a top level
	 * window
	 *
	 * @since 1.3
	 */
	public static String WINDOW_TOP_LEVEL = "shellTopLevel"; //$NON-NLS-1$

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
	 * This tag can be applied to an element (usually an MPart) to indicate that the element should
	 * be split with the result being side by side.
	 *
	 * @since 1.1
	 */
	public static String SPLIT_HORIZONTAL = "Split Horizontal"; //$NON-NLS-1$

	/**
	 * This tag can be applied to an element (usually an MPart) to indicate that the element should
	 * be split with the result being one above the other.
	 *
	 * @since 1.1
	 */
	public static String SPLIT_VERTICAL = "Split Vertical"; //$NON-NLS-1$

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
	 *
	 * NOTE: This image must be checked to ensure that it hasn't been disposed on retrieval.
	 */
	public static final String OVERRIDE_ICON_IMAGE_KEY = "e4_override_icon_image_key"; //$NON-NLS-1$

	/**
	 * This key should be used to add an optional String to an elements TRANSIENTDATA. If present,
	 * the string will be used to override the elements TitleToolTip. An example is setting the
	 * ToolTip of a minimized problems view stack to the number of errors and warnings in the view.
	 */
	public static final String OVERRIDE_TITLE_TOOL_TIP_KEY = "e4_override_title_tool_tip_key"; //$NON-NLS-1$

	/**
	 * This is a Tag that when applied to an MUILabel element will cause whatever Image is to be
	 * shown to be adorned with the 'pinned' affordance.
	 *
	 * @since 1.1
	 */
	public static final String ADORNMENT_PIN = "Pin Adornment"; //$NON-NLS-1$

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
	 * This key can be used , if the model element does not have a parent and a
	 * parent needs to be specified for the renderer to create the widget.
	 *
	 * @since 1.4
	 */
	public static final String RENDERING_PARENT_KEY = "Rendering Parent"; //$NON-NLS-1$

	/**
	 * This is the tag name that enables the DND support for the element. The
	 * element's tags list has to be updated with the tag in order to enable the
	 * DND processing.
	 *
	 * @since 1.1
	 */
	public static final String DRAGGABLE = "Draggable"; //$NON-NLS-1$

	/**
	 * This is the tag name that indicates that the model element is active.
	 *
	 * @since 1.3
	 */
	public static final String ACTIVE = "active"; //$NON-NLS-1$

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
	 * Attempts to set the UI focus onto the given element. By default we delegate this to the
	 * elements implementation's @Focus method (if any). If no such method exists we delegate the
	 * the renderer's 'forceFocus' method.
	 *
	 * @param element
	 */
	public void focusGui(MUIElement element);

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
