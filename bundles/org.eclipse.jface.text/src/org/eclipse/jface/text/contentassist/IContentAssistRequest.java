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
package org.eclipse.jface.text.contentassist;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;

/**
 * Describes the state that the content assistant is in when completing proposals.
 * <p>
 * Clients may use this class.
 * </p>
 *
 * @since 3.24
 * @noimplements
 */
public interface IContentAssistRequest {

	/**
	 * @return the viewer whose document is used to compute the proposals
	 */
	public ITextViewer getViewer();

	/**
	 * @return the document connect to viewer
	 */
	public IDocument getDocument();

	/**
	 * @return document offset
	 */
	public int getOffset();

	/**
	 * @return flag for auto-assist
	 */
	public boolean isAutoActivated();

	/**
	 * @return flag for incremental calls {@link IContentAssistantExtension2}
	 */
	public boolean isIncremental();

}
