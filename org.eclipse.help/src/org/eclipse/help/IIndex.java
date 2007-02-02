/*******************************************************************************
 * Copyright (c) 2005, 2007 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Intel Corporation - initial API and implementation
 *     IBM Corporation - 122967 [Help] Remote help system
 *******************************************************************************/
package org.eclipse.help;

/**
 * IIndex represents metaphor of the book index. It contains index entries,
 * each of them is a keyword with references into related content. 
 * 
 * @since 3.2
 */
public interface IIndex extends IUAElement {
	
	/**
	 * Obtains the index entries contained in the index.
	 * 
	 * @return Array of IIndexEntry
	 */
	IIndexEntry[] getEntries();
}
