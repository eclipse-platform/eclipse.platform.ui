/*******************************************************************************
 * Copyright (c) 2003, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.progress;

import org.eclipse.swt.graphics.Image;

/**
 * The JobTreeElement is the abstract superclass of items displayed in the tree.
 */
abstract class JobTreeElement implements Comparable {

	/**
	 * Return the parent of this object.
	 * 
	 * @return Object
	 */
	abstract Object getParent();

	/**
	 * Return whether or not the receiver has children.
	 * 
	 * @return boolean
	 */
	abstract boolean hasChildren();

	/**
	 * Return the children of the receiver.
	 * 
	 * @return Object[]
	 */
	abstract Object[] getChildren();

	/**
	 * Return the displayString for the receiver.
	 * 
	 * @return String
	 */
	abstract String getDisplayString();

	/**
	 * Return the displayString for the receiver.
	 * 
	 * @param showProgress
	 *            Whether or not progress is being shown (if relevant).
	 * @return String
	 */
	String getDisplayString(boolean showProgress) {
		return getDisplayString();
	}

	/**
	 * Get the image for the reciever. By default there is no image.
	 * 
	 * @return Image or <code>null</code>.
	 */
	public Image getDisplayImage() {
		return null;
	}

	/**
	 * Return the condensed version of the display string
	 * 
	 * @return String
	 */
	String getCondensedDisplayString() {
		return getDisplayString();
	}

	/**
	 * Return whether or not the receiver is an info.
	 * 
	 * @return boolean
	 */
	abstract boolean isJobInfo();

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(Object arg0) {
		if (arg0 instanceof JobTreeElement)
			return getDisplayString().compareTo(
					((JobTreeElement) arg0).getDisplayString());
		return 0;
	}

	/**
	 * Return whether or not this is currently active.
	 * 
	 * @return boolean
	 */
	abstract boolean isActive();

	/**
	 * Return whether or not the receiver can be cancelled.
	 * 
	 * @return boolean
	 */
	public boolean isCancellable() {
		return false;
	}

	/**
	 * Cancel the receiver.
	 */
	public void cancel() {
		// By default do nothing.
	}
}
