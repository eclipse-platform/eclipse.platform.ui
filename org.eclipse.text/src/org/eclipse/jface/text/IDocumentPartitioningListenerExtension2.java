/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
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
package org.eclipse.jface.text;

/**
 * Extension interface to
 * {@link org.eclipse.jface.text.IDocumentPartitioningListener}.
 * <p>
 *
 * Replaces the previous notification mechanisms by introducing an explicit
 * document partitioning changed event.
 *
 * @see org.eclipse.jface.text.DocumentPartitioningChangedEvent
 * @since 3.0
 */
public interface IDocumentPartitioningListenerExtension2 {

	/**
	 * Signals the change of document partitionings.
	 * <p>
	 * This method replaces
	 * {@link IDocumentPartitioningListener#documentPartitioningChanged(IDocument)}
	 * and
	 * {@link IDocumentPartitioningListenerExtension#documentPartitioningChanged(IDocument, IRegion)}
	 *
	 * @param event the event describing the change
	 * @see IDocument#addDocumentPartitioningListener(IDocumentPartitioningListener)
	 */
	void documentPartitioningChanged(DocumentPartitioningChangedEvent event);
}
