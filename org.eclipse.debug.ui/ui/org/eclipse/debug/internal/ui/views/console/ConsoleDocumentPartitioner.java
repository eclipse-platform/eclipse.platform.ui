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

package org.eclipse.debug.internal.ui.views.console;


import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IDebugEventSetListener;
import org.eclipse.debug.core.IStreamListener;
import org.eclipse.debug.core.model.IFlushableStreamMonitor;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.core.model.IStreamMonitor;
import org.eclipse.debug.core.model.IStreamsProxy;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.preferences.IDebugPreferenceConstants;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.debug.ui.console.IConsole;
import org.eclipse.debug.ui.console.IConsoleColorProvider;
import org.eclipse.debug.ui.console.IConsoleHyperlink;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.BadPositionCategoryException;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.jface.text.IDocumentPartitionerExtension;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.console.ConsolePlugin;

/**
 * Default console document partitioner. Partitions a document into
 * color regions for standard in, out, err.
 */
public class ConsoleDocumentPartitioner implements IDocumentPartitioner, IDocumentPartitionerExtension, IPropertyChangeListener, IConsole, IDebugEventSetListener {

	protected IProcess fProcess;
	protected IConsoleColorProvider fColorProvider;
	private IStreamsProxy fProxy;
	protected List fStreamListeners = new ArrayList(2);
	
	private String[] fSortedLineDelimiters;
	
	// high and low water marks for buffering output
	private boolean fUpdatingBuffer = false;
	private int fLowWaterMark;
	private int fHighWaterMark;
	
	// max amount of output (characters) processed per poll
	private int fMaxAppendSize;
	
	class StreamEntry {
		/**
		 * Identifier of the stream written to.
		 */
		private String fStreamIdentifier;
		/**
		 * The text written
		 */
		private String fText = null;
		
		StreamEntry(String text, String streamIdentifier) {
			fText = text;
			fStreamIdentifier = streamIdentifier;
		}
		
		/**
		 * Returns the stream identifier
		 */
		public String getStreamIdentifier() {
			return fStreamIdentifier;
		}
		
		/**
		 * Returns the text written
		 */
		public String getText() {
			return fText;
		}
		
		public boolean isClosedEntry() {
			return false;
		}
	}
	
	/**
	 * A stream entry representing stream closure
	 */
	class StreamsClosedEntry extends StreamEntry {
		StreamsClosedEntry() {
			super("", ""); //$NON-NLS-1$ //$NON-NLS-2$
		}
		
		public boolean isClosedEntry() {
			return true;
		}
	}
	
	class StreamListener implements IStreamListener {
		
		private String fStreamIdentifier;
		private IStreamMonitor fStreamMonitor;
		private boolean fIsSystemOut = false;
		private boolean fIsSystemErr = false;
	
		public StreamListener(String streamIdentifier, IStreamMonitor streamMonitor) {
			fStreamIdentifier = streamIdentifier;
			fStreamMonitor = streamMonitor;
			fIsSystemOut = IDebugUIConstants.ID_STANDARD_OUTPUT_STREAM.equals(streamIdentifier);
			fIsSystemErr = IDebugUIConstants.ID_STANDARD_ERROR_STREAM.equals(streamIdentifier); 
		}
		
		public void streamAppended(String newText, IStreamMonitor monitor) {
			if (fIsSystemOut) {
				DebugUIPlugin.getDefault().getConsoleDocumentManager().aboutToWriteSystemOut(getProcess());
			} else if (fIsSystemErr) {
				DebugUIPlugin.getDefault().getConsoleDocumentManager().aboutToWriteSystemErr(getProcess());
			}
			ConsoleDocumentPartitioner.this.streamAppended(newText, fStreamIdentifier);
		}
		
		public void streamClosed(IStreamMonitor monitor) {
			//ConsoleDocumentPartitioner.this.streamClosed(fStreamIdentifier);
		}
		
		public void connect() {
			fStreamMonitor.addListener(this);
			String contents= fStreamMonitor.getContents();
			if (fStreamMonitor instanceof IFlushableStreamMonitor) {
				// flush the underlying buffer and do not duplicate storage
				IFlushableStreamMonitor flushableStreamMonitor = (IFlushableStreamMonitor)fStreamMonitor;
				flushableStreamMonitor.flushContents();
				flushableStreamMonitor.setBuffered(false);
			}
			if (contents.length() > 0) {
				streamAppended(contents, fStreamMonitor);
			}
		}
		
		public void disconnect() {
			fStreamMonitor.removeListener(this);
		}
	}

	
	/**
	 * A queue of stream entries written to standard out and standard err.
	 * Entries appended to the end of the queue and removed from the front.
	 * Intentionally a vector to obtain synchronization as entries are
	 * added and removed.
	 */
	private Vector fQueue = new Vector(10);
	
	/**
	 * Thread that polls the queue for new output
	 */
	private Thread fPollingThread = null;
	
	/**
	 * Whether an append is still in  progress or to be run
	 */
	private boolean fAppending = false;
	
	/**
	 * Whether the console has been killed/disconnected
	 */
	private boolean fKilled = false;
	
	/**
	 * Whether to keep polling
	 */
	private boolean fPoll = false;
	
	/**
	 * Whether the streams coonnected to the associated process are closed
	 */
	private boolean fClosed= false;
	
	/**
	 * The associated document
	 */
	private IDocument fDocument = null;
	
	/**
	 * The length of the current line
	 */
	private int fLineLength = 0;
	
	/** 
	 * Maximum line length before wrapping.
	 */
	private int fMaxLineLength = 80;
	
	/**
	 * Whether using auto-wrap mode
	 */
	private boolean fWrap = false;
	
	/**
	 * List of partitions
	 */
	private List fPartitions = new ArrayList(5);
	
	/**
	 * The base number of milliseconds to pause
	 * between polls.
	 */
	private static final long BASE_DELAY= 100L;
	
	/**
	 * The identifier of the stream that was last appended to
	 */
	private String fLastStreamIdentifier= null;
	
	/**
	 * Keyboard input buffer
	 */
	private StringBuffer fInputBuffer = new StringBuffer();
	
	/**
	 * Queue of hyperlinks to be added to the console
	 */
	private Vector fPendingLinks = new Vector();
	
	/**
	 * The line notifier associated with this partitioner or <code>null</code> if none 
	 */
	private ConsoleLineNotifier fLineNotifier = null;

	/**
	 * @see org.eclipse.jface.text.IDocumentPartitioner#connect(org.eclipse.jface.text.IDocument)
	 */
	public void connect(IDocument document) {
		fDocument = document;
		fDocument.addPositionCategory(HyperlinkPosition.HYPER_LINK_CATEGORY);
		document.setDocumentPartitioner(this);
		IPreferenceStore store = DebugUIPlugin.getDefault().getPreferenceStore();
		fWrap = store.getBoolean(IDebugPreferenceConstants.CONSOLE_WRAP);
		fMaxLineLength = store.getInt(IDebugPreferenceConstants.CONSOLE_WIDTH);
		store.addPropertyChangeListener(this);		
		fColorProvider.connect(fProcess, this);
		DebugPlugin.getDefault().addDebugEventListener(this);
		if (fProcess.isTerminated()) {
			// it is possible the terminate event will have been fired before the
			// document is connected - in this case, ensure we have closed the streams
			// and notified the line tracker
			streamsClosed();
		}
	}

	/**
	 * @see org.eclipse.jface.text.IDocumentPartitioner#disconnect()
	 */
	public void disconnect() {
		kill();
		if (fLineNotifier != null) {
			fLineNotifier.disconnect();
		}
		fColorProvider.disconnect();
		fDocument.setDocumentPartitioner(null);
		DebugPlugin.getDefault().removeDebugEventListener(this);
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
		return new String[] {InputPartition.INPUT_PARTITION_TYPE, OutputPartition.OUTPUT_PARTITION_TYPE, BreakPartition.BREAK_PARTITION_TYPE};
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
		if (fUpdatingBuffer) {
			return new Region(0, fDocument.getLength());
		}
		addPendingLinks();
		String text = event.getText();
		if (isAppendInProgress()) {
			// stream input
			addPartition(new OutputPartition(fLastStreamIdentifier, event.getOffset(), text.length()));
			if (fLineNotifier != null) {
				fLineNotifier.consoleChanged(event);
			}
		} else {
			// console keyboard input
			int amountDeleted = event.getLength() - text.length();
			int docLength = fDocument.getLength();
			int bufferStartOffset = docLength + amountDeleted - fInputBuffer.length();
			int bufferModifyOffset = event.getOffset() - bufferStartOffset;
			int bufferModifyOffsetEnd = bufferModifyOffset + event.getLength();
			
			if (docLength == 0) {
				// cleared
				fQueue.clear();
				fInputBuffer.setLength(0);
				fPartitions.clear();
				// reset lines processed to 0
				if (fLineNotifier != null) {
					fLineNotifier.setLinesProcessed(0);
				}
				// remove existing positions
				try {
					Position[] positions = fDocument.getPositions(HyperlinkPosition.HYPER_LINK_CATEGORY);
					for (int i = 0; i < positions.length; i++) {
						Position position = positions[i];
						fDocument.removePosition(HyperlinkPosition.HYPER_LINK_CATEGORY, position);
					}
				} catch (BadPositionCategoryException e) {
				}
				return new Region(0,0);
			}
						
			if (amountDeleted > 0) {
				// deletion
				fInputBuffer.replace(bufferModifyOffset, bufferModifyOffsetEnd, text);
				// replace the last partition
				InputPartition partition = new InputPartition(IDebugUIConstants.ID_STANDARD_INPUT_STREAM, bufferStartOffset, fInputBuffer.length());
				fPartitions.set(fPartitions.size() - 1, partition);
			} else {
				// insert/replace - must process entire buffer in case of
				// line delimiter insertion in middle of buffer
				
				// parse for line delimiters (indicate chunks to write to standard in)
				String[] lineDelimiters= getLegalLineDelimiters();
				StringBuffer temp =new StringBuffer(fInputBuffer.toString());
				temp.replace(bufferModifyOffset, bufferModifyOffsetEnd, text); 
				String remaining = temp.toString();
				int partitionOffset = bufferStartOffset;
				fInputBuffer.setLength(0);
				boolean includesLF = false;
				// line delimiters are sorted by length (compare longest ones first)
				for (int i= lineDelimiters.length - 1; i >= 0; i--) {
					int lf = remaining.indexOf(lineDelimiters[i]);
					while (lf >= 0) {
						includesLF = true;
						int split = lf + lineDelimiters[i].length();
						fInputBuffer.append(remaining.substring(0, split));
						remaining = remaining.substring(split);
						String buffer = fInputBuffer.toString();
						fInputBuffer.setLength(0);
						InputPartition written = (InputPartition)addPartition(new InputPartition(IDebugUIConstants.ID_STANDARD_INPUT_STREAM, partitionOffset, split));
						written.setReadOnly(true);
						partitionOffset += split;
						addPartition(new InputPartition(IDebugUIConstants.ID_STANDARD_INPUT_STREAM, partitionOffset, 0));
						if (fProxy != null) {
							try {
								fProxy.write(buffer);
							} catch (IOException ioe) {
								DebugUIPlugin.log(ioe);
							}
						}
						lf = remaining.indexOf(lineDelimiters[i]);
					}
					if (includesLF) {
						break;
					}
				}	
				if (remaining.length() > 0) {
					fInputBuffer.append(remaining);
					addPartition(new InputPartition(IDebugUIConstants.ID_STANDARD_INPUT_STREAM, partitionOffset, remaining.length()));
				}
			}
		}
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
	protected StreamPartition addPartition(StreamPartition partition) {
		if (fPartitions.isEmpty()) {
			fPartitions.add(partition);
		} else {
			int index = fPartitions.size() - 1;
			StreamPartition last = (StreamPartition)fPartitions.get(index);
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
	 * Add any pending links to the document that are now within the document's
	 * bounds.
	 */
	protected void addPendingLinks() {
		synchronized (fPendingLinks) {
			if (fPendingLinks.isEmpty()) {
				return;
			}
			Iterator links = fPendingLinks.iterator();
			while (links.hasNext()) {
				HyperlinkPosition link = (HyperlinkPosition)links.next();
				if ((link.getOffset() + link.getLength()) <= fDocument.getLength()) {
					links.remove();
					addLink(link.getHyperLink(), link.getOffset(), link.getLength());
				}
			}
		}
	}
	
	public ConsoleDocumentPartitioner(IProcess process, IConsoleColorProvider colorProvider) {
		fProcess= process;
		fColorProvider = colorProvider;
		IPreferenceStore store = DebugUIPlugin.getDefault().getPreferenceStore();
		boolean limit = store.getBoolean(IDebugPreferenceConstants.CONSOLE_LIMIT_CONSOLE_OUTPUT);
		if (limit) {
			fLowWaterMark = store.getInt(IDebugPreferenceConstants.CONSOLE_LOW_WATER_MARK);
			fHighWaterMark = store.getInt(IDebugPreferenceConstants.CONSOLE_HIGH_WATER_MARK);
			fMaxAppendSize = fLowWaterMark;
		} else {
			fLowWaterMark = -1;
			fHighWaterMark = -1;
			fMaxAppendSize = 80000;
		}
	}
	
	/**
	 * Stops reading/polling immediately
	 */
	public synchronized void kill() {
		if (!fKilled) {
			fKilled = true;
			if (fPollingThread != null && fPollingThread.isAlive()) {
				fPollingThread.interrupt();
			}
			fPoll = false;
			Iterator iter = fStreamListeners.iterator();
			while (iter.hasNext()) {
				StreamListener listener = (StreamListener)iter.next();
				listener.disconnect();
			}
			DebugUIPlugin.getDefault().getPreferenceStore().removePropertyChangeListener(this);
		}
	}

	public synchronized void startReading() {
		if (fPollingThread != null) {
			// already polling
			return;
		}
		Runnable r = new Runnable() {
			public void run() {
				pollAndSleep();
			}
		};
		fPoll = true;
		fPollingThread = new Thread(r, "Console Polling Thread"); //$NON-NLS-1$
		fPollingThread.start();
	}
	
	/**
	 * Polls and sleeps until closed or the associated
	 * process terminates
	 */
	protected void pollAndSleep() {
		while (!fKilled && fPoll && (!isClosed() || !fQueue.isEmpty())) {
			poll();
			try {
				Thread.sleep(BASE_DELAY);
			} catch (InterruptedException e) {
			}
		}
	}
	
	/**
	 * Polls the queue for new output and updates this document
	 */
	protected void poll() {
		if (isAppendInProgress()) {
			return;
		}
		synchronized(fQueue) {
			StringBuffer buffer = null;
			StreamEntry prev = null;
			int processed = 0;
			int amount = 0;
			String[] lds = fDocument.getLegalLineDelimiters();
			boolean closed= false;
			while (!fKilled && !closed && processed < fQueue.size() && amount < fMaxAppendSize) {
				StreamEntry entry = (StreamEntry)fQueue.get(processed);
				if (entry.isClosedEntry()) {
					closed = true;
					processed++;
				} else if (prev == null || prev.getStreamIdentifier().equals(entry.getStreamIdentifier())) {
					String text = entry.getText();
					if (buffer == null) {
						buffer = new StringBuffer(text.length());
					}
					if (isWrap()) {
						for (int i = 0; i < text.length(); i++) {
							if (fLineLength >= fMaxLineLength) {
								String d = getLineDelimiter(text, i, lds);
								if (d == null) {
									buffer.append(lds[0]);
								} else {
									buffer.append(d);
									i = i + d.length();
								}
								fLineLength = 0;
							}			
							if (i < text.length()) {										
								String lineDelimiter = getLineDelimiter(text, i, lds);				
								if (lineDelimiter == null) { 
									buffer.append(text.charAt(i));
									fLineLength++;
								} else {
									buffer.append(lineDelimiter);
									fLineLength = 0;
									i = i + lineDelimiter.length() - 1;
								}
							}
						} 
					} else {
						buffer.append(text);
					}
					prev = entry;
					processed++;
					amount+= entry.getText().length();
				} else {
					// change streams - write the contents of the current stream
					// and start processing the next stream
					if (buffer != null) {
						appendToDocument(buffer.toString(), prev.getStreamIdentifier());
						buffer.setLength(0);
						prev = null;
					}
				}
			}
			if (buffer != null) {
				appendToDocument(buffer.toString(), prev.getStreamIdentifier());
			}
			if (closed) {
				Display display= DebugUIPlugin.getStandardDisplay(); 
				if (display != null) {
					display.asyncExec(new Runnable() {
						public void run() {
							if (fLineNotifier != null) {
								fLineNotifier.streamsClosed();
							}
						}
					});
				}
			}
			for (int i = 0; i < processed; i++) {
				fQueue.remove(0);
			}
		}
	}

	/**
	 * Returns the longest line delimiter at the given position in the given text,
	 * or <code>null</code> if none.
	 * 
	 * @param text the text in which to look for a line delimiter
	 * @param pos the position at which to look for a line delimiter
	 * @param lineDelimiters the line delimiters to look for
	 */
	protected String getLineDelimiter(String text, int pos, String[] lineDelimiters) {
		String ld = null;
		for (int i = 0; i < lineDelimiters.length; i++) {					
			if (text.regionMatches(pos, lineDelimiters[i], 0, lineDelimiters[i].length())) {
				if (ld == null) {
					ld = lineDelimiters[i];
				} else {
					if (ld.length() < lineDelimiters[i].length()) {
						ld = lineDelimiters[i];
					}
				}
			}
		}	
		return ld;	
	}
	
	/**
	 * Returns whether this console document is performing auto-wrap
	 */
	protected boolean isWrap() {
		return fWrap;
	}
	
	/**
	 * The stream with the given identifier has had text appended to it.
	 * Adds the new text to the document.
	 * 
	 * @see IStreamListener#streamAppended(String, IStreamMonitor)
	 */
	protected void appendToDocument(final String text, final String streamIdentifier) {
		Runnable r = new Runnable() {
			public void run() {
				setAppendInProgress(true);
				fLastStreamIdentifier = streamIdentifier;
				try {
					fDocument.replace(fDocument.getLength(), 0, text);
					warnOfContentChange();
				} catch (BadLocationException e) {
				}
				setAppendInProgress(false);
				checkOverflow();
			}
		};
		Display display = DebugUIPlugin.getStandardDisplay();
		if (display != null) {
			display.asyncExec(r);
		}
	}
	
	/**
	 * Checks to see if the console buffer has overflowed, and empties the
	 * overflow if needed, updating partitions and hyperlink positions.
	 */
	protected void checkOverflow() {
		if (fHighWaterMark >= 0) {
			if (fDocument.getLength() > fHighWaterMark) {
				int lineDifference = 0;
				if (fLineNotifier != null) {
					int processed = fLineNotifier.getLinesProcessed();
					int numLines = fDocument.getNumberOfLines();
					lineDifference = numLines - processed;
				}
				int overflow = fDocument.getLength() - fLowWaterMark; 
				fUpdatingBuffer = true;
				try {
					// update partitions
					List newParitions = new ArrayList(fPartitions.size());
					Iterator partitions = fPartitions.iterator();
					while (partitions.hasNext()) {
						ITypedRegion region = (ITypedRegion)partitions.next();
						if (region instanceof StreamPartition) {
							StreamPartition streamPartition = (StreamPartition)region;
							ITypedRegion newPartition = null;
							int offset = region.getOffset();
							if (offset < overflow) {
								int endOffset = offset + region.getLength();
								if (endOffset < overflow) {
									// remove partition
								} else {
									// split partition
									int length = endOffset - overflow;
									newPartition = streamPartition.createNewPartition(streamPartition.getStreamIdentifier(), 0, length);
								}
							} else {
								// modify parition offset
								newPartition = streamPartition.createNewPartition(streamPartition.getStreamIdentifier(), streamPartition.getOffset() - overflow, streamPartition.getLength());
							}
							if (newPartition != null) {
								newParitions.add(newPartition);
							}
						}
					}
					fPartitions = newParitions;
					// update hyperlinks
					try {
						Position[] hyperlinks = fDocument.getPositions(HyperlinkPosition.HYPER_LINK_CATEGORY);
						for (int i = 0; i < hyperlinks.length; i++) {
							HyperlinkPosition position = (HyperlinkPosition)hyperlinks[i];
							// remove old the position
							fDocument.removePosition(HyperlinkPosition.HYPER_LINK_CATEGORY, position);
							if (position.getOffset() >= overflow) {
								// add new poisition
								try {
									fDocument.addPosition(HyperlinkPosition.HYPER_LINK_CATEGORY, new HyperlinkPosition(position.getHyperLink(), position.getOffset() - overflow, position.getLength()));
								} catch (BadLocationException e) {
								}
							}
						}
					} catch (BadPositionCategoryException e) {
					}
					synchronized (fPendingLinks) {
						// update pending hyperlinks
						Vector newPendingLinks = new Vector(fPendingLinks.size());
						Iterator pendingLinks = fPendingLinks.iterator();
						while (pendingLinks.hasNext()) {
							HyperlinkPosition position = (HyperlinkPosition)pendingLinks.next();
							if (position.getOffset() >= overflow) {
								newPendingLinks.add(new HyperlinkPosition(position.getHyperLink(), position.getOffset() - overflow, position.getLength()));
							}
						}
						fPendingLinks = newPendingLinks;
					}
					
					// remove overflow text
					try {
						fDocument.replace(0, overflow, ""); //$NON-NLS-1$
					} catch (BadLocationException e) {
						DebugUIPlugin.log(e);
					}
				} finally {
					// update number of lines processed
					if (fLineNotifier != null) {
						fLineNotifier.setLinesProcessed(fDocument.getNumberOfLines() - lineDifference);
					}
					fUpdatingBuffer = false;
				}
			}
		}
	}
	
	/**
	 * The stream with the given identifier has had text appended to it.
	 * Adds a new entry to the queue.
	 */
	protected void streamAppended(String text, String streamIdentifier) {
		synchronized (fQueue) {
			if (fClosed) {
				// ERROR - attempt to append after console is closed
				DebugUIPlugin.logErrorMessage("An attempt was made to append text to the console, after it was closed."); //$NON-NLS-1$
			} else {
				fQueue.add(new StreamEntry(text, streamIdentifier));
			}
		}
	}
	
	/**
	 * The streams associated with this process have been closed.
	 * Adds a new "stream closed" entry to the queue.
	 */
	protected void streamsClosed() {
		synchronized (fQueue) {
			if (!fClosed) {
				fQueue.add(new StreamsClosedEntry());
				fClosed = true;
			}
		}
	}	
					
	/**
	 * Sets whether a runnable has been submitted to update the console
	 * document.
	 */
	protected void setAppendInProgress(boolean appending) {
		fAppending = appending;
	}

	/**
	 * Returns whether a runnable has been submitted to update the console
	 * document.
	 */
	protected boolean isAppendInProgress() {
		return fAppending;
	}
	
	/**
	 * @see org.eclipse.jface.util.IPropertyChangeListener#propertyChange(PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent event) {
		if (event.getProperty().equals(IDebugPreferenceConstants.CONSOLE_WRAP)) {
			fWrap = DebugUIPlugin.getDefault().getPreferenceStore().getBoolean(IDebugPreferenceConstants.CONSOLE_WRAP);
		} else if (event.getProperty().equals(IDebugPreferenceConstants.CONSOLE_WIDTH)) {
			fMaxLineLength = DebugUIPlugin.getDefault().getPreferenceStore().getInt(IDebugPreferenceConstants.CONSOLE_WIDTH);
		}
	}
	
	/**
	 * Returns a collection of legal line delimiters for this partitioner's
	 * associated document, sorted by length in descending order.
	 */
	protected String[] getLegalLineDelimiters() {
		if (fSortedLineDelimiters == null) {
			String[] lineDelimiters = fDocument.getLegalLineDelimiters();
			List list = new ArrayList(lineDelimiters.length);
			for (int i = 0; i < lineDelimiters.length; i++) {
				list.add(lineDelimiters[i]);
			}
			Comparator comparator = new Comparator() {
				/**
				 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
				 */
				public int compare(Object a, Object b) {
					String s1 = (String)a;
					String s2 = (String)b;
					return s2.length() - s1.length();
				}
	
			};
			Collections.sort(list, comparator);		
			fSortedLineDelimiters = (String[])list.toArray(new String[lineDelimiters.length]);
		}
		return fSortedLineDelimiters;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.console.IConsole#connect(org.eclipse.debug.core.model.IStreamMonitor, java.lang.String)
	 */
	public void connect(IStreamMonitor streamMonitor, String streamIdentifer) {
		if (streamMonitor != null) {
			StreamListener listener = new StreamListener(streamIdentifer, streamMonitor);
			fStreamListeners.add(listener);
			listener.connect();
			// ensure we start polling for output
			startReading();
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.console.IConsole#connect(org.eclipse.debug.core.model.IStreamsProxy)
	 */
	public void connect(IStreamsProxy streamsProxy) {
		fProxy = streamsProxy;
		connect(streamsProxy.getOutputStreamMonitor(), IDebugUIConstants.ID_STANDARD_OUTPUT_STREAM);
		connect(streamsProxy.getErrorStreamMonitor(), IDebugUIConstants.ID_STANDARD_ERROR_STREAM);
	}
	
	/**
	 * Returns whether the streams assocaited with this console's process
	 * have been closed.
	 */
	protected boolean isClosed() {
		return fClosed;
	}

	protected IConsoleColorProvider getColorProvider() {
		return fColorProvider;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.console.IConsole#addLink(org.eclipse.debug.ui.console.IConsoleHyperlink, int, int)
	 */
	public void addLink(IConsoleHyperlink link, int offset, int length) {
		HyperlinkPosition hyperlinkPosition = new HyperlinkPosition(link, offset, length); 
		try {
			fDocument.addPosition(HyperlinkPosition.HYPER_LINK_CATEGORY, hyperlinkPosition);
		} catch (BadPositionCategoryException e) {
			// internal error
			DebugUIPlugin.log(e);
		} catch (BadLocationException e) {
			// queue the link
			fPendingLinks.add(hyperlinkPosition);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.console.IConsole#getDocument()
	 */
	public IDocument getDocument() {
		return fDocument;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.console.IConsole#getProcess()
	 */
	public IProcess getProcess() {
		return fProcess;
	}
	
	/**
	 * Connects the given line notifier to this console document partitioner
	 * 
	 * @param lineNotifier
	 */
	public void connectLineNotifier(ConsoleLineNotifier lineNotifier) {
		fLineNotifier = lineNotifier;
		lineNotifier.connect(this);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.console.IConsole#getRegion(org.eclipse.debug.ui.console.IConsoleHyperlink)
	 */
	public IRegion getRegion(IConsoleHyperlink link) {
		try {
			Position[] positions = getDocument().getPositions(HyperlinkPosition.HYPER_LINK_CATEGORY);
			for (int i = 0; i < positions.length; i++) {
				HyperlinkPosition position = (HyperlinkPosition)positions[i];
				if (position.getHyperLink().equals(link)) {
					return new Region(position.getOffset(), position.getLength());
				}
			}
		} catch (BadPositionCategoryException e) {
		}
		return null;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.IDebugEventSetListener#handleDebugEvents(org.eclipse.debug.core.DebugEvent[])
	 */
	public void handleDebugEvents(DebugEvent[] events) {
		for (int i = 0; i < events.length; i++) {
			DebugEvent event = events[i];
			if (event.getKind() == DebugEvent.TERMINATE && event.getSource().equals(getProcess())) {
				DebugPlugin.getDefault().removeDebugEventListener(this);
				streamsClosed();
			}
		}

	}

	private void warnOfContentChange() {
		ConsolePlugin.getDefault().getConsoleManager().warnOfContentChange(DebugUITools.getConsole(fProcess));
	}

}
