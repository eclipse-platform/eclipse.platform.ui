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
package org.eclipse.help.internal.dynamic;

import org.eclipse.help.Node;
import org.eclipse.help.UAContentFilter;

/*
 * The handler responsible for filtering elements. Filters can either be
 * an attribute of the element to filter, or any number of child filter
 * elements.
 */
public class FilterHandler extends NodeHandler {

	/* (non-Javadoc)
	 * @see org.eclipse.help.internal.dynamic.NodeHandler#handle(org.eclipse.help.Node, java.lang.String)
	 */
	public short handle(Node node, String id) {
		if (UAContentFilter.isFiltered(node)) {
			Node parent = node.getParent();
			if (parent != null) {
				parent.removeChild(node);
			}
			return HANDLED_SKIP;
		}
		return UNHANDLED;
	}
}
