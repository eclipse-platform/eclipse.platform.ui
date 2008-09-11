/*******************************************************************************
 * Copyright (c) 2005, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Anton Leherbauer (Wind River Systems) - [content assist][api] ContentAssistEvent should contain information about auto activation - https://bugs.eclipse.org/bugs/show_bug.cgi?id=193728
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
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
public final class ContentAssistEvent {
	/**
	 * Creates a new event.
	 *
	 * @param ca the assistant
	 * @param proc the processor
	 * @param isAutoActivated whether content assist was triggered by auto activation
	 * @since 3.4
	 */
	ContentAssistEvent(IContentAssistant ca, IContentAssistProcessor proc, boolean isAutoActivated) {
		assistant= ca;
		processor= proc;
		this.isAutoActivated= isAutoActivated;
	}

	/**
	 * Creates a new event.
	 *
	 * @param ca the assistant
	 * @param proc the processor
	 */
	ContentAssistEvent(ContentAssistant ca, IContentAssistProcessor proc) {
		this(ca, proc, false);
	}

	/**
	 * The content assistant computing proposals.
	 */
	public final IContentAssistant assistant;
	/**
	 * The processor for the current partition.
	 */
	public final IContentAssistProcessor processor;
	/**
	 * Tells, whether content assist was triggered by auto activation.
	 * <p>
	 * <strong>Note:</strong> This flag is only valid in {@link ICompletionListener#assistSessionStarted(ContentAssistEvent)}.
	 * </p>
	 *
	 * @since 3.4
	 */
	public final boolean isAutoActivated;
}