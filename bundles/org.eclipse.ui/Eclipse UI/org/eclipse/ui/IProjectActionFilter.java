package org.eclipse.ui;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */

/**
 * Describes the public attributes for a project and the acceptables values
 * each may have.  
 * <p>
 * A popup menu extension may use these constants to describe its object target.  
 * Each identifies an attribute name or possible value.  
 * <p>
 * Clients are not expected to implement this interface.
 * </p>
 *
 * @see IActionFilter
 */
public interface IProjectActionFilter extends IResourceActionFilter {
	
	/**
	 * An attribute indicating the project nature (value <code>"nature"</code>).
	 * The attribute value in xml is unconstrained.
	 */
	public static final String NATURE = "nature";


	/**
	 * An attribute indicating whether the project is open (value <code>"open"</code>).
	 * The attribute value in xml must be one of <code>"true" or "false"</code>.
	 */
	public static final String OPEN = "open";

}
