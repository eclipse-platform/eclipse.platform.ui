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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
import org.eclipse.ui.console.IPatternMatchListener;
import org.eclipse.ui.console.PatternMatchEvent;
import org.eclipse.ui.progress.UIJob;

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
	 * Regular expression matching job 
	 */
	private MatchJob matchJob;
    /**
     * Collection of compiled pattern match listeners
     */
    private ArrayList patterns = new ArrayList();	
    /**
     * Set to <code>true</code> when the partitioner completes the
     * processing of buffered output.
     */
    private boolean partitionerFinished = false;    
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
	private boolean closeFired = false;
	
	private TrimJob trimJob = new TrimJob();
	private boolean pendingTrim = false;
	
	/**
	 * Lock for appending to and removing from the document - used
	 * to synchronize addition of new text/partitions in the update
	 * job and handling buffer overflow/clearing of the console. 
	 */
	private Object overflowLock = new Object();
	
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
	public void connect(IDocument doc) {
		document = doc;
		document.setDocumentPartitioner(this);
		lld = document.getLegalLineDelimiters();
		partitions = new ArrayList();
		pendingPartitions = new ArrayList();
		inputPartitions = new ArrayList();
		updateJob = new DocumentUpdaterJob();
		matchJob = new MatchJob();
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
	
	/**
	 * Notification from the console that all of its streams have been closed.
	 */
    public void streamsClosed() {
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
		synchronized (overflowLock) {
		    matchJob.cancel();
	        synchronized (patterns) {
	            Iterator iterator = patterns.iterator();
	            while (iterator.hasNext()) {
	                CompiledPatternMatchListener notifier = (CompiledPatternMatchListener) iterator.next();
	                notifier.dispose();
	            }
	            patterns.clear();
	        }	    
			document = null;
			partitions.clear();
			connected = false;
			try {
	            inputStream.close();
	        } catch (IOException e) {
	        }
		}
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
		if (document != null) {
			int length = document.getLength();
			if (length > highWaterMark) {
			    trimJob.setOffset(length - lowWaterMark);
			    trimJob.schedule();
			}
		}
	}
	
	/**
	 * Clears the console
	 */
	public void clearBuffer() {
	    synchronized (overflowLock) {
	        trimJob.setOffset(-1);
		    trimJob.schedule();
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
				if (updatePartitions != null) {
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
			}
		} else {// user input.
			int amountDeleted = event.getLength() ;
			
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
				
				int lastLineDelimiter = -1;
				String partitionText = lastPartition.getString();
				for (int i = 0; i < lld.length; i++) {
					String ld = lld[i];
					int index = partitionText.lastIndexOf(ld);
					if (index != -1) {
					    index += ld.length();
					}
					if (index > lastLineDelimiter) {
					    lastLineDelimiter = index;
					}
				}
				if (lastLineDelimiter != -1) {
					StringBuffer input = new StringBuffer();
					Iterator it = inputPartitions.iterator();
					while (lastLineDelimiter > 0 && it.hasNext()) {
					    IOConsolePartition partition = (IOConsolePartition) it.next();
					    if (partition.getLength() <= lastLineDelimiter) {
					        lastLineDelimiter -= partition.getLength();
					        input.append(partitionText);
							partition.clearBuffer();
							partition.setReadOnly();
							inputPartitions.remove(partition);
							lastPartition = null;
					    } else {
					        //create a new partition containing everything up to the line delimiter
					        //and append that to the string buffer.
					        String contentBefore = partitionText.substring(0, lastLineDelimiter);
					        IOConsolePartition newPartition = new IOConsolePartition(inputStream, contentBefore);
					        newPartition.setOffset(partition.getOffset());
					        newPartition.setReadOnly();
					        newPartition.clearBuffer();
					        int index = partitions.indexOf(partition);
						    partitions.add(index, newPartition);
					        input.append(contentBefore);
					        //delete everything that has been appended to the buffer.
					        partition.delete(0, lastLineDelimiter);
					        partition.setOffset(lastLineDelimiter + partition.getOffset());
					        lastLineDelimiter = 0;
					    }
					}
					if (input.length() > 0) {
					    inputStream.appendData(input.toString());
					}

				}
			}
		}   
		
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
				updateJob.schedule(100);
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
		    synchronized (overflowLock) {

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
			                partitionerFinished();
			            }
				        checkBufferSize();
				        matchJob.schedule(100);
				    }
				});
		    }
			
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

    /**
     * Notification that all pending partitions have been processed.
     */
    private void partitionerFinished() {
        partitionerFinished = true;
        if (matchJob != null) {
            matchJob.schedule();
        }
    }
    /**
     * Adds the given pattern match listener to this console. The listener will
     * be connected and receive match notifications.
     * 
     * @param matchListener the pattern match listener to add
     */
    public void addPatternMatchListener(IPatternMatchListener matchListener) {
        synchronized(patterns) {
            // TODO: check for dups
            if (matchListener == null || matchListener.getPattern() == null) {
                throw new IllegalArgumentException("Pattern cannot be null"); //$NON-NLS-1$
            }
            
            Pattern pattern = Pattern.compile(matchListener.getPattern(), matchListener.getCompilerFlags());
            String qualifier = matchListener.getLineQualifier();
            Pattern qPattern = null;
            if (qualifier != null) {
            	qPattern = Pattern.compile(qualifier, matchListener.getCompilerFlags());
            }
            CompiledPatternMatchListener notifier = new CompiledPatternMatchListener(pattern, qPattern, matchListener);
            patterns.add(notifier);
            matchListener.connect(console);
            matchJob.schedule(100);
        }
    }
    
    /**
     * Removes the given pattern match listener from this console. The listener will be
     * disconnected and will no longer receive match notifications.
     * 
     * @param matchListener the pattern match listener to remove.
     */
    public void removePatternMatchListener(IPatternMatchListener matchListener) {
        synchronized(patterns){
            for (Iterator iter = patterns.iterator(); iter.hasNext();) {
                CompiledPatternMatchListener element = (CompiledPatternMatchListener) iter.next();
                if (element.listener == matchListener) {
                    iter.remove();
                    matchListener.disconnect();
                }
            }
        }
    }	
	
    private class CompiledPatternMatchListener {
        Pattern pattern;
        Pattern qualifier;
        IPatternMatchListener listener;
        int end = 0;
        
        CompiledPatternMatchListener(Pattern pattern, Pattern qualifier, IPatternMatchListener matchListener) {
            this.pattern = pattern;
            this.listener = matchListener;
            this.qualifier = qualifier;
        }
        
        public void dispose() {
            listener.disconnect();
            pattern = null;
            qualifier = null;
            listener = null;
        }
    }
    
    /**
     * Job to trim the console document, runs in the  UI thread.
     */
    private class TrimJob extends UIJob {
        
        /**
         * trims output up to the line containing the given offset,
         * or all output if -1.
         */
        private int truncateOffset;
        
        /**
         * Creates a new job to trim the buffer.
         */
        TrimJob() {
            super("Trim Job"); //$NON-NLS-1$
            setSystem(true);
        }
        
        /**
         * Sets the trim offset.
         * 
         * @param offset trims output up to the line containing the given offset
         */
        public void setOffset(int offset) {
            truncateOffset = offset;
        }

        /* (non-Javadoc)
         * @see org.eclipse.ui.progress.UIJob#runInUIThread(org.eclipse.core.runtime.IProgressMonitor)
         */
        public IStatus runInUIThread(IProgressMonitor monitor) {
        	if (lastPartition == null || highWaterMark == -1 || document == null) {
        		return Status.OK_STATUS;
        	}
        	
        	int length = document.getLength();
        	if (truncateOffset < length) {
        		synchronized (overflowLock) {
        			try {
        			    pendingTrim = true;
        				if (truncateOffset < 0) {
        				    // clear
        				    setUpdateInProgress(true);
        					document.set(""); //$NON-NLS-1$
        					setUpdateInProgress(false);
        					partitions.clear();
	        	    		// buffer has been emptied, reset match listeners
	            			Iterator iter = patterns.iterator();
	            			while (iter.hasNext()) {
	            				CompiledPatternMatchListener notifier = (CompiledPatternMatchListener)iter.next();
	            				notifier.end = 0;
	            			}        					
        				} else {
        				    // overflow
        				    int cutoffLine = document.getLineOfOffset(truncateOffset);
        				    int cutOffset = document.getLineOffset(cutoffLine);
        				
	        				if (!closeFired) {
	        				    // let the match job catch up unless its complete
		            			Iterator iter = patterns.iterator();
		            			while (iter.hasNext()) {
		            				CompiledPatternMatchListener notifier = (CompiledPatternMatchListener)iter.next();
		            				if (notifier.end < cutOffset) {
		            				    matchJob.schedule();
		            				    return Status.OK_STATUS;
		            				}
		            			}
	        				}

        					// set the new length of the first partition
        					IOConsolePartition partition = (IOConsolePartition) getPartition(cutOffset);
        					partition.setLength(partition.getOffset() + partition.getLength() - cutOffset);
        					
        					setUpdateInProgress(true);
        					document.replace(0, cutOffset, ""); //$NON-NLS-1$
        					setUpdateInProgress(false);
        					
        					//remove partitions and reset Partition offsets
        					int index = partitions.indexOf(partition);
        					for (int i = 0; i < index; i++) {
                                partitions.remove(0);
                            }
        					
        					int offset = 0;
        					for (Iterator i = partitions.iterator(); i.hasNext(); ) {
        						IOConsolePartition p = (IOConsolePartition) i.next();
        						p.setOffset(offset);
        						offset += p.getLength();
        					}
	        				
	        	    		// buffer has been emptied, reset match listeners
	            			Iterator iter = patterns.iterator();
	            			while (iter.hasNext()) {
	            				CompiledPatternMatchListener notifier = (CompiledPatternMatchListener)iter.next();
	            				notifier.end = notifier.end - cutOffset;
	            				if (notifier.end < 0) {
	            					notifier.end = 0;
	            				}
	            			}
        				}
            			pendingTrim = false;
        			} catch (BadLocationException e) {
        			    pendingTrim = false;
        			}
        		}
        	}
        	return Status.OK_STATUS;
        }
    }
    
    private class MatchJob extends Job {
        MatchJob() {
            super("Match Job"); //$NON-NLS-1$
            setSystem(true);
        }

        /* (non-Javadoc)
         * @see org.eclipse.core.runtime.jobs.Job#run(org.eclipse.core.runtime.IProgressMonitor)
         */
        protected IStatus run(IProgressMonitor monitor) {
            synchronized (overflowLock) {
	        	try {
                    IDocument doc = getDocument();
                    String text = null;
                    int prevBaseOffset = -1;
                    if (doc != null && !monitor.isCanceled()) {
                        boolean allDone = partitionerFinished;
                    	int endOfSearch = doc.getLength();
                    	int indexOfLastChar = endOfSearch;
                    	if (indexOfLastChar > 0) {
                    		indexOfLastChar--;
                    	}
                    	int lastLineToSearch = 0;
                    	int offsetOfLastLineToSearch = 0;
                    	try {
                    		lastLineToSearch = doc.getLineOfOffset(indexOfLastChar);
                    		offsetOfLastLineToSearch = doc.getLineOffset(lastLineToSearch);
                    	} catch (BadLocationException e) {
                    		// perhaps the buffer was re-set 
                    		return Status.OK_STATUS;
                    	}
                    	for (int i = 0; i < patterns.size(); i++) {
                    	    if (monitor.isCanceled()) {
                    	        break;
                    	    }
                    		CompiledPatternMatchListener notifier = (CompiledPatternMatchListener) patterns.get(i);
                    		int baseOffset = notifier.end;
                    		int lengthToSearch = endOfSearch - baseOffset;
                    		if (lengthToSearch > 0) {
                    			try {
                    				if (prevBaseOffset != baseOffset) {
                    					// reuse the text string if possible
                    					text = doc.get(baseOffset, lengthToSearch);
                    				}
                    				Matcher reg = notifier.pattern.matcher(text);
                    				Matcher quick = null;
                    				if (notifier.qualifier != null) {
                    					quick = notifier.qualifier.matcher(text);
                    				}
                    				int startOfNextSearch = 0;
                    				int endOfLastMatch = -1;
                    				int lineOfLastMatch = -1;
                    				while ((startOfNextSearch < lengthToSearch) && !monitor.isCanceled()) {
                    					if (quick != null) {
                    						if (quick.find(startOfNextSearch)) {
                    							// start searching on the beginning of the line where the potential
                    							// match was found, or after the last match on the same line
                    							int matchLine = doc.getLineOfOffset(baseOffset + quick.start());
                    							if (lineOfLastMatch == matchLine) {
                    								startOfNextSearch = endOfLastMatch;
                    							} else {
                    								startOfNextSearch = doc.getLineOffset(matchLine) - baseOffset;
                    							}
                    						} else {
                    							startOfNextSearch = lengthToSearch;
                    						}
                    					}
                    					if (startOfNextSearch < lengthToSearch) {
                    						if (reg.find(startOfNextSearch)) {
                    							endOfLastMatch = reg.end();
                    							lineOfLastMatch = doc.getLineOfOffset(baseOffset + endOfLastMatch - 1);
                    							int regStart = reg.start();
                    							IPatternMatchListener listener = notifier.listener;
                    							if (listener != null && !monitor.isCanceled()) {
                    							    listener.matchFound(new PatternMatchEvent(console, baseOffset + regStart, endOfLastMatch - regStart));
                    							}
                    							startOfNextSearch = endOfLastMatch;
                    						} else {
                    							startOfNextSearch = lengthToSearch;
                    						}
                    					}
                    				}
                    				// update start of next search to the last line searched
                    				// or the end of the last match if it was on the line that
                    				// was last searched
                    				if (lastLineToSearch == lineOfLastMatch) {
                    					notifier.end = baseOffset + endOfLastMatch;
                    				} else {
                    					notifier.end = offsetOfLastLineToSearch;
                    				}
                        		} catch (BadLocationException e) {
                        			ConsolePlugin.log(e);
                            	}
                    		}
                    		prevBaseOffset = baseOffset;
                    		if (allDone) {
                    			int lastLineOfDoc = doc.getNumberOfLines() - 1;
                    			try {
                    				if (doc.getLineLength(lastLineOfDoc) == 0) {
                    					// if the last line is empty, do not consider it
                    					lastLineOfDoc--;
                    				}
                    			} catch (BadLocationException e) {
                    			    ConsolePlugin.log(e);
                    				allDone = false;
                    			}
                    			allDone = allDone && (lastLineToSearch >= lastLineOfDoc);
                    		}
                        }
                    	if (allDone && !monitor.isCanceled() && !closeFired) {
                    	    console.firePropertyChange(this, IOConsole.P_CONSOLE_OUTPUT_COMPLETE, null, null);
                    	    closeFired = true;
                    	    cancel(); // cancels this job if it has already been re-scheduled
                    	}
                    }
                } finally {
                    if (pendingTrim) {
                        trimJob.schedule();
                    }
                }
            }
            return Status.OK_STATUS;
        } 

    }	
    
	
}