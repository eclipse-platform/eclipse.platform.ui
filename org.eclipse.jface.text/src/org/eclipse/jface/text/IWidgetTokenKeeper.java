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
 * A widget token keeper may require a widget token from an
 * <code>IWidgetTokenOwner</code> and release the token
 * to the owner after usage. A widget token owner may request
 * the token from the token keeper. The keeper may deny that.
 * 
 * @since 2.0
 */ 
public interface IWidgetTokenKeeper {
	
	/**
	 * The given widget token owner requests the widget token  from 
	 * this token keeper. Returns  <code>true</code> if the token is released
	 * by this token keeper. Note, the keeper must not call 
	 * <code>releaseWidgetToken(IWidgetTokenKeeper)</code> explicitly.
	 * 
	 * @param owner the token owner
	 * @return <code>true</code> if token has been released <code>false</code> otherwise
	 */
	boolean requestWidgetToken(IWidgetTokenOwner owner);
}
