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

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
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
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IHyperlink;
import org.eclipse.ui.console.IOConsole;
import org.eclipse.ui.console.IOConsoleInputStream;
import org.eclipse.ui.console.IOConsoleOutputStream;

/**
 * Partitions an IOConsole's document
 * @since 3.1
 *
 */
public class IOConsolePartitioner implements IDocumentPartitioner, IDocumentPartitionerExtension {
	private PendingPartition consoleClosedPartition;
	private IDocument document;
	private ArrayList partitions;
	/**
	 * Blocks of data that have not yet been appended to the document.
	 */
	private ArrayList pendingPartitions;
	/**
	 * A list of PendingPartitions to be appended by the updateJob
	 */
	private ArrayList updatePartitions;
	/**
	 * The last partition appended to the document
	 */
	private IOConsolePartition lastPartition;
	/**
	 * Job that appends pending partitions to the document.
	 */
	private DocumentUpdaterJob updateJob;
	/**
	 * The input stream attached to this document.
	 */
	private IOConsoleInputStream inputStream;
	/**
	 * Flag to indicate that the updateJob is updating the document.
	 */
	private boolean updateInProgress;
	/**
	 * A list of partitions containing input from the console, that have
	 * not been appended to the input stream yet.
	 */
	private ArrayList inputPartitions;
	/**
	 * offset used by updateJob
	 */
	private int firstOffset;
	/**
	 * An array of legal line delimiters
	 */
	private String[] lld;
	private int highWaterMark = -1;
	private int lowWaterMark = -1;
    private boolean connected = false;

    private IOConsole console;
	
	public IOConsolePartitioner(IOConsoleInputStream inputStream, IOConsole console) {
		this.inputStream = inputStream;
		this.console = console;
	}
	
	public IDocument getDocument() {
		return document;
	}
	
	/*
	 *  (non-Javadoc)
	 * @see org.eclipse.jface.text.IDocumentPartitioner#connect(org.eclipse.jface.text.IDocument)
	 */
	public void connect(IDocument document) {
		this.document = document;
		document.setDocumentPartitioner(this);
		lld = document.getLegalLineDelimiters();
		partitions = new ArrayList();
		pendingPartitions = new ArrayList();
		inputPartitions = new ArrayList();
		updateJob = new DocumentUpdaterJob();
		updateJob.setSystem(true);
		connected = true;
	}
	
	public int getHighWaterMark() {
	    return highWaterMark;
	}
	
	public int getLowWaterMark() {
	    return lowWaterMark;
	}
	
	public void setWaterMarks(int low, int high) {
		lowWaterMark = low;
		highWaterMark = high;
		ConsolePlugin.getStandardDisplay().asyncExec(new Runnable() {
			public void run() {
				checkBufferSize();
			}
		});
	}
	
    public void consoleFinished() {
        consoleClosedPartition = new PendingPartition(null, null);
        synchronized (pendingPartitions) {
            pendingPartitions.add(consoleClosedPartition);
        }
        updateJob.schedule(); //ensure that all pending partitions are processed.
    }
	
	/*
	 *  (non-Javadoc)
	 * @see org.eclipse.jface.text.IDocumentPartitioner#disconnect()
	 */
	public void disconnect() {
		document = null;
		partitions.clear();
		connected = false;
		inputStream.disconnect();
	}
	
	/*
	 *  (non-Javadoc)
	 * @see org.eclipse.jface.text.IDocumentPartitioner#documentAboutToBeChanged(org.eclipse.jface.text.DocumentEvent)
	 */
	public void documentAboutToBeChanged(DocumentEvent event) {
	}
	
	/*
	 *  (non-Javadoc)
	 * @see org.eclipse.jface.text.IDocumentPartitioner#documentChanged(org.eclipse.jface.text.DocumentEvent)
	 */
	public boolean documentChanged(DocumentEvent event) {
		return documentChanged2(event) != null;
	}
	
	/*
	 *  (non-Javadoc)
	 * @see org.eclipse.jface.text.IDocumentPartitioner#getLegalContentTypes()
	 */
	public String[] getLegalContentTypes() {
		return new String[] { IOConsolePartition.OUTPUT_PARTITION_TYPE, IOConsolePartition.INPUT_PARTITION_TYPE };
	}
	
	/*
	 *  (non-Javadoc)
	 * @see org.eclipse.jface.text.IDocumentPartitioner#getContentType(int)
	 */
	public String getContentType(int offset) {
		return getPartition(offset).getType();
	}
	
	/*
	 *  (non-Javadoc)
	 * @see org.eclipse.jface.text.IDocumentPartitioner#computePartitioning(int, int)
	 */
	public ITypedRegion[] computePartitioning(int offset, int length) {
		int end = length == 0 ? offset : offset + length - 1;
		List list = new ArrayList();
		
		for (int i = 0; i < partitions.size(); i++) {
			ITypedRegion partition = (IOConsolePartition) partitions.get(i);
			int partitionStart = partition.getOffset();
			int partitionEnd = partitionStart + partition.getLength() - 1;
			
			if(partitionStart >= offset && partitionStart <= end) {
				list.add(partition);
			} else if (partitionEnd >= offset && partitionEnd <= end) {
				list.add(partition);
			} else if(partitionStart <= offset && partitionEnd>=end) {
			    list.add(partition);
			} else if (partitionStart > end) {
				break; // don't bother testing more.
			}
		}
		
		return (IOConsolePartition[]) list.toArray(new IOConsolePartition[list.size()]);
	}
	
	/*
	 *  (non-Javadoc)
	 * @see org.eclipse.jface.text.IDocumentPartitioner#getPartition(int)
	 */
	public ITypedRegion getPartition(int offset) {
		for (int i = 0; i < partitions.size(); i++) {
			ITypedRegion partition = (ITypedRegion) partitions.get(i);
			int start = partition.getOffset();
			int end = start + partition.getLength();
			if (offset >= start && offset < end) {
				return partition;
			} 
		}
		
		if (lastPartition == null)  {
			synchronized(partitions) {
				lastPartition = new IOConsolePartition(inputStream, ""); //$NON-NLS-1$
				lastPartition.setOffset(offset);
				partitions.add(lastPartition);
				inputPartitions.add(lastPartition);
			}
		}
		return lastPartition;
	}
	
	/**
	 * Returns the region occupied by the hyperlink
	 */
	public IRegion getRegion(IHyperlink link) {
		try {
			Position[] positions = getDocument().getPositions(IOConsoleHyperlinkPosition.HYPER_LINK_CATEGORY);
			for (int i = 0; i < positions.length; i++) {
				IOConsoleHyperlinkPosition position = (IOConsoleHyperlinkPosition)positions[i];
				if (position.getHyperLink().equals(link)) {
					return new Region(position.getOffset(), position.getLength());
				}
			}
		} catch (BadPositionCategoryException e) {
		}
		return null;
	}
	
	/**
	 * Enforces the buffer size.
	 * When the number of lines in the document exceeds the high water mark, the 
	 * beginning of the document is trimmed until the number of lines equals the 
	 * low water mark.
	 */
	private void checkBufferSize() {
		if (lastPartition == null || highWaterMark == -1) {
			return;
		}
		
		int lines = document.getNumberOfLines();
		if (lines > highWaterMark) {
			
			synchronized(partitions) {
				try {
					int lineOffset = document.getLineOffset(lowWaterMark);
					
					IOConsolePartition partition = (IOConsolePartition) getPartition(lineOffset);
					int offset = partition.getOffset();
					setUpdateInProgress(true);
					document.replace(0, offset, ""); //$NON-NLS-1$
					setUpdateInProgress(false);
					
					//remove partitions and reset Partition offsets
					int index = partitions.indexOf(partition);
					partitions.remove(partitions.subList(0, index));
					
					offset = 0;
					for (Iterator i = partitions.iterator(); i.hasNext(); ) {
						IOConsolePartition p = (IOConsolePartition) i.next();
						p.setOffset(offset);
						offset += p.getLength();
					}
				} catch (BadLocationException e) {
				}  
			}
			
		}
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.text.IDocumentPartitionerExtension#documentChanged2(org.eclipse.jface.text.DocumentEvent)
	 */
	public IRegion documentChanged2(DocumentEvent event) {
	    if (document == null) {
	        return null; //another thread disconnected the partitioner
	    }
		if (document.getLength() == 0) { //document cleared
			partitions.clear();
			inputPartitions.clear();
			pendingPartitions.clear();     
			lastPartition = null;
			return new Region(0, 0);
		}
		
		
		if (updateInProgress) {
			synchronized(partitions) {
			    for (Iterator i = updatePartitions.iterator(); i.hasNext(); ) {
			        PendingPartition pp = (PendingPartition) i.next();
			        if (pp == consoleClosedPartition) {
			            continue;
			        }
			        IOConsolePartition partition = new IOConsolePartition(pp.stream, pp.text.length());
			        partition.setOffset(firstOffset);
			        firstOffset += partition.getLength();
			        lastPartition = partition;
			        partitions.add(partition);
			    }
			}
		} else {// user input.
			int amountDeleted = event.getLength() ;//- event.getText().length();
			
			if (amountDeleted > 0) {
				int offset = event.fOffset;    
				IOConsolePartition partition = (IOConsolePartition) getPartition(offset);
				if(partition == lastPartition) {
					partition.delete(event.fOffset-partition.getOffset(), amountDeleted);
				} 
			}
			
			synchronized(partitions) {
				if (lastPartition == null || lastPartition.isReadOnly()) {
					lastPartition = new IOConsolePartition(inputStream, event.fText); //$NON-NLS-1$
					lastPartition.setOffset(event.fOffset);
					partitions.add(lastPartition);
					inputPartitions.add(lastPartition);
				} else {
					lastPartition.append(event.fText);
				}
				
				for (int i = 0; i < lld.length; i++) {
					String ld = lld[i];
					if (event.fText.endsWith(ld)) {
						StringBuffer input = new StringBuffer();
						for (Iterator it = inputPartitions.iterator(); it.hasNext(); ) {
							IOConsolePartition partition = (IOConsolePartition) it.next();
							input.append(partition.getString());
							partition.clearBuffer();
							partition.setReadOnly();
						}
						inputStream.appendData(input.toString());
						inputPartitions.clear();
						break;
					}
				}
			}
		}   
		
		checkBufferSize();
		return new Region(event.fOffset, event.fText.length());
	}
	
	private void setUpdateInProgress(boolean b) {
		updateInProgress = b;
	}
		
	/**
	 * A stream has been appended, add to pendingPartions list and schedule updateJob.
	 * updateJob is scheduled with a slight delay, this allows the console to run the job
	 * less frequently and update the document with a greater amount of data each time 
	 * the job is run
	 * @param stream The stream that was written to.
	 * @param s The string that should be appended to the document.
	 */
	public void streamAppended(IOConsoleOutputStream stream, String s) {
		synchronized(pendingPartitions) {
			PendingPartition last = (PendingPartition) (pendingPartitions.size() > 0 ? pendingPartitions.get(pendingPartitions.size()-1) : null);
			if (last != null && last.stream == stream) {
				last.append(s);
			} else {
				pendingPartitions.add(new PendingPartition(stream, s));
				updateJob.schedule(50);
			}
		}
	}
	
	/**
	 * Holds data until updateJob can be run and the document can be updated.
	 */
	private class PendingPartition {
		String text;
		IOConsoleOutputStream stream;
		
		PendingPartition(IOConsoleOutputStream stream, String text) {
			this.stream = stream;
			this.text=text;
		}
		
		void append(String moreText) {
			text += moreText;
		}
	}
	
	/**
	 * Updates the document. Will append everything that is available before 
	 * finishing.
	 */
	private class DocumentUpdaterJob extends Job {

        DocumentUpdaterJob() {
			super("IOConsole Updater"); //$NON-NLS-1$
		}
		
        /*
         *  (non-Javadoc)
         * @see org.eclipse.core.internal.jobs.InternalJob#run(org.eclipse.core.runtime.IProgressMonitor)
         */
		protected IStatus run(IProgressMonitor monitor) {
			Display display = ConsolePlugin.getStandardDisplay();
			
			ArrayList pendingCopy = new ArrayList();
			StringBuffer buffer = null;
			boolean consoleClosed = false;
			while (display != null && pendingPartitions.size() > 0) {
				synchronized(pendingPartitions) {
					pendingCopy.addAll(pendingPartitions);
					pendingPartitions.clear();
				}
				
				buffer = new StringBuffer();
				for (Iterator i = pendingCopy.iterator(); i.hasNext(); ) {
				    PendingPartition pp = (PendingPartition) i.next();
				    if (pp != consoleClosedPartition) { 
				        buffer.append(pp.text);
				    } else {
				        consoleClosed = true;
				    }
				}
			}
			
			final ArrayList finalCopy = pendingCopy;
			final String toAppend = buffer.toString();
			final boolean notifyClosed = consoleClosed;
			
			display.asyncExec(new Runnable() {
			    public void run() {
			        if (connected) {
			            setUpdateInProgress(true);
			            updatePartitions = finalCopy;
			            firstOffset = document.getLength();
			            try {
			                document.replace(firstOffset, 0, toAppend.toString());
			            } catch (BadLocationException e) {
			            }
			            updatePartitions = null;
			            setUpdateInProgress(false);
			        }
			        if (notifyClosed) {
		                console.partitionerFinished();
		            }
			    }
			});
			
			return Status.OK_STATUS;
		}        
		
        /* 
         * Job will process as much as it can each time it's run, but it gets
         * scheduled everytime a PendingPartition is added to the list, meaning
         * that this job could get scheduled unnecessarily in cases of heavy output.
         * Note however, that schedule() will only reschedule a running/scheduled Job
         * once even if it's called many times.
         */
        public boolean shouldRun() {
            boolean shouldRun = connected && pendingPartitions != null && pendingPartitions.size() > 0;
            return shouldRun;
        }
	}

    
	
}