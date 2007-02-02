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
 * A directive indicating a link to another toc to be inserted at (and replace)
 * this link.
 * 
 * @since 3.3
 */
public interface ILink extends IUAElement {

	/**
	 * Returns the unique id of the toc to link, e.g.
	 * "/org.eclipse.platform.doc.user/toc.xml"
	 * 
	 * @return the toc id to link
	 */
	public String getToc();
}
