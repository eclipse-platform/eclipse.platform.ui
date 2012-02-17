/*******************************************************************************
 * Copyright (c) 2005, 2012 IBM Corporation and others.
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
 * A completion listener is informed before the content assistant computes completion proposals.
 * <p>
 * In order to provide backward compatibility for clients of <code>ICompletionListener</code>, extension
 * interfaces are used to provide a means of evolution. The following extension interfaces exist:
 * <ul>
 *   <li>{@link org.eclipse.jface.text.contentassist.ICompletionListenerExtension} since version 3.4 introducing
 *		the following functions:
 *		<ul>
 *			<li>additional notification about restarting the current code assist session</li>
 *		</ul>
 *   </li>
 *   <li>{@link org.eclipse.jface.text.contentassist.ICompletionListenerExtension2} since version 3.8 introducing
 *		the following functions:
 *		<ul>
 *			<li>additional notification after applying a proposal</li>
 *		</ul>
 *   </li>
 * </ul>
 * </p>
 *
 * <p>
 * Clients may implement this interface.
 * </p>
 *
 * @since 3.2
 */
public interface ICompletionListener {

	/**
	 * Called when code assist is invoked when there is no current code assist session.
	 *
	 * @param event the content assist event
	 */
	void assistSessionStarted(ContentAssistEvent event);

	/**
	 * Called when a code assist session ends (for example, the proposal popup is closed).
	 *
	 * @param event the content assist event
	 */
	void assistSessionEnded(ContentAssistEvent event);

	/**
	 * Called when the selection in the proposal popup is changed or if the insert-mode changed.
	 *
	 * @param proposal the newly selected proposal, possibly <code>null</code>
	 * @param smartToggle <code>true</code> if the insert-mode toggle is being pressed,
	 *        <code>false</code> otherwise
	 */
	void selectionChanged(ICompletionProposal proposal, boolean smartToggle);
}