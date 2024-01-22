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
 *     Markus Alexander Kuppe, Versant Corporation - bug #215797
 *******************************************************************************/
package org.eclipse.ui.views;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPartDescriptor;

/**
 * This is a view descriptor. It provides a "description" of a given given view
 * so that the view can later be constructed.
 * <p>
 * The view registry provides facilities to map from an extension to a
 * IViewDescriptor.
 * </p>
 * <p>
 * This interface is not intended to be implemented by clients.
 * </p>
 *
 * @see org.eclipse.ui.views.IViewRegistry
 * @since 3.1
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IViewDescriptor extends IWorkbenchPartDescriptor, IAdaptable {
	/**
	 * Creates an instance of the view defined in the descriptor.
	 *
	 * @return the view part
	 * @throws CoreException thrown if there is a problem creating the part
	 */
	IViewPart createView() throws CoreException;

	/**
	 * Returns an array of strings that represent view's category path. This array
	 * will be used for hierarchical presentation of the view in places like
	 * submenus.
	 *
	 * @return array of category tokens or null if not specified.
	 */
	String[] getCategoryPath();

	/**
	 * Returns the description of this view.
	 *
	 * @return the description
	 */
	String getDescription();

	/**
	 * Returns the id of the view.
	 *
	 * @return the id
	 */
	@Override
	String getId();

	/**
	 * Returns the descriptor for the icon to show for this view.
	 */
	@Override
	ImageDescriptor getImageDescriptor();

	/**
	 * Returns the label to show for this view.
	 *
	 * @return the label
	 */
	@Override
	String getLabel();

	/**
	 * Returns the default fast view width ratio for this view.
	 *
	 * @return the fast view width ratio
	 */
	float getFastViewWidthRatio();

	/**
	 * Returns whether this view allows multiple instances.
	 *
	 * @return whether this view allows multiple instances
	 */
	boolean getAllowMultiple();

	/**
	 * Returns whether this view can be restored upon workbench restart.
	 *
	 * @return whether whether this view can be restored upon workbench restart
	 * @since 3.4
	 */
	boolean isRestorable();

}
