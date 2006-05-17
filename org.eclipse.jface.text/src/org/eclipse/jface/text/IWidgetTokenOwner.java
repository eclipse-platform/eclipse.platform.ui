/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.jface.text;


/**
 * In order to display information in a temporary window, a widget token must be
 * acquired. The intent behind this concept is that only one temporary window
 * should be presented at any moment in time and also to avoid overlapping
 * temporary windows. This concept is used by the
 * {@link org.eclipse.jface.text.ITextViewer}.
 * <p>
 * In order to provide backward compatibility for clients of
 * <code>IWidgetTokenOwner</code>, extension interfaces are used as a means
 * of evolution. The following extension interfaces exist:
 * <ul>
 * <li>{@link org.eclipse.jface.text.IWidgetTokenOwnerExtension} since version
 * 3.0 introducing priorities when requesting a widget token and thus replacing
 * the non-prioritized scheme.</li>
 * </ul>
 *
 * @see org.eclipse.jface.text.IWidgetTokenOwnerExtension
 * @since 2.0
 */
public interface IWidgetTokenOwner {

	/**
	 * Requests the widget token from this token owner. Returns
	 * <code>true</code> if the token has been acquired or is already owned by
	 * the requester. This method is non-blocking.
	 * <p>
	 * Replaced by
	 * {@link IWidgetTokenOwnerExtension#requestWidgetToken(IWidgetTokenKeeper, int)}.
	 *
	 * @param requester the token requester
	 * @return <code>true</code> if requester acquires the token,
	 *         <code>false</code> otherwise
	 */
	boolean requestWidgetToken(IWidgetTokenKeeper requester);

	/**
	 * The given token keeper releases the token to this
	 * token owner. If the token has previously not been held
	 * by the given token keeper, nothing happens. This
	 * method is non-blocking.
	 *
	 * @param tokenKeeper the token keeper
	 */
	void releaseWidgetToken(IWidgetTokenKeeper tokenKeeper);
}
