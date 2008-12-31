/*******************************************************************************
 * Copyright (c) 2006, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.text.contentassist;

/**
 * Extends {@link org.eclipse.jface.text.contentassist.IContentAssistant} with the following
 * functions:
 * <ul>
 * <li>completion listeners</li>
 * <li>repeated invocation mode</li>
 * <li>a local status line for the completion popup</li>
 * <li>control over the behavior when no proposals are available</li>
 * </ul>
 *
 * @since 3.2
 */
public interface IContentAssistantExtension2 {

	/**
	 * Adds a completion listener that will be informed before proposals are computed.
	 *
	 * @param listener the listener
	 */
	public void addCompletionListener(ICompletionListener listener);

	/**
	 * Removes a completion listener.
	 *
	 * @param listener the listener to remove
	 */
	public void removeCompletionListener(ICompletionListener listener);

	/**
	 * Enables repeated invocation mode, which will trigger re-computation of the proposals when
	 * code assist is executed repeatedly. The default is no <code>false</code>.
	 *
	 * @param cycling <code>true</code> to enable repetition mode, <code>false</code> to disable
	 */
	public void setRepeatedInvocationMode(boolean cycling);

	/**
	 * Enables displaying an empty completion proposal pop-up. The default is not to show an empty
	 * list.
	 *
	 * @param showEmpty <code>true</code> to show empty lists
	 */
	public void setShowEmptyList(boolean showEmpty);

	/**
	 * Enables displaying a status line below the proposal popup. The default is not to show the
	 * status line. The contents of the status line may be set via {@link #setStatusMessage(String)}.
	 *
	 * @param show <code>true</code> to show a message line, <code>false</code> to not show one.
	 */
	public void setStatusLineVisible(boolean show);

	/**
	 * Sets the caption message displayed at the bottom of the completion proposal popup.
	 *
	 * @param message the message
	 */
	public void setStatusMessage(String message);

	/**
	 * Sets the text to be shown if no proposals are available and
	 * {@link #setShowEmptyList(boolean) empty lists} are displayed.
	 *
	 * @param message the text for the empty list
	 */
	public void setEmptyMessage(String message);
}
