/*
 * (c) Copyright IBM Corp. 2000, 2001.
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

	public final static String HREF = "href";
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
