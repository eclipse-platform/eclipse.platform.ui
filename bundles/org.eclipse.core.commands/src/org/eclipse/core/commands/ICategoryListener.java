/*******************************************************************************
 * Copyright (c) 2003, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.core.commands;

/**
 * An instance of this interface can be used by clients to receive notification
 * of changes to one or more instances of <code>Category</code>.
 * <p>
 * This interface may be implemented by clients.
 * </p>
 * <p>
 * <em>EXPERIMENTAL</em>. The commands architecture is currently under
 * development for Eclipse 3.1. This class -- its existence, its name and its
 * methods -- are in flux. Do not use this class yet.
 * </p>
 * 
 * @since 3.1
 * @see Category#addCategoryListener(ICategoryListener)
 * @see Category#removeCategoryListener(ICategoryListener)
 */
public interface ICategoryListener {

	/**
	 * Notifies that one or more properties of an instance of
	 * <code>Category</code> have changed. Specific details are described in
	 * the <code>CategoryEvent</code>.
	 * 
	 * @param categoryEvent
	 *            the category event. Guaranteed not to be <code>null</code>.
	 */
	void categoryChanged(CategoryEvent categoryEvent);
}
