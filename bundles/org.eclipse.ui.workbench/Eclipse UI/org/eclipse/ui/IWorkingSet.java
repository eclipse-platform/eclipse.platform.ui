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
package org.eclipse.ui;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.resource.ImageDescriptor;

/**
 * A working set holds a number of IAdaptable elements. 
 * A working set is intended to group elements for presentation to 
 * the user or for operations on a set of elements.
 * <p>
 * This interface is not intended to be implemented by clients.
 * </p>
 * 
 * @since 2.0
 */
public interface IWorkingSet {
	/**
	 * Returns the elements that are contained in this working set.
	 * 
	 * @return	the working set's elements
	 */
	public IAdaptable[] getElements();

	/**
	 * Returns the working set id. Returns <code>null</code> if no
	 * working set id has been set.
	 * This is one of the ids defined by extensions of the 
	 * org.eclipse.ui.workingSets extension point.
	 * It is used by the workbench to determine the page to use in 
	 * the working set edit wizard. The default resource edit page
	 * is used if this value is <code>null</code>.
	 * 
	 * @return the working set id. May be <code>null</code>
	 * @since 2.1 
	 */
	public String getId();

	/**
	 * Returns the working set icon.
	 * Currently, this is one of the icons specified in the extensions 
	 * of the org.eclipse.ui.workingSets extension point. 
	 * The extension is identified using the value returned by
	 * <code>getId()</code>. 
	 * Returns <code>null</code> if no icon has been specified in the 
	 * extension or if <code>getId()</code> returns <code>null</code>. 
	 * 
	 * @return the working set icon or <code>null</code>.
	 * @since 2.1 
	 */
	public ImageDescriptor getImage();

	/**
	 * Returns the name of the working set.
	 * 
	 * @return	the name of the working set
	 */
	public String getName();

	/**
	 * Sets the elements that are contained in this working set.
	 * 
	 * @param elements the elements to set in this working set
	 */
	public void setElements(IAdaptable[] elements);

	/**
	 * Sets the working set id.
	 * This is one of the ids defined by extensions of the 
	 * org.eclipse.ui.workingSets extension point.
	 * It is used by the workbench to determine the page to use in 
	 * the working set edit wizard. The default resource edit page
	 * is used if this value is <code>null</code>.
	 * 
	 * @param id the working set id. May be <code>null</code>
	 * @since 2.1 
	 */
	public void setId(String id);

	/**
	 * Sets the name of the working set. 
	 * The working set name should be unique.
	 * The working set name must not have leading or trailing 
	 * whitespace.
	 * 
	 * @param name the name of the working set
	 */
	public void setName(String name);
}
