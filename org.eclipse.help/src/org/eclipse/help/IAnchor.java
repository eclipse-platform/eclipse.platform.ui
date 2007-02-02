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
 * Specifies a location in a document where other plug-ins may contribute
 * additional content. Extenders must specify a content extension and reference
 * this anchor to contribute content.
 * 
 * @since 3.3
 */
public interface IAnchor extends IUAElement {

	/**
	 * Returns the anchor's id. This id must be unique within this document.
	 * 
	 * @return the anchor id
	 */
	public String getId();
}
