/*******************************************************************************
 * Copyright (c) 2006, 2008 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.texteditor.spelling;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContextInformation;

/**
 * Proposal telling that there are no proposals available.
 * <p>
 * Applying this proposal does nothing.
 * </p>
 *
 * @since 3.3
 */
public final class NoCompletionsProposal implements ICompletionProposal {

	@Override
	public void apply(IDocument document) {
		// do nothing
	}

	@Override
	public String getAdditionalProposalInfo() {
		return null;
	}

	@Override
	public IContextInformation getContextInformation() {
		return null;
	}

	@Override
	public String getDisplayString() {
			return SpellingMessages.NoCompletionsProposal_displayString;
		}

	@Override
	public Image getImage() {
		return null;
	}

	@Override
	public Point getSelection(IDocument document) {
		return null;
	}

}
