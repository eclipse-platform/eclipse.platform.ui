/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.keys;

import java.util.Collection;
import java.util.Set;

import org.eclipse.jface.bindings.Scheme;
import org.eclipse.ui.IService;

/**
 * @since 3.1
 */
public interface IBindingService extends IService {

	/**
	 * Returns the collection of all active bindings. When building this
	 * collection, the effect of contexts should be disregarded.
	 * 
	 * @return The collection of active bindings (<code>Binding</code>).
	 *         This collection may be empty, but it is never <code>null</code>.
	 */
	public Collection getActiveBindingsDisregardingContext();

	/**
	 * Returns the currently active scheme.
	 * 
	 * @return The currently active scheme. This value may (in certain rare
	 *         circumstances) be <code>null</code>.
	 */
	public Scheme getActiveScheme();

	/**
	 * Returns the current set of bindings.
	 * 
	 * @return The current set of bindings (<code>Binding</code>). This is
	 *         an unmodifiable collection.
	 */
	public Set getBindings();

	/**
	 * Returns the collection of the identifiers for all of the defined schemes
	 * in the workbench.
	 * 
	 * @return The collection of scheme identifiers (<code>String</code>)
	 *         that are defined; never <code>null</code>, but may be empty.
	 */
	public Collection getDefinedSchemeIds();

	/**
	 * Returns the currently active locale.
	 * 
	 * @return The current locale.
	 */
	public String getLocale();

	/**
	 * Returns the currently active platform.
	 * 
	 * @return The current platform.
	 */
	public String getPlatform();

	/**
	 * Retrieves the scheme with the given identifier. If no such scheme exists,
	 * then an undefined scheme with the given id is created.
	 * 
	 * @param schemeId
	 *            The identifier to find; must not be <code>null</code>.
	 * @return A scheme with the given identifier, either defined or undefined.
	 */
	public Scheme getScheme(String schemeId);

}
