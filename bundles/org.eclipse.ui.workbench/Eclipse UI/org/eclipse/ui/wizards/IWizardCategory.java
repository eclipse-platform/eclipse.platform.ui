/*******************************************************************************
 * Copyright (c) 2004, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Jan-Hendrik Diederich, Bredex GmbH - bug 201052
 *******************************************************************************/
package org.eclipse.ui.wizards;

import org.eclipse.core.runtime.IPath;

/**
 * A wizard category may contain other categories or wizard elements. 
 * <p>
 * This interface is not intended to be implemented by clients.
 * </p>
 * 
 * @since 3.1
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IWizardCategory {

	/**
	 * Returns the category child object corresponding to the passed path
	 * (relative to this object), or <code>null</code> if such an object could
	 * not be found.  The segments of this path should correspond to category ids.
	 * 
	 * @param path
	 *            the search path
	 * @return the category or <code>null</code>
	 */
	IWizardCategory findCategory(IPath path);

	/**
	 * Find a wizard that has the provided id. This will search recursivly over
	 * this categories children.
	 * 
	 * @param id
	 *            the id to search for
	 * @return the wizard or <code>null</code>
	 */
	IWizardDescriptor findWizard(String id);

	/**
	 * Return the immediate child categories.
	 * 
	 * @return the child categories. Never <code>null</code>.
	 */
	IWizardCategory[] getCategories();

	/**
	 * Return the identifier of this category.
	 * 
	 * @return the identifier of this category
	 */
	String getId();

	/**
	 * Return the label for this category.
	 * 
	 * @return the label for this category
	 */
	String getLabel();

	/**
	 * Return the parent category.
	 * 
	 * @return the parent category. May be <code>null</code>.
	 */
	IWizardCategory getParent();

	/**
	 * Return this wizards path. The segments of this path will correspond to
	 * category ids.
	 * 
	 * @return the path
	 */
	IPath getPath();

	/**
	 * Return the wizards in this category, minus the wizards which failed
	 * the Expressions check.
	 * 
	 * @return the wizards in this category. Never <code>null</code>
	 */
	IWizardDescriptor[] getWizards();
}
