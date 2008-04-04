/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.wizards;


/**
 * A registry describing all wizard extensions known to the workbench.
 * <p>
 * This interface is not intended to be implemented by clients.
 * </p>
 * 
 * @since 3.1
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IWizardRegistry {

	/**
	 * Find a wizard with the given id.
	 * 
	 * @param id the id to search for
	 * @return the wizard descriptor matching the given id or <code>null</code>
	 */
	IWizardDescriptor findWizard(String id);
	
	/**
	 * Return the wizards that have been designated as "primary".
	 * 
	 * @return the primary wizard descriptors.  Never <code>null</code>.
	 */
	IWizardDescriptor [] getPrimaryWizards();

	/**
	 * Find the category with the given id.
	 * 
	 * @param id the id of the category to search for
	 * @return the category matching the given id or <code>null</code>
	 */
	IWizardCategory findCategory(String id);
	
	/**
	 * Return the root category.
	 * 
	 * @return the root category.  Never <code>null</code>.
	 */
	IWizardCategory getRootCategory();
}
