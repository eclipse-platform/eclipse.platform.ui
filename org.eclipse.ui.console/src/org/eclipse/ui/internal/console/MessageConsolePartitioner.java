/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.internal.console;


import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.MessageConsoleStream;

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
	
	
	private int highWaterMark = 100000;
	private int lowWaterMark = 80000;
	private int maxAppendSize = lowWaterMark;

	private List streamEntries = new ArrayList();
	private boolean killed = false;
	private boolean updaterThreadStarted = false;

	private IConsoleManager fConsoleManager;
	/**
	 * Creates a new paritioner and document, and connects this partitioner
	 * to the document.
	 */
	public MessageConsolePartitioner() {
		IDocument doc = new Document();
		connect(doc);
	}
	
	/**
	 * Sets the low and high water marks for this console's text buffer.
	 * 
	 * @param low low water mark
	 * @param high high water mark
	 */
	public void setWaterMarks(int low, int high) {
		if (low >= high) {
			throw new IllegalArgumentException(ConsoleMessages.getString("MessageConsolePartitioner.2")); //$NON-NLS-1$
		}
		if (low < 1000) {
			throw new IllegalArgumentException(ConsoleMessages.getString("MessageConsolePartitioner.3")); //$NON-NLS-1$
		}
		lowWaterMark = low;
		highWaterMark = high;
		maxAppendSize = Math.min(80000, low);
	}
	/**
	 * @return Returns the highWaterMark.
	 */
	public int getHighWaterMark() {
		return highWaterMark;
	}

	/**
	 * @return Returns the lowWaterMark.
	 */
	public int getLowWaterMark() {
		return lowWaterMark;
	}

	/**
	 * @return Returns the maxAppendSize.
	 */
	public int getMaxAppendSize() {
		return maxAppendSize;
	}

	/**
	 * @param maxAppendSize The maxAppendSize to set.
	 */
	public void setMaxAppendSize(int maxAppendSize) {
		this.maxAppendSize = maxAppendSize;
	}
	
	
	/**
	 * @see org.eclipse.jface.text.IDocumentPartitioner#connect(org.eclipse.jface.text.IDocument)
	 */
	public void connect(IDocument document) {
		fDocument = document;
		document.setDocumentPartitioner(this);
		fConsoleManager = ConsolePlugin.getDefault().getConsoleManager();
	}

	/**
	 * @see org.eclipse.jface.text.IDocumentPartitioner#disconnect()
	 */
	public void disconnect() {
		fDocument.setDocumentPartitioner(null);
		killed = true;
		fConsoleManager = null;
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
		} 
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
	 * Checks to see if the console buffer has overflowed, and empties the
	 * overflow if needed, updating partitions and hyperlink positions.
	 */
	protected void checkOverflow() {
		if (highWaterMark >= 0) {
			if (fDocument.getLength() > highWaterMark) {
				int overflow = fDocument.getLength() - lowWaterMark;
				
				try {
					int line = fDocument.getLineOfOffset(overflow);
					int nextLineOffset = fDocument.getLineOffset(line+1);
					overflow = nextLineOffset;
				} catch (BadLocationException e1) {
				}
				
				// update partitions
				List newParitions = new ArrayList(fPartitions.size());
				Iterator partitions = fPartitions.iterator();
				while (partitions.hasNext()) {
					ITypedRegion region = (ITypedRegion) partitions.next();
					if (region instanceof MessageConsolePartition) {
						MessageConsolePartition messageConsolePartition = (MessageConsolePartition)region;

						ITypedRegion newPartition = null;
						int offset = region.getOffset();
						if (offset < overflow) {
							int endOffset = offset + region.getLength();
							if (endOffset < overflow) {
								// remove partition
							} else {
								// split partition
								int length = endOffset - overflow;
								newPartition = messageConsolePartition.createNewPartition(0, length);
							}
						} else {
							// modify parition offset
							newPartition = messageConsolePartition.createNewPartition(messageConsolePartition.getOffset()-overflow, messageConsolePartition.getLength());
						}
						if (newPartition != null) {
							newParitions.add(newPartition);
						}
					}
				}
				fPartitions = newParitions;
		
				//called from GUI Thread (see startUpdaterThread()), no asyncExec needed.
				try {
					fDocument.replace(0, overflow, "");  //$NON-NLS-1$
				} catch (BadLocationException e) {
				}		
			}
		}
	}
		

	/**
	 * Adds a new colored input partition, combining with the previous partition if
	 * possible.
	 */
	private MessageConsolePartition addPartition(MessageConsolePartition partition) {
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
	 * Returns the document this partitioner is connected to, or <code>null</code>
	 * if none.
	 * 
	 * @return the document this partitioner is connected to, or <code>null</code>
	 *   if none
	 */
	public IDocument getDocument() {
		return fDocument;
	}

	/**
	 * 
	 */
	private void startUpdaterThread() {
		if (updaterThreadStarted) {
			return;
		}
		
		updaterThreadStarted = true;
		
		Runnable r = new Runnable() {
			public void run() {

				while(!killed && streamEntries.size()>0) {
					synchronized(streamEntries) {
						final StreamEntry streamEntry = (StreamEntry)streamEntries.get(0);
						streamEntries.remove(0);
						 
						Runnable innerRunnable = new Runnable() {
							public void run() {
								fLastStream = streamEntry.stream;
								try {
									fDocument.replace(fDocument.getLength(), 0, streamEntry.text.toString());
									checkOverflow();
									fConsoleManager.warnOfContentChange(streamEntry.stream.getConsole());
								} catch (BadLocationException e) {
								}
							}
						};
						Display display = ConsolePlugin.getStandardDisplay();
						if (display != null) {
							display.asyncExec(innerRunnable);
						}
						
						try {
							//Don't just die! Give up the lock and allow more StreamEntry objects to be
							//added to list
							Thread.sleep(100);
						} catch (InterruptedException e) {							
						}
					}
				}
				updaterThreadStarted = false;
			}
		};
		
		new Thread(r, "MessageConsoleUpdaterThread").start(); //$NON-NLS-1$
	}
	
	/**
	 * Adds the new text to the document.
	 * 
	 * @param text the text to append
	 * @param stream the stream to append to
	 */
	public void appendToDocument(final String text, final MessageConsoleStream stream) {
		int offset = 0;
		int length = text.length();
		
		synchronized(streamEntries) {
			//try to fit in last StreamEntry if they are the same stream		
			if (streamEntries.size() > 0) { 
				StreamEntry streamEntry = (StreamEntry)streamEntries.get(streamEntries.size()-1);
				if (streamEntry.stream == stream) {
					int emptySpace = maxAppendSize - streamEntry.text.length();
					if (length <= emptySpace) {
						streamEntry.text.append(text);
						offset = length;
						length = 0;
					} else {
						streamEntry.text.append(text.substring(offset, emptySpace));
						offset += emptySpace;
						length -= emptySpace;
					}
				}
			} 
			
			//put remaining text into new StreamEntry objects
			while (length > 0) {
				int toCopy = Math.min(maxAppendSize, length);
				String substring = text.substring(offset, offset+toCopy);
				StreamEntry streamEntry = new StreamEntry(substring, stream);
				streamEntries.add(streamEntry);
				offset += toCopy;
				length -= toCopy;
			}
			
		} //give up the lock
		
		startUpdaterThread();
	}	
	
	private class StreamEntry {
		MessageConsoleStream stream;
		StringBuffer text;
		
		StreamEntry(String text, MessageConsoleStream stream) {
			this.stream = stream;
			this.text = new StringBuffer(text);
		}
	}
}