/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
package org.eclipse.help;
/**
* A help resource, usually a help topic.
 * <p>
 * This interface models a help resource. In general, help resources are either
 * html help files, or xml TOC files.
 * </p>
 * @since 2.0
 */
public interface IHelpResource {
	
	/**
	 * This is attribute name used for href in XML files.
	 */
	public final static String HREF = "href";
	/**
	 * This is attribute name used for label in XML files.
	 */
	public final static String LABEL = "label";
	
	/**
	 * Returns the string URL associated with this help resource.
	 *
	 * @return the string URL of the resource
	 */
	public String getHref();
	/**
	 * Returns the label of this help resource.
	 *
	 * @return the label
	 */
	public String getLabel();
}
