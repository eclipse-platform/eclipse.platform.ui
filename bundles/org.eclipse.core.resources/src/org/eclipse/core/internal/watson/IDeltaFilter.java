/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.internal.watson;

/**
 * A DeltaFilter is used to allow the user of an ElementTreeDelta
 * to navigate through the delta in a selective manner.
 */
public interface IDeltaFilter {
/**
 * Returns true if the delta element with the given flag should be
 * included in the response to an ElementTreeDelta query, and false 
 * otherwise.  The flag is the integer set by the IElementComparator.
 *
 * @see IComparator.compare(Object, Object)
 */
public boolean includeElement(int flag);
}
