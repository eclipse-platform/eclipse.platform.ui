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
package org.eclipse.help.search;

import org.eclipse.help.IHelpResource;

/**
 * A search result created by the help search engine. Engines that
 * have direct access to the search servers are expected to
 * provide one search result object per each hit. Engines that
 * can only compose a search query that returns a document
 * with the results must create one serch result object whose
 * URL is the actual query.
 * @since 3.1
 */
public interface ISearchEngineResult {
	/**
	 * Returns the label of the search result to use in the UI.
	 * @return the search result label
	 */
	String getLabel();
	/**
	 * Returns the short description of the search result. If 
	 * coming from an individual search hit, this description is
	 * typically composed of the document fragment in the
	 * vicinity of the searched expression, or a combination
	 * of several document fragments. Other search engines
	 * may return couple of sententies at the beninning of the
	 * document.
	 * @return  a short description, or <code>null</code> if not
	 * available.
	 */
	String getDescription();
	/**
	 * Returns a category this search result belongs to. 
	 * Engines can use this object to denote the origin of the hit
	 * in the search domain. The usage of <code>IHelpResource</code>
	 * allows result UI to create a link to the category.
	 * @return a hit category or <code>null</code> if not available. 
	 */
	IHelpResource getCategory();
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
	 * @return the score of this hit between 0.0 and 1.0
	 */
	float getScore();
	/**
	 * Tests if the result link must be shown in an external web
	 * browser/Help window, or it can also be
	 * shown embedded. Contributors should force external
	 * window only if the link points at a web page that
	 * cannot be displayed in a narrow view without 
	 * distorsion. Text-only document typically reflow
	 * well to fit the narrow view. Documents with tables and/or
	 * images normally do not fit without showing the
	 * horizontal scroll bar.
	 * <p>
	 * Note that returning <code>false</code> does not
	 * prevent the link to be opened in the external window because
	 * the user may configure the help system to always
	 * open links in external windows.
	 * 
	 * @return <code>true</code> if external window must
	 * be used, <code>false</code> if the link opening mode
	 * can be chosen by the help system.
	 */
	boolean getForceExternalWindow();
}