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
 * Extension interface for {@link org.eclipse.jface.text.ITextViewer}.
 * Introduces the concept of text presentation listeners and improves focus
 * handling among widget token keepers.
 * <p>
 * A {@link org.eclipse.jface.text.ITextPresentationListener}is a listener that
 * is informed by the viewer that a text presentation is about to be applied.
 * During this callback the listener is allowed to modify the presentation. Text
 * presentation listeners are thus a mean to participate in the process of text
 * presentation creation.
 *
 * @since 3.0
 */
public interface ITextViewerExtension4 {

	/**
	 * Instructs the receiver to request the {@link IWidgetTokenKeeper}
	 * currently holding the widget token to take the keyboard focus.
	 *
	 * @return <code>true</code> if there was any
	 *         <code>IWidgetTokenKeeper</code> that was asked to take the
	 *         focus, <code>false</code> otherwise
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
