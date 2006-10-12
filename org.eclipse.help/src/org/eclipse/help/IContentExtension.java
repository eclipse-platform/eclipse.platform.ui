/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.help;

/**
 * An <code>IContentExtension</code> is an extension or a modification on
 * a user assistance document or part of a document. Different extension
 * types provide different ways to modify or add to a document.
 * 
 * @since 3.3
 */
public interface IContentExtension {

	/**
	 * The extension type for anchor contributions, where a content fragment
	 * is added into a target anchor identified by a document id and anchor id.
	 */
	public static final int CONTRIBUTION = 0;
	
	/**
	 * The extension type for content replacement, where one element of a
	 * document tree is replaced entirely with an element from another document.
	 */
	public static final int REPLACE = 1;
	
	/**
	 * Returns a string identifying the content to be used for modifying the
	 * document (i.e. what to insert or replace with). For XML files in
	 * bundles this has the form <code>/pluginId/path/file.xml#elementId</code>.
	 * There is also a shorthand form: <code>/pluginId/path/file.html</code>
	 * which will match the contents of the <code>body</code> element of an HTML
	 * or XHTML document.
	 * 
	 * @return the path to the content to use for the extension
	 */
	public String getContent();
	
	/**
	 * Returns a string identifying the target element to be affected by this
	 * extension. For example, for XML files in bundles this has the form
	 * <code>/pluginId/path/file.xml#elementId</code>.
	 * 
	 * @return the path to the element to be extended
	 */
	public String getPath();
	
	/**
	 * Returns the extension type. Must be either <code>CONTRIBUTION</code>
	 * or <code>REPLACE</code>.
	 * 
	 * @return the extension type
	 */
	public int getType();
}
