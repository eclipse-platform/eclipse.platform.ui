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
 * An instance of this class describes changes to an instance of <code>ICategory</code>.
 * <p>
 * This class is not intended to be extended by clients.
 * </p>
 * 
 * @since 3.0
 * @see ICategoryListener#categoryChanged
 */
public final class CategoryEvent {
	private ICategory category;
	private boolean categoryActivityBindingsChanged;
	private boolean definedChanged;
	private boolean nameChanged;
    private boolean descriptionChanged;

	/**
	 * Creates a new instance of this class.
	 * 
	 * @param category
 *            the instance of the interface that changed.
	 * @param categoryActivityBindingsChanged
 *            true, iff the categoryActivityBindings property changed.
	 * @param definedChanged
 *            true, iff the defined property changed.
	 * @param nameChanged
 *            true, iff the name property changed.
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
		this.nameChanged = nameChanged;
		this.descriptionChanged = definedChanged;
	}

	/**
	 * Returns the instance of the interface that changed.
	 * 
	 * @return the instance of the interface that changed. Guaranteed not to be
	 *         <code>null</code>.
	 */
	public ICategory getCategory() {
		return category;
	}

	/**
	 * Returns whether or not the defined property changed.
	 * 
	 * @return true, iff the defined property changed.
	 */
	public boolean hasDefinedChanged() {
		return definedChanged;
	}

	/**
	 * Returns whether or not the name property changed.
	 * 
	 * @return true, iff the name property changed.
	 */
	public boolean hasNameChanged() {
		return nameChanged;
	}
	
	/**
	 * Returns whether or not the description property changed.
	 * 
	 * @return true, iff the description property changed.
	 */
	public boolean hasDescriptionChanged() {
		return nameChanged;
	}	

	/**
	 * Returns whether or not the categoryActivityBindings property changed.
	 * 
	 * @return true, iff the categoryActivityBindings property changed.
	 */
	public boolean haveCategoryActivityBindingsChanged() {
		return categoryActivityBindingsChanged;
	}
}
