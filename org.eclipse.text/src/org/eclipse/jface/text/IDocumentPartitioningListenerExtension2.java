/**********************************************************************
Copyright (c) 2000, 2003 IBM Corp. and others.
All rights reserved. This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html

Contributors:
	IBM Corporation - Initial implementation
**********************************************************************/
package org.eclipse.jface.text;

/**
 * Extension interface to <code>DocumentPartitioningListener</code>. Replaces the previous concepts.
 * 
 * @since 3.0
 */
public interface IDocumentPartitioningListenerExtension2 {

	/**
	 * Signals the change of document partitionings.
	 *
	 * @param event the event describing the change
	 * @see IDocument#addDocumentPartitioningListener(IDocumentPartitioningListener)
	 */
	void documentPartitioningChanged(DocumentPartitioningChangedEvent event);
}
