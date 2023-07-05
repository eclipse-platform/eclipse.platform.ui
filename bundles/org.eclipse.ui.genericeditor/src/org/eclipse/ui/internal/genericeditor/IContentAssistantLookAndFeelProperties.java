/*******************************************************************************
 * Copyright (c) 2023 Avaloq Group AG (http://www.avaloq.com).
 * 
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 * 
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *  Andrew Lamb (Avaloq Group AG) - initial implementation
 *******************************************************************************/
package org.eclipse.ui.internal.genericeditor;

import org.eclipse.jface.text.contentassist.ICompletionProposalExtension;
import org.eclipse.jface.text.contentassist.ICompletionProposalExtension6;
import org.eclipse.jface.text.contentassist.IContentAssistant;

/**
 * Interface for setting the look and feel properties of a content assistant.
 * 
 * @since 1.3
 */
public interface IContentAssistantLookAndFeelProperties {
	/**
	 * Enables the content assistant's auto activation mode.
	 *
	 * @param enabled indicates whether auto activation is enabled or not
	 */
	void enableAutoActivation(boolean enabled);

	/**
	 * Enables the content assistant's auto insertion mode. If enabled, the content
	 * assistant inserts a proposal automatically if it is the only proposal. In the
	 * case of ambiguities, the user must make the choice.
	 *
	 * @param enabled indicates whether auto insertion is enabled or not
	 */
	void enableAutoInsert(boolean enabled);

	/**
	 * Sets the delay after which the content assistant is automatically invoked if
	 * the cursor is behind an auto activation character.
	 *
	 * @param delay the auto activation delay (as of 3.6 a negative argument will be
	 *              set to 0)
	 */
	void setAutoActivationDelay(int delay);

	/**
	 * Sets the proposal pop-ups' orientation. The following values may be used:
	 * <ul>
	 * <li>{@link IContentAssistant#PROPOSAL_OVERLAY PROPOSAL_OVERLAY}
	 * <p>
	 * proposal popup windows should overlay each other</li>
	 * <li>{@link IContentAssistant#PROPOSAL_REMOVE PROPOSAL_REMOVE}
	 * <p>
	 * any currently shown proposal popup should be closed</li>
	 * <li>{@link IContentAssistant#PROPOSAL_STACKED PROPOSAL_STACKED}
	 * <p>
	 * proposal popup windows should be vertical stacked, with no overlap, beneath
	 * the line containing the current cursor location</li>
	 * </ul>
	 *
	 * @param orientation the popup's orientation
	 */
	void setProposalPopupOrientation(int orientation);

	/**
	 * Sets the context information popup's orientation. The following values may be
	 * used:
	 * <ul>
	 * <li>{@link IContentAssistant#CONTEXT_INFO_ABOVE CONTEXT_INFO_ABOVE}
	 * <p>
	 * context information popup should always appear above the line containing the
	 * current cursor location</li>
	 * <li>{@link IContentAssistant#CONTEXT_INFO_BELOW CONTEXT_INFO_BELOW}
	 * <p>
	 * context information popup should always appear below the line containing the
	 * current cursor location</li>
	 * </ul>
	 *
	 * @param orientation the popup's orientation
	 */
	void setContextInformationPopupOrientation(int orientation);

	/**
	 * Enables displaying an empty completion proposal pop-up. The default is not to
	 * show an empty list.
	 *
	 * @param showEmpty <code>true</code> to show empty lists
	 */
	void setShowEmptyList(boolean showEmpty);

	/**
	 * Enables displaying a status line below the proposal popup. The default is not
	 * to show the status line. The contents of the status line may be set via
	 * {@link #setStatusMessage(String)}.
	 *
	 * @param show <code>true</code> to show a message line, <code>false</code> to
	 *             not show one.
	 */
	void setStatusLineVisible(boolean show);

	/**
	 * Enables the support for colored labels in the proposal popup.
	 * <p>
	 * Completion proposals can implement {@link ICompletionProposalExtension6} to
	 * provide colored proposal labels.
	 * </p>
	 *
	 * @param isEnabled if <code>true</code> the support for colored labels is
	 *                  enabled in the proposal popup
	 */
	void enableColoredLabels(boolean isEnabled);

	/**
	 * Set whether completion trigger chars are enabled. If set to false, completion
	 * proposal trigger chars are ignored and only Enter key can be used to select a
	 * proposal.
	 *
	 * @param enable whether current content assistant should consider completion
	 *               trigger chars.
	 * @see ICompletionProposalExtension#getTriggerCharacters()
	 */
	void enableCompletionProposalTriggerChars(boolean enable);

	/**
	 * Sets whether this completion list is shown on each valid character which is
	 * either a letter or digit. This only applies to asynchronous content
	 * assistants.
	 *
	 * @param enable whether or not to enable this feature
	 */
	void enableAutoActivateCompletionOnType(boolean enable);
}
