package org.eclipse.update.core;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

/**
 * a Category is used by the User Interface to categorize Features.
 * They are declared in the <code>site.xml</code> file of the Site.
 * The association between a DefaultFeature and a category is also done
 * In this file.
 * 
 * <p>
 * a Category name is of the form:
 * <code> ParentCategory1/ParentCategory2/Category </code>
 * where <code> ParentCategory1 </code> and <code> ParentCategory2 </code>
 * are defined Category.
 * </p>
 * 
 * <p>
 * The label of a category can be translated in the <code>site.properties</code>
 * file.
 * </p>
 * 
 * <p>
 * This interface is not intended to be implemented by clients.
 * </p>
 * 
 * @see DefaultCategory
 */


public interface ICategory {
	
	/** 
	 * The name or identifier of the category.
	 * The identifier is of the form:
	 * <code> ParentCategory1/ParentCategory2/Category </code>
	 * where <code> ParentCategory1 </code> and <code> ParentCategory2 </code>
	 * are defined Category.
	 * 
	 * @return the identifier of the category
	 * @since 2.0 
	 */

	String getName();
	
	/**
	 * returns the translated Label of this category.
	 * @return the translated, user-friendly, name of the category.
	 * @since 2.0 
	 */

	String getLabel();
	
	/** 
	 * returns the description associated with the category
	 * @return teh description of the category
	 * @since 2.0 
	 */

	IURLEntry getDescription();
}

