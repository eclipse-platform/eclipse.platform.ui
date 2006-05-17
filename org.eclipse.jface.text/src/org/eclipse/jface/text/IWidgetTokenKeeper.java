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
 * A widget token keeper may require a widget token from an
 * {@link org.eclipse.jface.text.IWidgetTokenOwner} and release the token to the
 * owner after usage. A widget token owner may request the token from the token
 * keeper. The keeper may deny the return of the token.
 * <p>
 * The widget token owner and keeper interplay is used by a text viewer in
 * order to manage the appearance and disappearance of addition, on-top popup
 * windows such as text hovers, content assist, etc.
 *
 * In order to provide backward compatibility for clients of
 * <code>IWidgetTokeKeeper</code>, extension interfaces are used as a means
 * of evolution. The following extension interfaces exist:
 * <ul>
 * <li>{@link org.eclipse.jface.text.IWidgetTokenKeeperExtension} since version
 *     3.0 introducing priorities when requesting a widget token and thus replacing
 *     the non-prioritized scheme. It also allows a client to force a widget token
 *     keeper to accept focus.</li>
 * </ul>
 *
 * @see org.eclipse.jface.text.IWidgetTokenKeeperExtension
 * @since 2.0
 */
public interface IWidgetTokenKeeper {

	/**
	 * The given widget token owner requests the widget token from this token
	 * keeper. Returns <code>true</code> if the token is released by this
	 * token keeper. Note, the keeper must not call
	 * <code>releaseWidgetToken(IWidgetTokenKeeper)</code> explicitly.
	 * <p>
	 * Replaced by
	 * {@link IWidgetTokenKeeperExtension#requestWidgetToken(IWidgetTokenOwner, int)}.
	 *
	 * @param owner the token owner
	 * @return <code>true</code> if token has been released <code>false</code>
	 *         otherwise
	 */
	boolean requestWidgetToken(IWidgetTokenOwner owner);
}
