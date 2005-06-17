/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.jface.text;

/**
 * Extension interface for {@link org.eclipse.jface.text.IDocumentAdapter}.
 * <p>
 * Introduces the concepts of batching a series of document changes into a
 * single styled text content change notification. Batching start when a client
 * calls <code>stopForwardingDocumentChanges</code>. After that call this
 * document adapter does not send out styled text content change notifications
 * until <code>resumeForwardingDocumentChanges</code> is called. On
 * <code>resumeForwardingDocumentChanges</code>, it sends out one styled text
 * content change notification that covers all changes that have been applied to
 * the document since calling <code>stopForwardingDocumentChanges</code>.
 *
 * @since 2.0
 */
public interface IDocumentAdapterExtension {

	/**
	 * Stops forwarding document changes to the styled text.
	 */
	void stopForwardingDocumentChanges();

	/**
	 * Resumes forwarding document changes to the styled text.
	 * Also forces the styled text to catch up with all the changes
	 * that have been applied since <code>stopForwardingDocumentChanges</code>
	 * has been called.
	 */
	void resumeForwardingDocumentChanges();
}
