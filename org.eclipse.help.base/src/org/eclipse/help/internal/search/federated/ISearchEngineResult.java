/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.help.internal.search.federated;

/**
 * A search result in the federated search.
 */
public interface ISearchEngineResult {
	/**
	 * Returns the label of the search result.
	 * @return
	 */
	String getLabel();
	/**
	 * Returns the short description of the search result (if available), or 
	 * <code>null</code> otherwise.
	 * @return
	 */
	String getDescription();
	/**
	 * Returns the URL (as a string) associated with this help resource.
	 * 
	 * @return the URL (as a string) associated with the resource
	 *         <p>
	 *         Valid URL of a help resource is:
	 *         <ul>
	 *         <li>a <em>/pluginID/path/to/resource</em>, where
	 *         <ul>
	 *         <li><em>pluginID</em> is the unique identifier of the plugin
	 *         containing the help resource,
	 *         <li><em>path/to/document</em> is the help resource path,
	 *         relative to the plugin directory.
	 *         </ul>
	 *         For example. <em>/myplugin/mytoc.xml</em> or
	 *         <em>/myplugin/references/myclass.html</em> are vaild.
	 *         <li>string representation of URI to an external document. In
	 *         this case, all special characters have to be enoded such that the
	 *         URI is appropriate to be opened with a web browser.
	 *         <em>http://eclipse.org/documents/my%20file.html</em> and
	 *         <em>jar:file:/c:/my%20sources/src.zip!/mypackage/MyClass.html</em>
	 *         are examples of valid URIs.
	 *         </ul>
	 *         </p>
	 */
	String getHref();
	/**
	 * Returns a float number in the range between 0 and 1 that can be used to
	 * sort the hits by relevance (1 being the perfect result). The number can
     * be interpreted as the probability of a match in the given topic.
	 * @return
	 */
	float getScore();
}
