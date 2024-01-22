/*******************************************************************************
 * Copyright (c) 2004, 2015 IBM Corporation and others.
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
package org.eclipse.ui.internal.intro;

/**
 * Registry for introduction elements.
 *
 * @since 3.0
 */
public interface IIntroRegistry {

	/**
	 * Return the number of introduction extensions known by this registry.
	 *
	 * @return the number of introduction extensions known by this registry
	 */
	int getIntroCount();

	/**
	 * Return the introduction extensions known by this registry.
	 *
	 * @return the introduction extensions known by this registry
	 */
	IIntroDescriptor[] getIntros();

	/**
	 * Return the introduction extension that is bound to the given product.
	 *
	 * @param productId the product identifier
	 * @return the introduction extension that is bound to the given product, or
	 *         <code>null</code> if there is no such binding
	 */
	IIntroDescriptor getIntroForProduct(String productId);

	/**
	 * Find an intro descriptor with the given identifier.
	 *
	 * @param id the id
	 * @return the intro descriptor, or <code>null</code>
	 */
	IIntroDescriptor getIntro(String id);
}
