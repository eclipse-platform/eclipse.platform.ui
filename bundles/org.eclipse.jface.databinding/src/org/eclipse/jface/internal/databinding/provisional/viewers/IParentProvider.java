/*******************************************************************************
 * Copyright (c) 2006, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Stefan Xenos, IBM - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.internal.databinding.provisional.viewers;

/**
 * NON-API - Returns the parent of elements in a tree.
 * 
 * @since 1.1
 */
public interface IParentProvider {
	
    /**
     * TODO: comment me
     * 
     * @param child
     * @return TODO: comment
     */
    public Object getParent(Object child);
}
