/*
Copyright (c) 2000, 2001, 2002 IBM Corp.
All rights reserved.  This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
*/

package org.eclipse.jface.viewers;

import org.eclipse.swt.graphics.Color;

/**
 * Interface to provide color representation based on a specific object and 
 * index.
 */
public interface IIndexedColorProvider {

	/**
 	* Provides a foreground color based on a specified object and index.
 	* @param	element	the object for which to resolve the foreground color.
 	* @param	index 	the index for which to resolve the foreground color. 
 	* @return	the foreground color.
 	*/
	Color getForeground(Object element, int index);

	/**
 	* Provides a background color based on a specified object and index.
 	* @param	element	the object for which to resolve the background color.
 	* @param	index 	the index for which to resolve the background color. 
 	* @return	the background color.
 	*/
	Color getBackground(Object element, int index);
}
