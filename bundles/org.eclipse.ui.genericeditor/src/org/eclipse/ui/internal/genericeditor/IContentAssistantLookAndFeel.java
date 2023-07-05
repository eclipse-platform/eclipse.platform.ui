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

import org.eclipse.jface.text.contentassist.IContentAssistant;

/**
 * Interface to be implemented by contributions that want to set the look and
 * feel of a generic editor content assistant.
 * 
 * @since 1.3
 */
public interface IContentAssistantLookAndFeel {
	/**
	 * Apply this look and feel to the given content assistant.
	 * 
	 * @param assistant the content assistant to set the look and feel properties
	 *                  of.
	 */
	void applyTo(IContentAssistantLookAndFeelProperties assistant);

	static final IContentAssistantLookAndFeel DEFAULT = assistant -> {
		assistant.setContextInformationPopupOrientation(IContentAssistant.CONTEXT_INFO_BELOW);
		assistant.setProposalPopupOrientation(IContentAssistant.PROPOSAL_REMOVE);
		assistant.setAutoActivationDelay(10);
		assistant.enableColoredLabels(true);
		assistant.enableAutoActivation(true);
		assistant.enableAutoActivateCompletionOnType(true);
	};
}
