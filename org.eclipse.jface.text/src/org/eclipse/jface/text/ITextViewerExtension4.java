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
 * Extension interface for <code>ITextViewer</code>. Adds the following functionality:
 * <ul>
 * <li>focus handling for widget token keepers</li>
 * <li>introduces text presentation listener</li>
 * </ul>
 * 
 * @since 3.0
 */
public interface ITextViewerExtension4 {
	
	/**
	 * Instructs the receiver to request the <code>IWidgetTokenKeeper</code>
	 * currently holding the widget token to take the keyboard focus. 
	 * 
	 * @return <code>true</code> if there was any <code>IWidgetTokenKeeper</code> that was asked to take the focus, <code>false</code> otherwise
	 */
	boolean moveFocusToWidgetToken();
	
	/**
	 * Adds the given text presentation listener to this text viewer.
	 * This call has no effect if the listener is already registered
	 * with this text viewer.
	 *
	 * @param listener the text presentation listener
	 */
	void addTextPresentationListener(ITextPresentationListener listener);
	
	/**
	 * Removes the given text presentation listener from this text viewer.
	 * This call has no effect if the listener is not registered with this
	 * text viewer.
	 *  
	 * @param listener the text presentation listener
	 */
	void removeTextPresentationListener(ITextPresentationListener listener);
}
