/*******************************************************************************
 * Copyright (c) 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.progress;

/**
 * The JobTreeElement is the abstract superclass of items
 * displayed in the tree.
 */
abstract class JobTreeElement implements Comparable{

	/**
	 * Return the parent of this object.
	 * @return Object
	 */
	abstract Object getParent();

	/**
	 * Return whether or not the receiver has children.
	 * @return boolean
	 */
	abstract boolean hasChildren();

	/**
	 * Return the children of the receiver.
	 * @return Object[]
	 */
	abstract Object[] getChildren();

	/**
	 * Return the displayString for the receiver.
	 * @return
	 */
	abstract String getDisplayString();
	
	/**
	 * Return whether or not the receiver is an info.
	 * @return boolean
	 */
	abstract boolean isJobInfo();

}
