/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.viewers.model.provisional;

import org.eclipse.debug.ui.commands.IStatusMonitor;
import org.eclipse.jface.viewers.TreePath;

/**
 * A context sensitive viewer update request.
 * 
 * @since 3.3
 */
public interface IViewerUpdate extends IStatusMonitor {

	/**
	 * Returns the context this update was requested in.
	 * 
	 * @return context this update was requested in
	 */
	public IPresentationContext getPresentationContext();
    
    /**
     * Returns the model element corresponding to the given tree path.
     * Returns the root element for the empty path.
     * 
     * @param path viewer tree path
     * @return corresponding model element
     */
    public Object getElement(TreePath path);
}
