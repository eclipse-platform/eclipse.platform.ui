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

package org.eclipse.ui.activities;

/**
 * <p>
 * An instance of <code>CategoryEvent</code> describes changes to an instance
 * of <code>ICategory</code>.
 * </p>
 * <p>
 * This class is not intended to be extended by clients.
 * </p>
 * <p>
 * <em>EXPERIMENTAL</em>
 * </p>
 * 
 * @since 3.0
 * @see ICategory
 * @see ICategoryListener#categoryChanged
 */
public final class CategoryEvent {
	private ICategory category;
	private boolean categoryActivityBindingsChanged;
	private boolean definedChanged;
	private boolean descriptionChanged;
	private boolean nameChanged;

	/**
	 * TODO javadoc
	 * 
	 * @param category
	 * @param categoryActivityBindingsChanged
	 * @param definedChanged
	 * @param descriptionChanged
	 * @param nameChanged
	 */
	public CategoryEvent(
		ICategory category,
		boolean categoryActivityBindingsChanged,
		boolean definedChanged,
		boolean descriptionChanged,
		boolean nameChanged) {
		if (category == null)
			throw new NullPointerException();

		this.category = category;
		this.categoryActivityBindingsChanged = categoryActivityBindingsChanged;
		this.definedChanged = definedChanged;
		this.descriptionChanged = descriptionChanged;
		this.nameChanged = nameChanged;
	}

	/**
	 * Returns the instance of <code>ICategory</code> that has changed.
	 * 
	 * @return the instance of <code>ICategory</code> that has changed.
	 *         Guaranteed not to be <code>null</code>.
	 */
	public ICategory getCategory() {
		return category;
	}

	/**
	 * TODO javadoc
	 */
	public boolean hasDefinedChanged() {
		return definedChanged;
	}

	/**
	 * TODO javadoc
	 */
	public boolean hasDescriptionChanged() {
		return descriptionChanged;
	}

	/**
	 * TODO javadoc
	 */
	public boolean hasNameChanged() {
		return nameChanged;
	}

	/**
	 * TODO javadoc
	 */
	public boolean haveCategoryActivityBindingsChanged() {
		return categoryActivityBindingsChanged;
	}
}
