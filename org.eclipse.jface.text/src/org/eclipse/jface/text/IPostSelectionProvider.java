/**********************************************************************
Copyright (c) 2000, 2003 IBM Corp. and others.
All rights reserved. This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html

Contributors:
	IBM Corporation - Initial implementation
**********************************************************************/

package org.eclipse.jface.text;

import org.eclipse.jface.viewers.ISelectionChangedListener;

/**
 * Interface common to objects that provide an after-the-fact selection change notification/
 * 
 * @since 3.0
 */
public interface IPostSelectionProvider {

	/**
	 * Adds a post selection listener to this selection provider. If the listener
	 * is already registered this call does not have any effect.
	 *
	 * @param listener the post selection changed listener
	 */
	void addPostSelectionChangedListener(ISelectionChangedListener listener);
	
	/**
	 * Removes the given listener from this selection provider. If the listener
	 * is not registered this call has no effect.
	 *
	 * @param listener the post selection changed listener
	 */
	void removePostSelectionChangedListener(ISelectionChangedListener listener);
}
