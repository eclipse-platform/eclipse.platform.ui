package org.eclipse.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

/**
 * Describes the public attributes for a resource and the acceptables values
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
public interface IResourceActionFilter extends IActionFilter {
	/**
	 * An attribute indicating the file name (value <code>"name"</code>).  
	 * The attribute value in xml is unconstrained.  "*" may be used at the start or
	 * the end to represent "one or more characters".
	 */
	public static final String NAME = "name"; //$NON-NLS-1$
	
	/**
	 * An attribute indicating the file extension (value <code>"extension"</code>).
	 * The attribute value in xml is unconstrained.
	 */
	public static final String EXTENSION = "extension"; //$NON-NLS-1$

	/**
	 * An attribute indicating the file path (value <code>"path"</code>).
	 * The attribute value in xml is unconstrained.  "*" may be used at the start or
	 * the end to represent "one or more characters".
	 */
	public static final String PATH = "path"; //$NON-NLS-1$

	/**
	 * An attribute indicating whether the file is read only (value <code>"readOnly"</code>).
	 * The attribute value in xml must be one of <code>"true" or "false"</code>.
	 */
	public static final String READ_ONLY = "readOnly"; //$NON-NLS-1$

	/**
	 * An attribute indicating the project nature (value <code>"projectNature"</code>).
	 * The attribute value in xml is unconstrained.
	 */
	public static final String PROJECT_NATURE = "projectNature";	 //$NON-NLS-1$
	
	/**
	 * An attribute indicating a persistent property value (value <code>"persistentProperty"</code>).
	 * If the value is a simple string, then this simply tests for existence of the property on the resource.
	 * If it has the format <code>"propertyName=propertyValue" this obtains the value of the property
	 * with the specified name and tests it for equality with the specified value.
	 */
	public static final String PERSISTENT_PROPERTY = "persistentProperty";	 //$NON-NLS-1$
}
