package org.eclipse.update.core;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

/**
 * Feature category definition.
 * A site can organize its features into categories. Categories
 * can be further organized into hierarchies. Each category name
 * is a composed of the name of its parent and a simple identifier
 * separated by a slash ("/"). For example <code>tools/utilities/print</code>
 * defines a category that is a child of <code>tools/utilities</code> and
 * grandchild of <code>tools</code>.
 * <p>
 * Clients may implement this interface. However, in most cases clients should 
 * directly instantiate or subclass the provided implementation of this 
 * interface.
 * </p>
 * @see org.eclipse.update.core.Category
 * @since 2.0
 */
public interface ICategory {

	/** 
	 * Retrieve the name of the category. The name can be a simple
	 * token (root category) or a number of slash-separated ("/") 
	 * tokens.
	 * 
	 * @return the category name
	 * @since 2.0 
	 */
	String getName();

	/**
	 * Retrieve the displayable label for the category
	 * 
	 * @return displayable category label, or <code>null</code>
	 * @since 2.0 
	 */
	String getLabel();

	/** 
	 * Retrieve the detailed category description
	 * 
	 * @return category description, or <code>null</code>
	 * @since 2.0 
	 */
	IURLEntry getDescription();
}