package org.eclipse.update.core;
/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */

import org.eclipse.update.core.model.CategoryModel;

/**
 * Convenience implementation of feature category definition.
 * <p>
 * This class may be instantiated or subclassed by clients.
 * </p> 
 * @see org.eclipse.update.core.ICategory
 * @see org.eclipse.update.core.model.CategoryModel
 * @since 2.0
 */
public class Category extends CategoryModel implements ICategory {

	/**
	 * Default Constructor
	 */
	public Category() {
	}

	/**
	 * Constructor
	 */
	public Category(String name, String label) {
		setName(name);
		setLabel(label);
	}

	/**
	 * Retrieve the detailed category description
	 * @see ICategory#getDescription()
	 */
	public IURLEntry getDescription() {
		return (IURLEntry) getDescriptionModel();
	}
}