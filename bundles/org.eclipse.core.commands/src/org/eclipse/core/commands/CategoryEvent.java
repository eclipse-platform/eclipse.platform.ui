/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.commands;

/**
 * An instance of this class describes changes to an instance of
 * <code>Category</code>.
 * <p>
 * This class is not intended to be extended by clients.
 * </p>
 * <p>
 * <em>EXPERIMENTAL</em>. The commands architecture is currently under
 * development for Eclipse 3.1. This class -- its existence, its name and its
 * methods -- are in flux. Do not use this class yet.
 * </p>
 * 
 * @since 3.1
 * @see ICategoryListener#categoryChanged(CategoryEvent)
 */
public class CategoryEvent {

	/**
	 * The category that has changed; this value is never <code>null</code>.
	 */
	private final Category category;

	/**
	 * Whether the defined state of the category has changed.
	 */
	private final boolean definedChanged;

	/**
	 * Whether the description of the category has changed.
	 */
	private final boolean descriptionChanged;

	/**
	 * Whether the name of the category has changed.
	 */
	private final boolean nameChanged;

	/**
	 * Creates a new instance of this class.
	 * 
	 * @param category
	 *            the instance of the interface that changed.
	 * @param definedChanged
	 *            true, iff the defined property changed.
	 * @param descriptionChanged
	 *            true, iff the description property changed.
	 * @param nameChanged
	 *            true, iff the name property changed.
	 */
	public CategoryEvent(final Category category, final boolean definedChanged,
			final boolean descriptionChanged, final boolean nameChanged) {
		if (category == null)
			throw new NullPointerException();

		this.category = category;
		this.definedChanged = definedChanged;
		this.descriptionChanged = descriptionChanged;
		this.nameChanged = nameChanged;
	}

	/**
	 * Returns the instance of the interface that changed.
	 * 
	 * @return the instance of the interface that changed. Guaranteed not to be
	 *         <code>null</code>.
	 */
	public final Category getCategory() {
		return category;
	}

	/**
	 * Returns whether or not the defined property changed.
	 * 
	 * @return true, iff the defined property changed.
	 */
	public final boolean hasDefinedChanged() {
		return definedChanged;
	}

	/**
	 * Returns whether or not the description property changed.
	 * 
	 * @return true, iff the description property changed.
	 */
	public final boolean hasDescriptionChanged() {
		return descriptionChanged;
	}

	/**
	 * Returns whether or not the name property changed.
	 * 
	 * @return true, iff the name property changed.
	 */
	public final boolean hasNameChanged() {
		return nameChanged;
	}
}
