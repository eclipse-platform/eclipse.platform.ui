/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
package org.eclipse.help;
/**
* A help resource, usually a help topic.
 * <p>
 * This interface models a help resource. In general, help resources are either
 * html help files, or xml navigation/topics files.
 * </p>
 */
public interface IHelpResource {

	public final static String HREF = "href";
	public final static String LABEL = "label";
	
	/**
	 * Obtains href
	 */
	public String getHref();
	/**
	 * Obtains label
	 */
	public String getLabel();
}