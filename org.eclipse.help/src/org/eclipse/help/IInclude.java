/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
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
 * A directive indicating the content at the given path should be included in
 * this document, and replace this node.
 * 
 * @since 3.3
 */
public interface IInclude extends IUAElement {

	/**
	 * An href pointing to a document element. The href has the form
	 * <code>/plugin_id/path/file.ext#anchorId</code>. For example, to include
	 * the element with id <code>myId</code> in the XML file
	 * <code>files/myFile.xml</code> under plug-in <code>my.plugin</code>, the
	 * path is <code>/my.plugin/files/myFile.xml#myId</code>.
	 * 
	 * @return the path of the element to include
	 */
	public String getPath();
}
