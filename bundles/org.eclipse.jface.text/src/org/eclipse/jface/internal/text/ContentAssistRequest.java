/*******************************************************************************
 * Copyright (c) 2023 Dawid Pakuła and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Dawid Pakuła - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.internal.text;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.IContentAssistRequest;
import org.eclipse.jface.text.contentassist.IContentAssistantExtension2;

/**
 * Describes the state that the content assistant is in when completing proposals.
 *
 * @since 3.24
 */
public final class ContentAssistRequest implements IContentAssistRequest {
	/**
	 * Creates a new event.
	 *
	 * @param viewer text viewer
	 * @param offset assist offset
	 * @param autoActivated whether content assist was triggered by auto activation
	 * @param incremental incremental flag {@link IContentAssistantExtension2}
	 */
	public ContentAssistRequest(ITextViewer viewer, int offset, boolean autoActivated, boolean incremental) {
		this.fViewer= viewer;
		this.fOffset= offset;
		this.fAutoActivated= autoActivated;
		this.fIncremental= incremental;
	}

	/**
	 * the viewer whose document is used to compute the proposals
	 */
	private ITextViewer fViewer;

	/**
	 * an offset within the document for which completions should be computed
	 */
	private int fOffset;

	/**
	 * Tells, whether content assist was triggered by auto activation.
	 */
	private boolean fAutoActivated;

	/**
	 * Tells, whether content assist was triggered incrementally
	 */
	private boolean fIncremental;


	@Override
	public ITextViewer getViewer() {
		return fViewer;
	}

	@Override
	public IDocument getDocument() {
		return fViewer.getDocument();
	}

	@Override
	public int getOffset() {
		return fOffset;
	}

	@Override
	public boolean isAutoActivated() {
		return fAutoActivated;
	}

	@Override
	public boolean isIncremental() {
		return fIncremental;
	}


}
