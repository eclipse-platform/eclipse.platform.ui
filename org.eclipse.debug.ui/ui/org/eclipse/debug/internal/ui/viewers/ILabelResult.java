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
package org.eclipse.debug.internal.ui.viewers;

import org.eclipse.jface.viewers.TreePath;
import org.eclipse.swt.graphics.Image;


/**
 * Results of collecting an asynchronous label in a tree. 
 * 
 * @since 3.2
 */
public interface ILabelResult {

	/**
	 * Returns the labels for the element. One for each column.
	 * 
	 * @return
	 */
	public String[] getLabels();
	
	/**
	 * Returns the images for the element.
	 * 
	 * @return
	 */
	public Image[] getImages();
	
	/**
	 * Returns the element the label is for.
	 * 
	 * @return
	 */
	public Object getElement();
	
	/**
	 * Returns the path to the element in the tree.
	 * 
	 * @return
	 */
	public TreePath getTreePath();
	
	/**
	 * Returns this element's depth in the tree.
	 * 
	 * @return
	 */
	public int getDepth();
}
