package org.eclipse.debug.internal.ui.views.console;

/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp.  All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
**********************************************************************/

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.eclipse.debug.core.IStreamListener;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.core.model.IStreamMonitor;
import org.eclipse.debug.core.model.IStreamsProxy;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.preferences.IDebugPreferenceConstants;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.jface.text.IDocumentPartitionerExtension;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.widgets.Display;

/**
 * Default console document paritioner. Partitions a document into
 * color regions for standard in, out, err.
 */
public class ConsoleDocumentPartitioner implements IDocumentPartitioner, IDocumentPartitionerExtension, IPropertyChangeListener, IConsoleDocument {

	private boolean fClosed= false;
	private boolean fKilled= false;

	protected IProcess fProcess;
	protected IConsoleDocumentContentProvider fContentProvider;
	private IStreamsProxy fProxy;
	protected List fStreamListeners = new ArrayList(2);
	
	private String[] fSortedLineDelimiters;
	
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
	}
	
	class StreamListener implements IStreamListener {
		
		private String fStreamIdentifier;
		private IStreamMonitor fStreamMonitor;
	
		public StreamListener(String streamIdentifier, IStreamMonitor streamMonitor) {
			fStreamIdentifier = streamIdentifier;
			fStreamMonitor = streamMonitor;
		}
		
		public void streamAppended(String newText, IStreamMonitor monitor) {
			DebugUIPlugin.getConsoleDocumentManager().aboutToWriteSystemErr(fDocument);
			ConsoleDocumentPartitioner.this.streamAppended(newText, fStreamIdentifier);
		}
		
		public void connect() {
			fStreamMonitor.addListener(this);
			String contents= fStreamMonitor.getContents();
			if (contents.length() > 0) {
				ConsoleDocumentPartitioner.this.streamAppended(contents, fStreamIdentifier);
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
	 * Whether to keep polling
	 */
	private boolean fPoll = false;
	
	/**
	 * The associated docuemnt	 */
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
	 * List of partitions	 */
	private List fPartitions = new ArrayList(5);
	
	/**
	 * The base number of milliseconds to pause
	 * between polls.
	 */
	private static final long BASE_DELAY= 100L;
	
	/**
	 * The identifier of the stream that was last appended to	 */
	private String fLastStreamIdentifier= null;
	
	/**
	 * Keyboard input buffer	 */
	private StringBuffer fInputBuffer = new StringBuffer();

	/**
	 * @see org.eclipse.jface.text.IDocumentPartitioner#connect(org.eclipse.jface.text.IDocument)
	 */
	public void connect(IDocument document) {
		fDocument = document;
		document.setDocumentPartitioner(this);
		IPreferenceStore store = DebugUIPlugin.getDefault().getPreferenceStore();
		fWrap = store.getBoolean(IDebugPreferenceConstants.CONSOLE_WRAP);
		fMaxLineLength = store.getInt(IDebugPreferenceConstants.CONSOLE_WIDTH);
		store.addPropertyChangeListener(this);		
		fContentProvider.connect(fProcess, this);
	}

	/**
	 * @see org.eclipse.jface.text.IDocumentPartitioner#disconnect()
	 */
	public void disconnect() {
		kill();
		fContentProvider.disconnect();
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
			if (offset >= start && offset <= end) {
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
		if (isAppendInProgress()) {
			// stream input
			addPartition(new OutputPartition(fLastStreamIdentifier, event.getOffset(), text.length()));
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
				return new Region(0,0);
			}
						
			if (amountDeleted > 0) {
				// deletion
				fInputBuffer.replace(bufferModifyOffset, bufferModifyOffsetEnd, text);
				InputPartition partition = null;
				if (fInputBuffer.length() > 0) { 
					// replace the last partition
					partition = new InputPartition(IDebugPreferenceConstants.CONSOLE_SYS_IN_RGB, bufferStartOffset, fInputBuffer.length());
					fPartitions.set(fPartitions.size() - 1, partition);
				} else {
					// remove last partition - it is now empty
					fPartitions.remove(fPartitions.size() - 1);
				}
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
						addPartition(new InputPartition(IDebugPreferenceConstants.CONSOLE_SYS_IN_RGB, partitionOffset, split));
						partitionOffset += split;
						addPartition(new InputPartition(IDebugPreferenceConstants.CONSOLE_SYS_IN_RGB, partitionOffset, 0));
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
					addPartition(new InputPartition(IDebugPreferenceConstants.CONSOLE_SYS_IN_RGB, partitionOffset, remaining.length()));
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
	protected void addPartition(StreamPartition partition) {
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
	}	
	
	public ConsoleDocumentPartitioner(IProcess process, IConsoleDocumentContentProvider contentProvider) {
		fProcess= process;
		fContentProvider = contentProvider;
	}

	public void close() {
		if (!fClosed) {
			fClosed= true;
			fPoll = false;
			Iterator iter = fStreamListeners.iterator();
			while (iter.hasNext()) {
				StreamListener listener = (StreamListener)iter.next();
				listener.disconnect();
			}
			DebugUIPlugin.getDefault().getPreferenceStore().removePropertyChangeListener(this);
			fDocument.set(""); //$NON-NLS-1$
		}
	}
	
	/**
	 * Stops reading/polling immediately
	 */
	public void kill() {
		fKilled = true;
		if (fPollingThread != null && fPollingThread.isAlive()) {
			fPollingThread.interrupt();
		}
		close();
	}

	protected boolean isClosed() {
		return fClosed;
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
		while (!fKilled && fPoll && (!isTerminated() || !fQueue.isEmpty())) {
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
			while (!fKilled && processed < fQueue.size() && amount < 8096) {
				StreamEntry entry = (StreamEntry)fQueue.get(processed);
				if (prev == null || prev.getStreamIdentifier().equals(entry.getStreamIdentifier())) {
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
				} else {
					// only do one append per poll
					break;
				}
				
				prev = entry;
				processed++;
				amount+= entry.getText().length();
			}
			if (buffer != null) {
				appendToDocument(buffer.toString(), prev.getStreamIdentifier());
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
	 * System out or System error has had text append to it.
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
				} catch (BadLocationException e) {
				}
				setAppendInProgress(false);
			}
		};
		Display display = DebugUIPlugin.getStandardDisplay();
		if (display != null) {
			display.asyncExec(r);
		}
	}
	
	/**
	 * System out or System error has had text append to it.
	 * Adds a new entry to the queue.
	 */
	protected void streamAppended(String text, String streamIdetifier) {
		fQueue.add(new StreamEntry(text, streamIdetifier));
	}
					
	/**
	 * Sets whether a runnable has been submitted to update the console
	 * document.
	 */
	protected void setAppendInProgress(boolean appending) {
		fAppending = appending;
	}

	/**
	 * Sets whether a runnable has been submitted to update the console
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
	 * associated document, sorted by length in descending order.	 */
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

	/**
	 * @see org.eclipse.debug.internal.ui.views.console.IConsoleDocumentPartitioner#connect(org.eclipse.debug.core.model.IStreamMonitor, java.lang.String)
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

	/**
	 * @see org.eclipse.debug.internal.ui.views.console.IConsoleDocumentPartitioner#connect(org.eclipse.debug.core.model.IStreamsProxy)
	 */
	public void connect(IStreamsProxy streamsProxy) {
		fProxy = streamsProxy;
		connect(streamsProxy.getOutputStreamMonitor(), IDebugPreferenceConstants.CONSOLE_SYS_OUT_RGB);
		connect(streamsProxy.getErrorStreamMonitor(), IDebugPreferenceConstants.CONSOLE_SYS_ERR_RGB);
	}
	
	protected boolean isTerminated() {
		return fContentProvider.isTerminated();
	}

	protected IConsoleDocumentContentProvider getContentProvider() {
		return fContentProvider;
	}
}
