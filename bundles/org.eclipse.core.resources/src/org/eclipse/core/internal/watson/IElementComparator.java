/**********************************************************************
 * Copyright (c) 2000,2002 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.core.internal.watson;

import org.eclipse.core.internal.dtree.IComparator;
import org.eclipse.core.internal.dtree.NodeComparison;
/**
 * This interface allows clients of the element tree to specify
 * how element infos are compared, and thus how element tree deltas
 * are created.
 */
public interface IElementComparator extends IComparator {
	/**
	 * The kinds of changes
	 */
	public int K_NO_CHANGE = 0;
}
