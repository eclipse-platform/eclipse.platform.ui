/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui;

import org.eclipse.jface.resource.ImageDescriptor;

/**
 * A perspective descriptor describes a perspective in an
 * <code>IPerspectiveRegistry</code>.
 * <p>
 * A perspective is a template for view visibility, layout, and action
 * visibility within a workbench page. There are two types of perspective: a
 * predefined perspective and a custom perspective.
 * </p>
 * <ul>
 * <li>A predefined perspective is defined by an extension to the workbench's
 * perspective extension point (<code>"org.eclipse.ui.perspectives"</code>). The
 * extension defines a id, label, and <code>IPerspectiveFactory</code>. A
 * perspective factory is used to define the initial layout for a page.</li>
 * <li>A custom perspective is defined by the user. In this case a predefined
 * perspective is modified to suit a particular task and saved as a new
 * perspective. The attributes for the perspective are stored in a separate file
 * in the workbench's metadata directory.</li>
 * </ul>
 * <p>
 * Within a page the user can open any of the perspectives known to the
 * workbench's perspective registry, typically by selecting one from the
 * workbench's <code>Open Perspective</code> menu. When selected, the views and
 * actions within the active page rearrange to reflect the perspective.
 * </p>
 * <p>
 * This interface is not intended to be implemented by clients.
 * </p>
 *
 * @see IPerspectiveRegistry
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IPerspectiveDescriptor {
	/**
	 * Returns the description of this perspective. This is the value of its
	 * <code>"description"</code> attribute.
	 *
	 * @return the description
	 * @since 3.0
	 */
	String getDescription();

	/**
	 * Returns this perspective's id. For perspectives declared via an extension,
	 * this is the value of its <code>"id"</code> attribute.
	 *
	 * @return the perspective id
	 */
	String getId();

	/**
	 * Returns the descriptor of the image to show for this perspective. If the
	 * extension for this perspective specifies an image, the descriptor for it is
	 * returned. Otherwise a default image is returned.
	 *
	 * @return the descriptor of the image to show for this perspective
	 */
	ImageDescriptor getImageDescriptor();

	/**
	 * Returns this perspective's label. For perspectives declared via an extension,
	 * this is the value of its <code>"label"</code> attribute.
	 *
	 * @return the label
	 */
	String getLabel();

	/**
	 * Returns this perspective's preferred view to show resources when no better
	 * editor or view can be deduced from context.
	 * <p>
	 * For perspectives declared via an extension, this is the value of its
	 * <code>defaultShowIn</code> attribute.
	 * </p>
	 *
	 * @return the preferred view id, or <code>null</code>.
	 * @since 3.124
	 */
	String getDefaultShowIn();
}
