/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
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
 * Extension interface for {@link org.eclipse.jface.text.IWidgetTokenOwner}.
 * <p>
 * Replaces the original <code>requestWidgetToken</code> functionality with a
 * new priority based approach.
 *
 * @since 3.0
 */
public interface IWidgetTokenOwnerExtension {

	/**
	 * Requests the widget token from this token owner. Returns
	 * <code>true</code> if the token has been acquired or is
	 * already owned by the requester. This method is non-blocking.
	 *
	 * <p><code>priority</code> is forwarded to any existing token keeper
	 * to give it an estimate on whether the request has higher priority than
	 * the current keeper's. There is, however, no guarantee that another keeper
	 * will release the token even if it has a high priority.</p>
	 *
	 * @param requester the token requester
	 * @param priority the priority of the request
	 * @return <code>true</code> if requester acquires the token,
	 * 	<code>false</code> otherwise
	 */
	boolean requestWidgetToken(IWidgetTokenKeeper requester, int priority);
}
