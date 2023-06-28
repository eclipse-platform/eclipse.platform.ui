/*******************************************************************************
 * Copyright (c) 2007, 2008 IBM Corporation and others.
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
package org.eclipse.jface.text.source;

import org.eclipse.jface.text.contentassist.IContentAssistant;


/**
 * Extension interface for {@link org.eclipse.jface.text.source.ISourceViewer}.
 * <p>
 * It introduces API to access a minimal set of content assistant APIs.
 * </p>
 *
 * @see IContentAssistant
 * @since 3.4
 */
public interface ISourceViewerExtension4 {

	/**
	 * Returns a facade for this viewer's content assistant.
	 *
	 * @return a content assistant facade or <code>null</code> if none is
	 *         configured
	 */
	public ContentAssistantFacade getContentAssistantFacade();

}
