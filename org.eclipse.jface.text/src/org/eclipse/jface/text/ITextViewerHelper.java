/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.text;


/**
 * A text viewer helper is a tool that modifies the viewer contents or shows
 * information about the content in a popup. It provides clients with
 * information about its state.
 * <p>
 * Clients may ask a helper whether it is currently displaying a shell that has
 * focus, and whether document changes triggered by the helper share a common
 * subject region. Depending on the answers to these queries, clients may decide
 * to react differently to incoming notifications about events. For example, a
 * special editing mode that is usually deactivated when the main shell looses
 * focus may decide to not deactivate if the focus event was triggered by an
 * editor helper.
 * </p>
 * <p>
 * XXX This interface is subject to change. Do not use.
 * </p>
 * 
 * @since 3.1
 */
public interface ITextViewerHelper {
	/**
	 * Checks whether the given region contains the subject region of the
	 * helper.
	 * 
	 * @param region the region to check
	 * @return <code>true</code> if modifications performed by the receiver
	 *         relate to <code>region</code>
	 */
	boolean isValidSubjectRegion(IRegion region);
	/**
	 * Returns <code>true</code> if the helper's shell has focus,
	 * <code>false</code> if it does not have focus or the helper has no
	 * shell.
	 * 
	 * @return <code>true</code> if the helper's shell has focus,
	 *         <code>false</code> otherwise
	 */
	boolean hasShellFocus();
}