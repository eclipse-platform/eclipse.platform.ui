/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.debug.internal.ui.console;


import java.util.ArrayList;
import java.util.List;

import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.jface.text.IDocumentPartitionerExtension;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.Region;
import org.eclipse.swt.widgets.Display;

/**
 * A console that displays text messages.
 * 
 * @since 3.0
 */
public class MessageConsolePartitioner implements IDocumentPartitioner, IDocumentPartitionerExtension {
	
	/**
	 * The associated docuemnt
	 */
	private IDocument fDocument = null;
			
	/**
	 * List of partitions
	 */
	private List fPartitions = new ArrayList(5);
		
	/**
	 * The stream that was last appended to
	 */
	private MessageConsoleStream fLastStream = null;
	
	/**
	 * @see org.eclipse.jface.text.IDocumentPartitioner#connect(org.eclipse.jface.text.IDocument)
	 */
	public void connect(IDocument document) {
		fDocument = document;
		document.setDocumentPartitioner(this);
	}

	/**
	 * @see org.eclipse.jface.text.IDocumentPartitioner#disconnect()
	 */
	public void disconnect() {
		fDocument.setDocumentPartitioner(null);
	}

	/**
	 * @see org.eclipse.jface.text.IDocumentPartitioner#documentAboutToBeChanged(org.eclipse.jface.text.DocumentEvent)
	 */
	public void documentAboutToBeChanged(DocumentEvent event) {
	}

	/**
	 * @see org.eclipse.jface.text.IDocumentPartitioner#documentChanged(org.eclipse.jface.text.DocumentEvent)
	 */
	public boolean documentChanged(DocumentEvent event) {
		return documentChanged2(event) != null;
	}

	/**
	 * @see org.eclipse.jface.text.IDocumentPartitioner#getLegalContentTypes()
	 */
	public String[] getLegalContentTypes() {
		return new String[] {MessageConsolePartition.MESSAGE_PARTITION_TYPE};
	}

	/**
	 * @see org.eclipse.jface.text.IDocumentPartitioner#getContentType(int)
	 */
	public String getContentType(int offset) {
		ITypedRegion partition = getPartition(offset);
		if (partition != null) {
			return partition.getType();
		}
		return null;
	}

	/**
	 * @see org.eclipse.jface.text.IDocumentPartitioner#computePartitioning(int, int)
	 */
	public ITypedRegion[] computePartitioning(int offset, int length) {
		if (offset == 0 && length == fDocument.getLength()) {
			return (ITypedRegion[])fPartitions.toArray(new ITypedRegion[fPartitions.size()]);
		} else {
			int end = offset + length;
			List list = new ArrayList();
			for (int i = 0; i < fPartitions.size(); i++) {
				ITypedRegion partition = (ITypedRegion)fPartitions.get(i);
				int partitionStart = partition.getOffset();
				int partitionEnd = partitionStart + partition.getLength();
				if ((offset >= partitionStart && offset <= partitionEnd) ||
					(offset < partitionStart && end >= partitionStart)) {
						list.add(partition);
				} 
			}
			return (ITypedRegion[])list.toArray(new ITypedRegion[list.size()]);
		}
	}

	/**
	 * @see org.eclipse.jface.text.IDocumentPartitioner#getPartition(int)
	 */
	public ITypedRegion getPartition(int offset) {
		for (int i = 0; i < fPartitions.size(); i++) {
			ITypedRegion partition = (ITypedRegion)fPartitions.get(i);
			int start = partition.getOffset();
			int end = start + partition.getLength();
			if (offset >= start && offset < end) {
				return partition;
			} 
		}
		return null;
	}

	/**
	 * @see org.eclipse.jface.text.IDocumentPartitionerExtension#documentChanged2(org.eclipse.jface.text.DocumentEvent)
	 */
	public IRegion documentChanged2(DocumentEvent event) {
		String text = event.getText();
		if (getDocument().getLength() == 0) {
			// cleared
			fPartitions.clear();
			return new Region(0,0);
		}
		addPartition(new MessageConsolePartition(fLastStream, event.getOffset(), text.length()));
		ITypedRegion[] affectedRegions = computePartitioning(event.getOffset(), text.length());
		if (affectedRegions.length == 0) {
			return null;
		}
		if (affectedRegions.length == 1) {
			return affectedRegions[0];
		}
		int affectedLength = affectedRegions[0].getLength();
		for (int i = 1; i < affectedRegions.length; i++) {
			ITypedRegion region = affectedRegions[i];
			affectedLength += region.getLength();
		}
		return new Region(affectedRegions[0].getOffset(), affectedLength);
	}

	/**
	 * Adds a new colored input partition, combining with the previous partition if
	 * possible.
	 */
	protected MessageConsolePartition addPartition(MessageConsolePartition partition) {
		if (fPartitions.isEmpty()) {
			fPartitions.add(partition);
		} else {
			int index = fPartitions.size() - 1;
			MessageConsolePartition last = (MessageConsolePartition)fPartitions.get(index);
			if (last.canBeCombinedWith(partition)) {
				// replace with a single partition
				partition = last.combineWith(partition);
				fPartitions.set(index, partition);
			} else {
				// different kinds - add a new parition
				fPartitions.add(partition);
			}
		}
		return partition;
	}	
	
	/**
	 * Creates a new paritioner and document, and connects this partitioner
	 * to the document.
	 */
	public MessageConsolePartitioner() {
		IDocument doc = new Document();
		connect(doc);
	}
	
	/**
	 * Adds the new text to the document.
	 * 
	 * @param text the text to append
	 * @param stream the stream to append to
	 */
	protected synchronized void appendToDocument(final String text, final MessageConsoleStream stream) {
		Runnable r = new Runnable() {
			public void run() {
				fLastStream = stream;
				try {
					fDocument.replace(fDocument.getLength(), 0, text);
				} catch (BadLocationException e) {
				}
			}
		};
		Display display = DebugUIPlugin.getStandardDisplay();
		if (display != null) {
			display.asyncExec(r);
		}
	}
	
	/**
	 * Returns the document this partitioner is connected to, or <code>null</code>
	 * if none.
	 * 
	 * @return the document this partitioner is connected to, or <code>null</code>
	 *   if none
	 */
	protected IDocument getDocument() {
		return fDocument;
	}

}
