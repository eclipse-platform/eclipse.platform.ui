/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.registry;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.ui.*;
import org.eclipse.jface.resource.ImageDescriptor;

/**
 * This is a view descriptor. It provides a "description" of a given
 * given view so that the view can later be constructed.
 * <p>
 * [Issue: This interface is not exposed in API, but time may
 * demonstrate that it should be.  For the short term leave it be.
 * In the long term its use should be re-evaluated. ]
 * </p>
 * <p>
 * The view registry provides facilities to map from an extension
 * to a IViewDescriptor.
 * </p>
 * 
 */
public interface IViewDescriptor extends IWorkbenchPartDescriptor {
/**
 * Creates an instance of the view defined in the descriptor.
 */
public IViewPart createView() throws CoreException;
/**
 * Returns an array of strings that represent
 * view's category path. This array will be used
 * for hierarchical presentation of the
 * view in places like submenus.
 * @return array of category tokens or null if not specified.
 */
public String[] getCategoryPath();
/**
 * Returns the configuration element which contributed this view.
 */
public IConfigurationElement getConfigurationElement();
/**
 * Returns the id of the view.
 */
public String getID() ;
/**
 * Returns the descriptor for the icon to show for this view.
 */
public ImageDescriptor getImageDescriptor();
/**
 * Returns the label to show for this view.
 */
public String getLabel() ;

/**
 * Returns the text of the accelerator to use for this view.
 */
public String getAccelerator();

/**
 * Returns the default fast view width ratio for this view.
 * 
 * @since 2.0
 */
public float getFastViewWidthRatio(); 

}
