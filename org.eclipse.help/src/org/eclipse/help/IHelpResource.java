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
	 * @return the string URL associated with the resource
	 * <p>
	 * Valid URL of a help resource is:
	 * <ul>
	 *  <li>a <em>/pluginID/path/to/resource</em>, where
	 *  <ul>
	 *   <li><em>pluginID</em> is the unique identifier
	 *   of the plugin containing the help resource, 
	 *   <li><em>path/to/document</em> is the help resource path,
	 *   relative to the plugin directory.
	 *  </ul>
	 *  For example. <em>/myplugin/mytoc.xml</em>
	 *  or <em>/myplugin/references/myclass.html</em>
	 *  are vaild.
	 *   <li>string representation of URL to an external document.
	 *   In this case, all special characters have to be enoded
	 *    such that the URL is appropriate to be opened with a web browser.
	 *    For example <em>http://eclipse.org/documents/my%20file.html</em> is valid.
	 *  </ul>
	 * </p>
	 */
	public String getHref();
	/**
	 * Returns the label of this help resource.
	 *
	 * @return the label
	 */
	public String getLabel();
}
