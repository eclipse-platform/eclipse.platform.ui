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

package org.eclipse.jface.text.source;

import org.eclipse.jface.text.quickassist.IQuickAssistAssistant;
import org.eclipse.jface.text.quickassist.IQuickAssistInvocationContext;

/**
 * Extension interface for {@link org.eclipse.jface.text.source.ISourceViewer}.<p>
 * It introduces the concept of a quick assist assistant and provides access
 * to the quick assist invocation context. It also gives access to any currently
 * showing annotation hover.</p>
 *
 * @see IQuickAssistAssistant
 * @see IQuickAssistInvocationContext
 * @since 3.2
 */
public interface ISourceViewerExtension3 {

	/**
	 * Returns this viewers quick assist assistant.
	 *
	 * @return the quick assist assistant or <code>null</code> if none is configured
	 * @since 3.2
	 */
	public IQuickAssistAssistant getQuickAssistAssistant();

	/**
	 * Returns this viewer's quick assist invocation context.
	 *
	 * @return the quick assist invocation context or <code>null</code> if none is available
	 */
	IQuickAssistInvocationContext getQuickAssistInvocationContext();

	/**
	 * Returns the currently displayed annotation hover if any, <code>null</code> otherwise.
	 *
	 * @return the currently displayed annotation hover or <code>null</code>
	 */
	IAnnotationHover getCurrentAnnotationHover();

}
