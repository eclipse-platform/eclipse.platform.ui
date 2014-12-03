/*******************************************************************************
 * Copyright (c) 2014 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 * *******************************************************************************/
package org.eclipse.ui.console;


/**
 * Scroll lock provider for the text console viewer.
 * <p>
 * This interface allows a text console viewer to control the scroll lock state of its container, e.g. a view.
 * </p>
 * <p>
 * Clients may implement this interface.
 * </p>
 *
 * @since 3.6
 */
public interface IScrollLockStateProvider {

	/**
	 * Sets the scroll lock state.
	 *
	 * @param scrollLock <code>true</code> to turn scroll lock on, otherwise
	 *            <code>false</code>
	 */
	public void setScrollLock(boolean scrollLock);

	/**
	 * Returns the scroll lock state.
	 *
	 * @return <code>true</code> if scroll lock is on, <code>false</code>
	 *         otherwise
	 */
	public boolean getScrollLock();
}
