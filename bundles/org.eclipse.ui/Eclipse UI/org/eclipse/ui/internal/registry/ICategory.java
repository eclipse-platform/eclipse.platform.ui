package org.eclipse.ui.internal.registry;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */
/**
 * Category provides for hierarchical grouping
 * of elements registered in the registry. 
 * One extension normally defines a category,
 * and other reference it via its ID.
 * Category may specify its parent
 * category in order to achieve hierarchy.
 * <p>
 * [Issue: This interface is not exposed in API, but time may
 * demonstrate that it should be.  For the short term leave it be.
 * In the long term its use should be re-evaluated. ]
 * </p>
 */
public interface ICategory {
/**
 * Returns a unique category ID.
 */

public String getID();
/**
 * Returns a presentation label for this category.
 */

public String getLabel();
/**
 * Returns an array of tokens that represent
 * a path to the parent category of this category.
 * If this is top-level category, null is returned.
 */

public String [] getParentCategoryPath();
}
