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
 * A widget token must be aquired in order to display
 * information in a temporary window.  The intent behind this concept is that
 * only one temporary window should be presented at any moment in time and
 * also to avoid overlapping temporary windows.
 * 
 * @since 2.0
 */ 
public interface IWidgetTokenOwner {
	
	/**
	 * Requests the widget token from this token owner. Returns 
	 * <code>true</code> if the token has been aquired or is
	 * already owned by the requester. This method is non-blocking.
	 * 
	 * @param requester the token requester
	 * @return <code>true</code> if requester aquires the token,
	 * 	<code>false</code> otherwise
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
