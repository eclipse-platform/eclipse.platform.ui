/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.help.internal.toc;

import org.eclipse.help.internal.model.*;
/**
 * ITocNode interface.
 */
interface ITocNode extends INavigationElement {

	/**
	 * Adds another element as child of this element Modifies parents of a child
	 * as well
	 * 
	 * @param child
	 *            node to add as child
	 */
	void addChild(ITocNode child);

	/**
	 * When a builder builds the navigation, each node must "accomodate" the
	 * builder by responding to the build() command.
	 */
	void build(TocBuilder builder);
}
