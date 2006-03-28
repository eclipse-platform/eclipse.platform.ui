/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
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
 * Describes the state that the content assistant is in when completing proposals.
 * <p>
 * Clients may use this class.
 * </p>
 * 
 * @since 3.2
 * @see ICompletionListener
 */
public final class ContentAssistEvent {
	/**
	 * Creates a new event.
	 * 
	 * @param ca the assistant
	 * @param proc the processor
	 */
	ContentAssistEvent(IContentAssistant ca, IContentAssistProcessor proc) {
		assistant= ca;
		processor= proc;
	}

	/**
	 * The content assistant computing proposals.
	 */
	public final IContentAssistant assistant;
	/**
	 * The processor for the current partition.
	 */
	public final IContentAssistProcessor processor;
}