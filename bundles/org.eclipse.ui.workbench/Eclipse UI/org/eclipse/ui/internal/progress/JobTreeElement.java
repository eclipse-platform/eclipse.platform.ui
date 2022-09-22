/*******************************************************************************
 * Copyright (c) 2003, 2022 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.progress;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.graphics.Image;

/**
 * The JobTreeElement is the abstract superclass of items displayed in the tree.
 */
public abstract class JobTreeElement {
	/**
	 * Returns the parent of this object.
	 *
	 * @return JobTreeElement
	 */
	public JobTreeElement getParent() {
		return null;
	}

	/**
	 * Returns whether or not the receiver has children.
	 *
	 * @return boolean
	 */
	abstract boolean hasChildren();

	/**
	 * Returns the children of the receiver.
	 *
	 * @return Object[]
	 */
	abstract Object[] getChildren();

	/**
	 * Returns the displayString for the receiver.
	 *
	 * @return String
	 */
	abstract String getDisplayString();

	/**
	 * Returns the displayString for the receiver.
	 *
	 * @param showProgress Whether or not progress is being shown (if relevant).
	 * @return String
	 */
	String getDisplayString(boolean showProgress) {
		return getDisplayString();
	}

	/**
	 * Returns the image for the receiver.
	 *
	 * @return Image or <code>null</code>.
	 */
	public Image getDisplayImage() {
		return JFaceResources.getImage(ProgressInfoItem.DEFAULT_JOB_KEY);
	}

	/**
	 * Returns the condensed version of the display string.
	 *
	 * @return String
	 */
	String getCondensedDisplayString() {
		return getDisplayString();
	}

	/**
	 * Returns whether or not the receiver is an info.
	 *
	 * @return boolean
	 */
	abstract boolean isJobInfo();

	/**
	 * Returns whether or not this is currently active.
	 *
	 * @return boolean
	 */
	abstract boolean isActive();

	/**
	 * Returns whether or not the receiver can be cancelled.
	 *
	 * @return boolean
	 */
	public boolean isCancellable() {
		return false;
	}

	/**
	 * Cancels the receiver.
	 */
	public void cancel() {
		// By default do nothing.
	}
}
