/*******************************************************************************
 * Copyright (c) 2007, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.text.source;

import org.eclipse.jface.text.contentassist.IContentAssistant;


/**
 * Extension interface for {@link org.eclipse.jface.text.source.ISourceViewer}.
 * <p>
 * It introduces API to access a minimal set of content assistant APIs.</li>
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
